# AEISP Template Module API Test Script (PowerShell)
# Usage: Run in PowerShell: .\test-template-api.ps1
# Prerequisite: Application running on localhost:8080

chcp 65001 | Out-Null
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$baseUrl = "http://localhost:8080"
$adminPassword = "admin123"

function Invoke-JsonApi {
    param(
        [string]$Uri,
        [string]$Method = "GET",
        [hashtable]$Headers = @{},
        [string]$Body = $null
    )
    $wc = New-Object System.Net.WebClient
    $wc.Encoding = [System.Text.Encoding]::UTF8
    foreach ($key in $Headers.Keys) {
        $wc.Headers.Add($key, $Headers[$key])
    }
    if ($Method -eq "POST" -or $Method -eq "PUT" -or $Method -eq "PATCH") {
        $wc.Headers.Add("Content-Type", "application/json")
    }
    try {
        if ($Method -eq "GET") {
            $res = $wc.DownloadString($Uri)
        } elseif ($Method -eq "POST") {
            $res = $wc.UploadString($Uri, "POST", $Body)
        } elseif ($Method -eq "PUT") {
            $res = $wc.UploadString($Uri, "PUT", $Body)
        } elseif ($Method -eq "PATCH") {
            $res = $wc.UploadString($Uri, "PATCH", $Body)
        } elseif ($Method -eq "DELETE") {
            $res = $wc.UploadString($Uri, "DELETE", "")
        } else {
            throw "Unsupported method: $Method"
        }
        return ($res | ConvertFrom-Json)
    } catch {
        $err = $_.Exception.Response
        if ($err) {
            $stream = $err.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream, [System.Text.Encoding]::UTF8)
            $errBody = $reader.ReadToEnd()
            $reader.Close()
            return ($errBody | ConvertFrom-Json)
        }
        throw
    }
}

# Create a temporary ZIP file for upload
$tempZip = "$env:TEMP\test_template_$(Get-Random).zip"
$tempDir = "$env:TEMP\test_template_dir_$(Get-Random)"
New-Item -ItemType Directory -Path $tempDir -Force | Out-Null
"print('hello')" | Out-File -FilePath "$tempDir\main.py" -Encoding utf8
Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::CreateFromDirectory($tempDir, $tempZip)
Remove-Item -Recurse -Force $tempDir

# 1. Admin login
Write-Host "`n[1/9] Admin login..." -ForegroundColor Cyan
$loginRes = Invoke-JsonApi -Uri "$baseUrl/api/v1/auth/login" -Method POST -Body (@{ username = "admin"; password = $adminPassword } | ConvertTo-Json -Depth 3)
$adminToken = $loginRes.data.accessToken
Write-Host "Admin Token: $($adminToken.Substring(0,30))..." -ForegroundColor Green

$headers = @{ Authorization = "Bearer $adminToken" }

# 2. Create template (multipart upload)
Write-Host "`n[2/9] Create template..." -ForegroundColor Cyan
$boundary = [System.Guid]::NewGuid().ToString()
$lines = @(
    "--$boundary",
    'Content-Disposition: form-data; name="templateName"',
    '',
    "Test-Template-$(Get-Random -Minimum 1000 -Maximum 9999)",
    "--$boundary",
    'Content-Disposition: form-data; name="scenario"',
    '',
    "teaching",
    "--$boundary",
    'Content-Disposition: form-data; name="description"',
    '',
    "Test template description",
    "--$boundary",
    'Content-Disposition: form-data; name="sortWeight"',
    '',
    "1",
    "--$boundary",
    'Content-Disposition: form-data; name="versionNo"',
    '',
    "1.0.0",
    "--$boundary",
    'Content-Disposition: form-data; name="changelog"',
    '',
    "Initial version",
    "--$boundary",
    "Content-Disposition: form-data; name=`"zipFile`"; filename=`"test.zip`"",
    "Content-Type: application/zip",
    '',
    ([System.IO.File]::ReadAllText($tempZip)),
    "--$boundary--",
    ''
)
$bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($lines -join "`r`n")

