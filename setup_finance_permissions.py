import psycopg2

conn = psycopg2.connect(host='localhost', database='aeisp', user='postgres', password='postgres')
conn.autocommit = True
cur = conn.cursor()

print('=== Inserting finance permissions ===')
permissions = [
    ('finance:package:manage', '套餐管理', 'finance', 'manage', '创建/编辑/删除时长套餐'),
    ('order:manage', '订单管理', 'finance', 'manage', '查看订单列表和管理订单'),
    ('order:refund', '订单退款', 'finance', 'manage', '对已支付订单进行退款操作'),
    ('finance:balance:adjust', '余额调整', 'finance', 'manage', '扣减/调整用户余额'),
]

inserted = []
for code, name, resource, action, desc in permissions:
    try:
        cur.execute("""
            INSERT INTO sys_permission (permission_name, permission_code, resource_type, action, description, created_at, updated_at, deleted)
            VALUES (%s, %s, %s, %s, %s, NOW(), NOW(), 0)
            ON CONFLICT (permission_code) DO NOTHING
            RETURNING id
        """, (name, code, resource, action, desc))
        row = cur.fetchone()
        if row:
            inserted.append((row[0], code, name))
            print(f'  INSERTED: {code} ({name})')
        else:
            cur.execute("SELECT id FROM sys_permission WHERE permission_code = %s", (code,))
            existing = cur.fetchone()
            if existing:
                print(f'  EXISTS: {code} (id={existing[0]})')
                inserted.append((existing[0], code, name))
    except Exception as e:
        print(f'  FAILED {code}: {e}')

print(f'\nAssigning {len(inserted)} permissions to role_id=1 (ROLE_SUPER_ADMIN)...')
for perm_id, code, name in inserted:
    try:
        cur.execute("""
            INSERT INTO sys_role_permission (role_id, permission_id, created_at)
            VALUES (1, %s, NOW())
            ON CONFLICT (role_id, permission_id) DO NOTHING
        """, (perm_id,))
        print(f'  ASSIGNED: {code} to role 1')
    except Exception as e:
        print(f'  FAILED: {code} -> {e}')

cur.close()
conn.close()
print('\nFinance permissions setup completed!')