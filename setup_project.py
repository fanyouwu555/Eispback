import psycopg2

conn = psycopg2.connect(host='localhost', database='aeisp', user='postgres', password='postgres')
conn.autocommit = True
cur = conn.cursor()

print('=== Creating prj_project table ===')
cur.execute('''
    CREATE TABLE IF NOT EXISTS prj_project (
        id BIGSERIAL PRIMARY KEY,
        project_name VARCHAR(128) NOT NULL,
        description TEXT,
        user_id BIGINT NOT NULL,
        template_id BIGINT,
        template_version_id BIGINT,
        status SMALLINT NOT NULL DEFAULT 0,
        is_pinned SMALLINT NOT NULL DEFAULT 0,
        run_time_seconds BIGINT NOT NULL DEFAULT 0,
        project_config JSONB,
        remark VARCHAR(500),
        archived_at TIMESTAMP,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP,
        created_by BIGINT,
        updated_by BIGINT,
        deleted SMALLINT NOT NULL DEFAULT 0
    )
''')
print('  OK - CREATE TABLE prj_project')

cur.execute('CREATE INDEX IF NOT EXISTS idx_prj_project_user_id ON prj_project(user_id)')
cur.execute('CREATE INDEX IF NOT EXISTS idx_prj_project_status ON prj_project(status)')
cur.execute('CREATE INDEX IF NOT EXISTS idx_prj_project_created_at ON prj_project(created_at)')
print('  OK - CREATE INDEXES')

print()
print('=== Inserting project permissions ===')
new_permissions = [
    ('project:list', '查看项目', 'project', 'read', '查看项目列表和详情'),
    ('project:manage', '管理项目', 'project', 'manage', '归档、删除项目'),
]
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

print()
print('=== Verification ===')
cur.execute("SELECT table_name FROM information_schema.tables WHERE table_schema='public' ORDER BY table_name")
tables = [r[0] for r in cur.fetchall()]
print('Tables:', tables)

if 'prj_project' in tables:
    cur.execute("SELECT column_name FROM information_schema.columns WHERE table_name='prj_project' ORDER BY ordinal_position")
    cols = [r[0] for r in cur.fetchall()]
    print(f'prj_project columns ({len(cols)}):', cols)

cur.execute("""
    SELECT p.permission_code, p.permission_name
    FROM sys_permission p
    JOIN sys_role_permission rp ON p.id = rp.permission_id
    WHERE rp.role_id = 1 AND p.permission_code LIKE 'project:%'
    ORDER BY p.permission_code
""")
perms = cur.fetchall()
print(f'Project permissions for role 1 ({len(perms)}):')
for row in perms:
    print(f'  {row[0]} - {row[1]}')

cur.close()
conn.close()
print('\nProject table setup completed!')