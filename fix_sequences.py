import psycopg2

conn = psycopg2.connect(host='localhost', database='aeisp', user='postgres', password='postgres')
conn.autocommit = True
cur = conn.cursor()

# New permissions needed by the controllers
new_permissions = [
    ('system:menu:list', '查看菜单', 'system', 'read', '查看菜单树和列表'),
    ('system:menu:manage', '管理菜单', 'system', 'manage', '创建、编辑、删除菜单'),
    ('system:dict:list', '查看字典', 'system', 'read', '查看字典类型和数据'),
    ('system:dict:manage', '管理字典', 'system', 'manage', '创建、编辑、删除字典'),
    ('user:permission:read', '查看用户权限', 'user', 'read', '查看用户业务权限'),
    ('user:permission:update', '修改用户权限', 'user', 'update', '为用户分配业务权限'),
]

print('Inserting new permissions...')
inserted = []
for code, name, resource, action, desc in new_permissions:
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

# Assign all new permissions to ROLE_SUPER_ADMIN (role_id=1)
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

# Verify
cur.execute("""
    SELECT p.permission_code, p.permission_name
    FROM sys_permission p
    JOIN sys_role_permission rp ON p.id = rp.permission_id
    WHERE rp.role_id = 1
    ORDER BY p.permission_code
""")
rows = cur.fetchall()
print(f'\nRole 1 permissions ({len(rows)} total):')
for row in rows:
    print(f'  {row[0]} - {row[1]}')

cur.close()
conn.close()
print('\nDone!')