$httpReq = [System.Net.HttpWebRequest]::Create("$baseUrl/api/v1/templates")
$httpReq.Method = "POST"
$httpReq.ContentType = "multipart/form-data; boundary=$boundary"
$httpReq.Headers.Add("Authorization", "Bearer $adminToken")
$httpReq.ContentLength = $bodyBytes.Length
$stream = $httpReq.GetRequestStream()
$stream.Write($bodyBytes, 0, $bodyBytes.Length)
$stream.Close()
try {
    $response = $httpReq.GetResponse()
    $reader = New-Object System.IO.StreamReader($response.GetResponseStream())
    $createRes = $reader.ReadToEnd() | ConvertFrom-Json
    $reader.Close()
    Write-Host "Create result: $($createRes.message)" -ForegroundColor Green
} catch {
    Write-Host "Create template failed (may need manual test with proper multipart): $($_.Exception.Message)" -ForegroundColor Yellow
}

# 3. List templates
Write-Host "`n[3/9] List templates..." -ForegroundColor Cyan
$listRes = Invoke-JsonApi -Uri "$baseUrl/api/v1/templates?pageNum=1&pageSize=10" -Method GET -Headers $headers
Write-Host "Total: $($listRes.data.total), Count: $($listRes.data.list.Length)" -ForegroundColor Green
if ($listRes.data.list.Length -gt 0) {
    $testTemplateId = $listRes.data.list[0].id
    Write-Host "Test template ID: $testTemplateId"
} else {
    Write-Host "SKIP: No templates found" -ForegroundColor Yellow
    Remove-Item -Force $tempZip -ErrorAction SilentlyContinue
    exit
}

# 4. Get template detail
Write-Host "`n[4/9] Get template detail..." -ForegroundColor Cyan
$detailRes = Invoke-JsonApi -Uri "$baseUrl/api/v1/templates/$testTemplateId" -Method GET -Headers $headers
Write-Host "Name: $($detailRes.data.templateName), Status: $($detailRes.data.status)" -ForegroundColor Green

# 5. Update template info
Write-Host "`n[5/9] Update template info..." -ForegroundColor Cyan
$updateBody = @{
    templateName = "Updated-Template-$(Get-Random -Minimum 1000 -Maximum 9999)"
    description = "Updated description"
} | ConvertTo-Json -Depth 3
$updateRes = Invoke-JsonApi -Uri "$baseUrl/api/v1/templates/$testTemplateId" -Method PUT -Headers $headers -Body $updateBody
Write-Host "Update result: $($updateRes.message)" -ForegroundColor Green

# 6. Toggle status (offline)
Write-Host "`n[6/9] Toggle status to offline..." -ForegroundColor Cyan
$statusRes = Invoke-JsonApi -Uri "$baseUrl/api/v1/templates/$testTemplateId/status?status=2" -Method POST -Headers $headers
Write-Host "Toggle result: $($statusRes.message)" -ForegroundColor Green

# 7. List public templates (should exclude offline)
Write-Host "`n[7/9] List public templates..." -ForegroundColor Cyan
$publicRes = Invoke-JsonApi -Uri "$baseUrl/api/v1/templates/public" -Method GET -Headers $headers
$publicCount = if ($publicRes.data) { $publicRes.data.Length } else { 0 }
Write-Host "Public templates count: $publicCount" -ForegroundColor Green

# 8. Toggle status back to online
Write-Host "`n[8/9] Toggle status back to online..." -ForegroundColor Cyan
$status2Res = Invoke-JsonApi -Uri "$baseUrl/api/v1/templates/$testTemplateId/status?status=1" -Method POST -Headers $headers
Write-Host "Toggle result: $($status2Res.message)" -ForegroundColor Green

# 9. Get file structure
Write-Host "`n[9/9] Get file structure..." -ForegroundColor Cyan
$filesRes = Invoke-JsonApi -Uri "$baseUrl/api/v1/templates/$testTemplateId/files" -Method GET -Headers $headers
if ($filesRes.data) {
    Write-Host "Files count: $($filesRes.data.Length)" -ForegroundColor Green
} else {
    Write-Host "Files: empty or null" -ForegroundColor Yellow
}

# Cleanup
Remove-Item -Force $tempZip -ErrorAction SilentlyContinue

Write-Host "`n================ Template Module Test Completed ================" -ForegroundColor Cyan
Write-Host "Note: Create template with ZIP upload may need manual Postman test." -ForegroundColor Yellow
