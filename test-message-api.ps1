# AEISP Message Module API Test Script (PowerShell)
# Usage: Run in PowerShell: .\test-message-api.ps1
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
    if ($Method -eq "POST" -or $Method -eq "PATCH" -or $Method -eq "PUT") {
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
Write-Host "`n[1/10] Admin login..." -ForegroundColor Cyan
$loginRes = Invoke-Api -Uri "$baseUrl/api/v1/auth/login" -Method POST -Body (@{ username = "admin"; password = $adminPassword } | ConvertTo-Json -Depth 3)
$adminToken = $loginRes.data.accessToken
Write-Host "Admin Token: $($adminToken.Substring(0,30))..." -ForegroundColor Green

$headers = @{ Authorization = "Bearer $adminToken" }

# 2. Create notification (draft)
Write-Host "`n[2/10] Create notification..." -ForegroundColor Cyan
$notifBody = @{
    title = "Test Notification $(Get-Random -Minimum 1000 -Maximum 9999)"
    content = "<p>This is a test message content.</p>"
    msgType = 1
    pushScope = 1
    pushType = 1
} | ConvertTo-Json -Depth 3
$createRes = Invoke-Api -Uri "$baseUrl/api/v1/messages" -Method POST -Headers $headers -Body $notifBody
Write-Host "Create result: $($createRes.message)" -ForegroundColor Green

# 3. List notifications
Write-Host "`n[3/10] List notifications..." -ForegroundColor Cyan
$listRes = Invoke-Api -Uri "$baseUrl/api/v1/messages?pageNum=1&pageSize=10" -Method GET -Headers $headers
Write-Host "Total: $($listRes.data.total), Count: $($listRes.data.list.Length)" -ForegroundColor Green
$testNotifId = $listRes.data.list[0].id
Write-Host "Test notification ID: $testNotifId"

# 4. Get notification detail
Write-Host "`n[4/10] Get notification detail..." -ForegroundColor Cyan
$detailRes = Invoke-Api -Uri "$baseUrl/api/v1/messages/$testNotifId" -Method GET -Headers $headers
Write-Host "Title: $($detailRes.data.title), Status: $($detailRes.data.status)" -ForegroundColor Green

# 5. Push notification
Write-Host "`n[5/10] Push notification..." -ForegroundColor Cyan
$pushRes = Invoke-Api -Uri "$baseUrl/api/v1/messages/$testNotifId/push" -Method POST -Headers $headers
Write-Host "Push result: $($pushRes.message)" -ForegroundColor Green

# 6. Top notification
Write-Host "`n[6/10] Top notification..." -ForegroundColor Cyan
$topRes = Invoke-Api -Uri "$baseUrl/api/v1/messages/$testNotifId/top?isTop=1" -Method POST -Headers $headers
Write-Host "Top result: $($topRes.message)" -ForegroundColor Green

# 7. Revoke notification
Write-Host "`n[7/10] Revoke notification..." -ForegroundColor Cyan
$revokeRes = Invoke-Api -Uri "$baseUrl/api/v1/messages/$testNotifId/revoke" -Method POST -Headers $headers
Write-Host "Revoke result: $($revokeRes.message)" -ForegroundColor Green

# 8. Archive notification
Write-Host "`n[8/10] Archive notification..." -ForegroundColor Cyan
$archiveRes = Invoke-Api -Uri "$baseUrl/api/v1/messages/$testNotifId/archive" -Method POST -Headers $headers
Write-Host "Archive result: $($archiveRes.message)" -ForegroundColor Green

# 9. Get unread count (as user)
Write-Host "`n[9/10] Get unread count (as admin user)..." -ForegroundColor Cyan
$unreadRes = Invoke-Api -Uri "$baseUrl/api/v1/messages/my/unread-count" -Method GET -Headers $headers
Write-Host "Unread count: $($unreadRes.data)" -ForegroundColor Green

# 10. List my notifications
Write-Host "`n[10/10] List my notifications..." -ForegroundColor Cyan
$myRes = Invoke-Api -Uri "$baseUrl/api/v1/messages/my?pageNum=1&pageSize=10" -Method GET -Headers $headers
Write-Host "My messages count: $($myRes.data.list.Length)" -ForegroundColor Green

Write-Host "`n================ Message Module Test Completed ================" -ForegroundColor Cyan
