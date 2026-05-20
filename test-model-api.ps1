# AEISP Model Module API Test Script (PowerShell)
# Usage: Run in PowerShell: .\test-model-api.ps1
# Prerequisite: Application running on localhost:8080

chcp 65001 | Out-Null
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$baseUrl = "http://localhost:8080"
$adminPassword = "admin123"

function Invoke-Api {
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
    if ($Method -eq "POST" -or $Method -eq "PATCH") {
        $wc.Headers.Add("Content-Type", "application/json")
    }
    try {
        if ($Method -eq "GET") {
            $res = $wc.DownloadString($Uri)
        } elseif ($Method -eq "POST") {
            $res = $wc.UploadString($Uri, "POST", $Body)
        } elseif ($Method -eq "PATCH") {
            $res = $wc.UploadString($Uri, "PATCH", $Body)
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

# 1. Admin login
Write-Host "`n[1/8] Admin login..." -ForegroundColor Cyan
$loginRes = Invoke-Api -Uri "$baseUrl/api/v1/auth/login" -Method POST -Body (@{ username = "admin"; password = $adminPassword } | ConvertTo-Json -Depth 3)
$adminToken = $loginRes.data.accessToken
Write-Host "Admin Token: $($adminToken.Substring(0,30))..." -ForegroundColor Green

$headers = @{ Authorization = "Bearer $adminToken" }

# 2. Create model
Write-Host "`n[2/8] Create model..." -ForegroundColor Cyan
$modelBody = @{
    modelName = "GPT-Test-$(Get-Random -Minimum 1000 -Maximum 9999)"
    modelType = "general"
    apiEndpoint = "https://api.openai.com/v1/chat/completions"
    apiKey = "sk-test"
    weight = 10
    maxQps = 100
    status = 1
    sortOrder = 1
} | ConvertTo-Json -Depth 3
$createRes = Invoke-Api -Uri "$baseUrl/api/v1/models" -Method POST -Headers $headers -Body $modelBody
Write-Host "Create result: $($createRes.message)" -ForegroundColor Green

# 3. List models
Write-Host "`n[3/8] List models..." -ForegroundColor Cyan
$listRes = Invoke-Api -Uri "$baseUrl/api/v1/models?pageNum=1&pageSize=10" -Method GET -Headers $headers
Write-Host "Total models: $($listRes.data.total), Count: $($listRes.data.list.Length)" -ForegroundColor Green
$testModelId = $listRes.data.list[0].id
Write-Host "Test model ID: $testModelId"

# 4. Get model detail
Write-Host "`n[4/8] Get model detail..." -ForegroundColor Cyan
$detailRes = Invoke-Api -Uri "$baseUrl/api/v1/models/$testModelId" -Method GET -Headers $headers
Write-Host "Model name: $($detailRes.data.modelName), Status: $($detailRes.data.status)" -ForegroundColor Green

# 5. Toggle status
Write-Host "`n[5/8] Toggle model status..." -ForegroundColor Cyan
$statusRes = Invoke-Api -Uri "$baseUrl/api/v1/models/$testModelId/status?status=0" -Method POST -Headers $headers
Write-Host "Toggle result: $($statusRes.message)" -ForegroundColor Green

# 6. Update sort order
Write-Host "`n[6/8] Update sort order..." -ForegroundColor Cyan
$sortRes = Invoke-Api -Uri "$baseUrl/api/v1/models/$testModelId/sort-order?sortOrder=99" -Method POST -Headers $headers
Write-Host "Sort update result: $($sortRes.message)" -ForegroundColor Green

# 7. Test model
Write-Host "`n[7/8] Test model..." -ForegroundColor Cyan
$testRes = Invoke-Api -Uri "$baseUrl/api/v1/models/$testModelId/test" -Method POST -Headers $headers -Body (@{ testInput = "hello" } | ConvertTo-Json -Depth 3)
Write-Host "Test result: $($testRes.message)" -ForegroundColor Green

# 8. Get usage stats
Write-Host "`n[8/8] Get usage stats..." -ForegroundColor Cyan
$statsRes = Invoke-Api -Uri "$baseUrl/api/v1/models/$testModelId/stats" -Method GET -Headers $headers
Write-Host "Stats count: $($statsRes.data.Length)" -ForegroundColor Green

Write-Host "`n================ Model Module Test Completed ================" -ForegroundColor Cyan
