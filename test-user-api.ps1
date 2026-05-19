# AEISP User Module API Test Script (PowerShell)
# Usage: Run in PowerShell: .\test-user-api.ps1
# Prerequisite: Application running on localhost:8080, PostgreSQL/Redis running

# Fix Chinese character display in console
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
Write-Host "`n[1/12] Admin login..." -ForegroundColor Cyan
$loginRes = Invoke-Api -Uri "$baseUrl/api/v1/auth/login" -Method POST -Body (@{ username = "admin"; password = $adminPassword } | ConvertTo-Json -Depth 3)
$adminToken = $loginRes.data.accessToken
Write-Host "Admin Token: $($adminToken.Substring(0,30))..." -ForegroundColor Green

$headers = @{ Authorization = "Bearer $adminToken" }

# 2. User list query
Write-Host "`n[2/12] User list query..." -ForegroundColor Cyan
$listRes = Invoke-Api -Uri "$baseUrl/api/v1/users?pageNum=1&pageSize=10" -Method GET -Headers $headers
Write-Host "Total users: $($listRes.data.total), Page count: $($listRes.data.list.Length)" -ForegroundColor Green

# Pick first NORMAL user for state-change tests; fallback to first user
$normalUser = $listRes.data.list | Where-Object { $_.status -eq 1 } | Select-Object -First 1
$testUserId = if ($normalUser) { $normalUser.id } else { $listRes.data.list[0].id }
Write-Host "Test user ID: $testUserId"

# 3. User detail
Write-Host "`n[3/12] User detail..." -ForegroundColor Cyan
$detailRes = Invoke-Api -Uri "$baseUrl/api/v1/users/$testUserId" -Method GET -Headers $headers
Write-Host "Username: $($detailRes.data.username), Status: $($detailRes.data.status)" -ForegroundColor Green

# 4. Create user
Write-Host "`n[4/12] Create user..." -ForegroundColor Cyan
$createBody = @{
    username = "testuser_$(Get-Random -Minimum 1000 -Maximum 9999)"
    password = "Test@12345"
    phone = "138$(Get-Random -Minimum 10000000 -Maximum 99999999)"
    nickname = "Test User"
} | ConvertTo-Json -Depth 3
$createRes = Invoke-Api -Uri "$baseUrl/api/v1/users" -Method POST -Headers $headers -Body $createBody
Write-Host "Create result: $($createRes.message)" -ForegroundColor Green

# 5. Excel import - skip in script, guide user to use Postman
Write-Host "`n[5/12] Excel import..." -ForegroundColor Cyan
Write-Host "Please test manually in Postman: POST $baseUrl/api/v1/users/import-excel" -ForegroundColor Yellow
Write-Host "  Body: form-data, key=file, select a .xlsx file" -ForegroundColor Yellow

# 6. Update status (secondary confirmation)
Write-Host "`n[6/12] Update status (disable + secondary confirmation)..." -ForegroundColor Cyan
$statusBody = @{
    status = 2
    reason = "Test disable"
    adminPassword = $adminPassword
} | ConvertTo-Json -Depth 3
$statusRes = Invoke-Api -Uri "$baseUrl/api/v1/users/$testUserId/status" -Method PATCH -Headers $headers -Body $statusBody
Write-Host "Status update result: $($statusRes.message)" -ForegroundColor Green

# 7. Reset password (secondary confirmation)
Write-Host "`n[7/12] Reset password (secondary confirmation)..." -ForegroundColor Cyan
$resetBody = @{
    adminPassword = $adminPassword
} | ConvertTo-Json -Depth 3
$resetRes = Invoke-Api -Uri "$baseUrl/api/v1/users/$testUserId/reset-password" -Method POST -Headers $headers -Body $resetBody
Write-Host "New password: $($resetRes.data)" -ForegroundColor Green

# 8. Adjust duration (SET mode + secondary confirmation)
Write-Host "`n[8/12] Adjust duration (set to 120 minutes)..." -ForegroundColor Cyan
$durationBody = @{
    adjustType = 3
    deltaMinutes = 120
    reason = "Test direct set duration"
    adminPassword = $adminPassword
} | ConvertTo-Json -Depth 3
$durationRes = Invoke-Api -Uri "$baseUrl/api/v1/users/$testUserId/adjust-duration" -Method POST -Headers $headers -Body $durationBody
Write-Host "Duration adjust result: $($durationRes.message)" -ForegroundColor Green

# 9. Filter query (duration range)
Write-Host "`n[9/12] User list filter (duration range)..." -ForegroundColor Cyan
$filterRes = Invoke-Api -Uri "$baseUrl/api/v1/users?remainingMinutesMin=0&remainingMinutesMax=200&pageNum=1&pageSize=10" -Method GET -Headers $headers
Write-Host "Filter result count: $($filterRes.data.list.Length)" -ForegroundColor Green

# 10. Excel export
Write-Host "`n[10/12] Excel export..." -ForegroundColor Cyan
$exportPath = "$env:TEMP\users_export.xlsx"
try {
    $wc = New-Object System.Net.WebClient
    $wc.Encoding = [System.Text.Encoding]::UTF8
    $wc.Headers.Add("Authorization", "Bearer $adminToken")
    $wc.DownloadFile("$baseUrl/api/v1/users/export-excel?status=2", $exportPath)
    $sizeKB = [math]::Round((Get-Item $exportPath).Length / 1KB, 2)
    Write-Host "Export success, file size: $sizeKB KB, path: $exportPath" -ForegroundColor Green
} catch {
    Write-Host "Export failed: $($_.Exception.Message)" -ForegroundColor Red
}

# 11. User statistics
Write-Host "`n[11/12] User statistics..." -ForegroundColor Cyan
$statsRes = Invoke-Api -Uri "$baseUrl/api/v1/users/statistics" -Method GET -Headers $headers
Write-Host "Total users: $($statsRes.data.totalUsers), Today new: $($statsRes.data.newUsersToday)" -ForegroundColor Green
Write-Host "Normal: $($statsRes.data.normalCount), Disabled: $($statsRes.data.disabledCount), Frozen: $($statsRes.data.frozenCount), Locked: $($statsRes.data.lockedCount)" -ForegroundColor Green
Write-Host "DAU: $($statsRes.data.dau), WAU: $($statsRes.data.wau), MAU: $($statsRes.data.mau)" -ForegroundColor Green

# 12. Growth trend
Write-Host "`n[12/12] Growth trend..." -ForegroundColor Cyan
$trendRes = Invoke-Api -Uri "$baseUrl/api/v1/users/statistics/trend?period=day" -Method GET -Headers $headers
Write-Host "Last 7 days trend:" -ForegroundColor Green
$trendRes.data | ForEach-Object { Write-Host "  $($_.date): $($_.count)" }

Write-Host "`n================ Test Completed ================" -ForegroundColor Cyan
Write-Host "To test login lockout, call /api/v1/auth/login with wrong password 5 times in a row" -ForegroundColor Yellow
