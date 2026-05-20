$baseUri = "http://localhost:8080/api/v1"

# 1. Login to get token
$loginBody = @{ username = "admin"; password = "admin123" } | ConvertTo-Json -Depth 10
$login = Invoke-RestMethod -Uri "$baseUri/auth/login" -Method POST -ContentType "application/json" -Body $loginBody
$token = $login.data.accessToken
$headers = @{ Authorization = "Bearer $token" }

Write-Host "=== Test 1: List Permissions ==="
$perms = Invoke-RestMethod -Uri "$baseUri/system/permissions" -Headers $headers
Write-Host "Permissions count: $($perms.data.Count)"
if ($perms.data.Count -eq 0) { Write-Host "FAIL: No permissions" -ForegroundColor Red } else { Write-Host "PASS" -ForegroundColor Green }

Write-Host "`n=== Test 2: List Roles ==="
$roles = Invoke-RestMethod -Uri "$baseUri/system/roles" -Headers $headers
Write-Host "Roles count: $($roles.data.Count)"
if ($roles.data.Count -eq 0) { Write-Host "FAIL: No roles" -ForegroundColor Red } else { Write-Host "PASS" -ForegroundColor Green }

Write-Host "`n=== Test 3: Create Role ==="
$testRoleCode = "test:role_$(Get-Date -Format 'HHmmss')"
$createBody = @{ roleName = "TestRole"; roleCode = $testRoleCode; description = "Test role"; permissionIds = @(1,2) } | ConvertTo-Json -Depth 10
$res = Invoke-RestMethod -Uri "$baseUri/system/roles" -Method POST -Headers $headers -ContentType "application/json" -Body $createBody
if ($res.code -eq 200) {
    Write-Host "PASS: Create role returned 200" -ForegroundColor Green
} else {
    Write-Host "FAIL: Create role failed - $($res.message)" -ForegroundColor Red
}

Write-Host "`n=== Test 4: Delete Custom Role ==="
# Find and delete the test role
$roles = Invoke-RestMethod -Uri "$baseUri/system/roles" -Headers $headers
$testRole = $roles.data | Where-Object { $_.roleCode -eq $testRoleCode }
if ($testRole) {
    $res = Invoke-RestMethod -Uri "$baseUri/system/roles/$($testRole.id)" -Method DELETE -Headers $headers
    if ($res.code -eq 200) {
        Write-Host "PASS: Delete custom role returned 200" -ForegroundColor Green
    } else {
        Write-Host "FAIL: Delete custom role failed - $($res.message)" -ForegroundColor Red
    }
} else {
    Write-Host "SKIP: Test role not found"
}

Write-Host "`n=== Test 5: Delete System Role (should fail) ==="
$res = Invoke-RestMethod -Uri "$baseUri/system/roles/1" -Method DELETE -Headers $headers
if ($res.code -eq 400) {
    Write-Host "PASS: Correctly rejected system role deletion with 400" -ForegroundColor Green
} else {
    Write-Host "FAIL: Unexpected response code $($res.code)" -ForegroundColor Red
}

Write-Host "`n=== Test 6: Get Config Value (multi-env) ==="
$config = Invoke-RestMethod -Uri "$baseUri/system/configs/official_website_url" -Headers $headers
Write-Host "Config value: $($config.data)"
if ($config.data) { Write-Host "PASS: Config value returned" -ForegroundColor Green } else { Write-Host "FAIL: Config value is null" -ForegroundColor Red }

Write-Host "`n=== Test 7: Update Config ==="
$updateBody = @{ configValue = "https://updated.example.com" } | ConvertTo-Json -Depth 10
try {
    Invoke-RestMethod -Uri "$baseUri/system/configs/official_website_url" -Method PUT -Headers $headers -ContentType "application/json" -Body $updateBody
    $config = Invoke-RestMethod -Uri "$baseUri/system/configs/official_website_url" -Headers $headers
    if ($config.data -eq "https://updated.example.com") {
        Write-Host "PASS: Config updated successfully" -ForegroundColor Green
    } else {
        Write-Host "FAIL: Config not updated, value=$($config.data)" -ForegroundColor Red
    }
    # Restore original
    $restoreBody = @{ configValue = "https://aeisp.example.com" } | ConvertTo-Json -Depth 10
    Invoke-RestMethod -Uri "$baseUri/system/configs/official_website_url" -Method PUT -Headers $headers -ContentType "application/json" -Body $restoreBody
} catch {
    Write-Host "FAIL: Update config failed - $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Test 8: List Operation Logs ==="
$logs = Invoke-RestMethod -Uri "$baseUri/system/logs" -Headers $headers
Write-Host "Logs total: $($logs.data.total)"
if ($logs.data.total -ge 0) { Write-Host "PASS: Logs query works" -ForegroundColor Green } else { Write-Host "FAIL" -ForegroundColor Red }

Write-Host "`n=== Test 9: Export Operation Logs ==="
try {
    $exportPath = "$env:TEMP\operation_logs_export.xlsx"
    Invoke-RestMethod -Uri "$baseUri/system/logs/export" -Headers $headers -OutFile $exportPath
    $sizeKB = [math]::Round((Get-Item $exportPath).Length / 1KB, 2)
    Write-Host "Export success, file size: $sizeKB KB" -ForegroundColor Green
} catch {
    Write-Host "FAIL: Export failed - $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Test 10: List Users ==="
$users = Invoke-RestMethod -Uri "$baseUri/system/users" -Headers $headers
Write-Host "Users total: $($users.data.total)"
if ($users.data.total -ge 0) { Write-Host "PASS: Users query works" -ForegroundColor Green } else { Write-Host "FAIL" -ForegroundColor Red }

Write-Host "`nAll tests completed!"
