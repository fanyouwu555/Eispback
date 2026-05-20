# AEISP Recharge Module API Test Script (PowerShell)
# Usage: Run in PowerShell: .\test-recharge-api.ps1
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
Write-Host "`n[1/14] Admin login..." -ForegroundColor Cyan
$loginRes = Invoke-Api -Uri "$baseUrl/api/v1/auth/login" -Method POST -Body (@{ username = "admin"; password = $adminPassword } | ConvertTo-Json -Depth 3)
$adminToken = $loginRes.data.accessToken
Write-Host "Admin Token: $($adminToken.Substring(0,30))..." -ForegroundColor Green

$headers = @{ Authorization = "Bearer $adminToken" }

# 2. Create package
Write-Host "`n[2/14] Create package..." -ForegroundColor Cyan
$pkgBody = @{
    packageName = "Test-Package-$(Get-Random -Minimum 1000 -Maximum 9999)"
    price = 9900
    durationHours = 100
    validDays = 365
    status = 1
    sortOrder = 1
} | ConvertTo-Json -Depth 3
$pkgRes = Invoke-Api -Uri "$baseUrl/api/v1/recharge/packages" -Method POST -Headers $headers -Body $pkgBody
Write-Host "Create package result: $($pkgRes.message)" -ForegroundColor Green

# 3. List packages
Write-Host "`n[3/14] List packages..." -ForegroundColor Cyan
$pkgListRes = Invoke-Api -Uri "$baseUrl/api/v1/recharge/packages?pageNum=1&pageSize=10" -Method GET -Headers $headers
Write-Host "Total packages: $($pkgListRes.data.total), Count: $($pkgListRes.data.list.Length)" -ForegroundColor Green
$testPkgId = $pkgListRes.data.list[0].id
Write-Host "Test package ID: $testPkgId"

# 4. List active packages
Write-Host "`n[4/14] List active packages..." -ForegroundColor Cyan
$activeRes = Invoke-Api -Uri "$baseUrl/api/v1/recharge/packages/active" -Method GET -Headers $headers
Write-Host "Active packages count: $($activeRes.data.Length)" -ForegroundColor Green

# 5. Create order
Write-Host "`n[5/14] Create order..." -ForegroundColor Cyan
$orderRes = Invoke-Api -Uri "$baseUrl/api/v1/recharge/orders?userId=1&packageId=$testPkgId" -Method POST -Headers $headers
Write-Host "Create order result: $($orderRes.message), OrderNo: $($orderRes.data.orderNo)" -ForegroundColor Green
$testOrderNo = $orderRes.data.orderNo

# 6. List orders
Write-Host "`n[6/14] List orders..." -ForegroundColor Cyan
$orderListRes = Invoke-Api -Uri "$baseUrl/api/v1/recharge/orders?pageNum=1&pageSize=10" -Method GET -Headers $headers
Write-Host "Total orders: $($orderListRes.data.total)" -ForegroundColor Green

# 7. Pay order
Write-Host "`n[7/14] Pay order..." -ForegroundColor Cyan
$payRes = Invoke-Api -Uri "$baseUrl/api/v1/recharge/orders/$testOrderNo/pay?payType=alipay" -Method POST -Headers $headers
Write-Host "Pay result: $($payRes.message)" -ForegroundColor Green

# 8. Refund order
Write-Host "`n[8/14] Refund order..." -ForegroundColor Cyan
$refundRes = Invoke-Api -Uri "$baseUrl/api/v1/recharge/orders/$testOrderNo/refund?reason=test refund&adminPassword=$adminPassword" -Method POST -Headers $headers
Write-Host "Refund result: $($refundRes.message)" -ForegroundColor Green

# Create another order for cancel test
Write-Host "`n[Creating another order for cancel test...]" -ForegroundColor DarkGray
$order2Res = Invoke-Api -Uri "$baseUrl/api/v1/recharge/orders?userId=1&packageId=$testPkgId" -Method POST -Headers $headers
$cancelOrderNo = $order2Res.data.orderNo
Write-Host "Cancel test orderNo: $cancelOrderNo"

# 9. Cancel order
Write-Host "`n[9/14] Cancel order..." -ForegroundColor Cyan
$cancelRes = Invoke-Api -Uri "$baseUrl/api/v1/recharge/orders/$cancelOrderNo/cancel" -Method POST -Headers $headers
Write-Host "Cancel result: $($cancelRes.message)" -ForegroundColor Green

# 10. Get balance
Write-Host "`n[10/14] Get balance..." -ForegroundColor Cyan
$balanceRes = Invoke-Api -Uri "$baseUrl/api/v1/recharge/balance/1" -Method GET -Headers $headers
Write-Host "Balance: $($balanceRes.data.balance) cents" -ForegroundColor Green

# 11. Recharge balance
Write-Host "`n[11/14] Recharge balance..." -ForegroundColor Cyan
$rechargeRes = Invoke-Api -Uri "$baseUrl/api/v1/recharge/balance/1/recharge?amount=5000" -Method POST -Headers $headers
Write-Host "Recharge result: $($rechargeRes.message)" -ForegroundColor Green

# 12. Deduct balance
Write-Host "`n[12/14] Deduct balance..." -ForegroundColor Cyan
$deductRes = Invoke-Api -Uri "$baseUrl/api/v1/recharge/balance/1/deduct?amount=1000&reason=test deduction&adminPassword=$adminPassword" -Method POST -Headers $headers
Write-Host "Deduct result: $($deductRes.message)" -ForegroundColor Green

# 13. Get duration stats
Write-Host "`n[13/14] Get duration stats..." -ForegroundColor Cyan
$durStatsRes = Invoke-Api -Uri "$baseUrl/api/v1/recharge/duration/1/stats" -Method GET -Headers $headers
Write-Host "Remaining: $($durStatsRes.data.remainingDuration) min" -ForegroundColor Green

# 14. Add duration
Write-Host "`n[14/14] Add duration..." -ForegroundColor Cyan
$addDurRes = Invoke-Api -Uri "$baseUrl/api/v1/recharge/duration/1/add?minutes=60&reason=test add" -Method POST -Headers $headers
Write-Host "Add duration result: $($addDurRes.message)" -ForegroundColor Green

Write-Host "`n================ Recharge Module Test Completed ================" -ForegroundColor Cyan
