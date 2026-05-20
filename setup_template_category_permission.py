import psycopg2

conn = psycopg2.connect(host='localhost', database='aeisp', user='postgres', password='postgres')
conn.autocommit = True
cur = conn.cursor()

# (permission_code, permission_name, resource_type, action, description)
new_permissions = [
    ('template:category:manage', '管理模板分类', 'template', 'manage', '新增、编辑、删除模板三级分类'),
]

inserted = []
for code, name, resource, action, desc in new_permissions:
    cur.execute("""
        INSERT INTO sys_permission (permission_name, permission_code, resource_type, action, description, created_at, updated_at, deleted)
        VALUES (%s, %s, %s, %s, %s, NOW(), NOW(), 0)
        ON CONFLICT (permission_code) DO NOTHING
        RETURNING id
    """, (name, code, resource, action, desc))
    row = cur.fetchone()
    if row:
        inserted.append((row[0], code, name))
        print(f'INSERTED: {code} (id={row[0]})')
    else:
        cur.execute("SELECT id FROM sys_permission WHERE permission_code = %s", (code,))
        existing = cur.fetchone()
        if existing:
            print(f'ALREADY EXISTS: {code} (id={existing[0]})')
            inserted.append((existing[0], code, name))
        else:
            print(f'FAILED: {code}')

# assign to role 1 (super admin)
for perm_id, code, name in inserted:
    cur.execute("""
        INSERT INTO sys_role_permission (role_id, permission_id, created_at)
        VALUES (1, %s, NOW())
        ON CONFLICT (role_id, permission_id) DO NOTHING
    """, (perm_id,))
    print(f'  Assigned {code} to role 1')

cur.close()
conn.close()
print('Done.')