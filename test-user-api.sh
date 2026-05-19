#!/bin/bash
# AEISP 用户模块接口测试脚本 (Bash/curl)
# 前置条件: 应用已启动在 localhost:8080

set -e

BASE_URL="http://localhost:8080"
ADMIN_PASSWORD="admin123"

echo "========================================"
echo "AEISP 用户模块接口测试"
echo "========================================"

# 1. 管理员登录
echo -e "\n[1/12] 管理员登录..."
LOGIN_RES=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"$ADMIN_PASSWORD\"}")
ADMIN_TOKEN=$(echo "$LOGIN_RES" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
echo "Token: ${ADMIN_TOKEN:0:30}..."

# 2. 用户列表查询
echo -e "\n[2/12] 用户列表查询..."
LIST_RES=$(curl -s -X GET "$BASE_URL/api/v1/users?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer $ADMIN_TOKEN")
echo "$LIST_RES" | sed -n 's/.*"total":\([0-9]*\).*/Total: \1/p; s/.*"list":\[\([^]]*\)\].*/List items found/p'

# 动态获取第一个用户 ID
TEST_USER_ID=$(echo "$LIST_RES" | sed -n 's/.*"id":\([0-9]*\).*/\1/p' | head -1)
echo "Test user ID: $TEST_USER_ID"

# 3. 用户详情
echo -e "\n[3/12] 用户详情..."
curl -s -X GET "$BASE_URL/api/v1/users/$TEST_USER_ID" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 4. 创建用户
echo -e "\n[4/12] 创建用户..."
RAND=$((1000 + RANDOM % 9000))
curl -s -X POST "$BASE_URL/api/v1/users" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{\"username\":\"apitest_$RAND\",\"password\":\"Test@12345\",\"phone\":\"138${RAND}1234\",\"nickname\":\"API测试用户\"}"

# 5. Excel 导入
echo -e "\n[5/12] Excel 导入 (需在 Postman 中手动上传文件)..."
echo "POST $BASE_URL/api/v1/users/import-excel"
echo "  Body: form-data, key=file, 选择 .xlsx 文件"

# 6. 修改状态（二次确认）
echo -e "\n[6/12] 修改用户状态（禁用 + 二次确认）..."
curl -s -X PATCH "$BASE_URL/api/v1/users/$TEST_USER_ID/status" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{\"status\":2,\"reason\":\"接口测试禁用\",\"adminPassword\":\"$ADMIN_PASSWORD\"}"

# 7. 重置密码（二次确认）
echo -e "\n[7/12] 重置密码（二次确认）..."
RESET_RES=$(curl -s -X POST "$BASE_URL/api/v1/users/$TEST_USER_ID/reset-password" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{\"adminPassword\":\"$ADMIN_PASSWORD\"}")
echo "响应: $RESET_RES"

# 8. 调整时长（SET 设定模式）
echo -e "\n[8/12] 调整时长（设定为 200 分钟）..."
curl -s -X POST "$BASE_URL/api/v1/users/$TEST_USER_ID/adjust-duration" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d "{\"adjustType\":3,\"deltaMinutes\":200,\"reason\":\"接口测试设定\",\"adminPassword\":\"$ADMIN_PASSWORD\"}"

# 9. 筛选查询（时长范围）
echo -e "\n[9/12] 用户列表筛选（时长 0~500 分钟）..."
curl -s -X GET "$BASE_URL/api/v1/users?remainingMinutesMin=0&remainingMinutesMax=500&pageNum=1&pageSize=5" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 10. Excel 导出
echo -e "\n[10/12] Excel 导出..."
EXPORT_PATH="/tmp/users_export.xlsx"
curl -s -X GET "$BASE_URL/api/v1/users/export-excel?status=2" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -o "$EXPORT_PATH"
if [ -f "$EXPORT_PATH" ]; then
    ls -lh "$EXPORT_PATH" | awk '{print $5}'
else
    echo "导出文件未生成"
fi

# 11. 统计
echo -e "\n[11/12] 用户统计..."
curl -s -X GET "$BASE_URL/api/v1/users/statistics" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 12. 趋势
echo -e "\n[12/12] 增长趋势..."
curl -s -X GET "$BASE_URL/api/v1/users/statistics/trend?period=day" \
  -H "Authorization: Bearer $ADMIN_TOKEN"

echo -e "\n========================================"
echo "测试完成"
echo "========================================"
