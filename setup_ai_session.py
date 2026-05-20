import psycopg2

conn = psycopg2.connect(host='localhost', database='aeisp', user='postgres', password='postgres')
conn.autocommit = True
cur = conn.cursor()

print('=== Creating ai_session table ===')
cur.execute('''
    CREATE TABLE IF NOT EXISTS ai_session (
        id BIGSERIAL PRIMARY KEY,
        user_id BIGINT NOT NULL,
        model_id BIGINT,
        session_title VARCHAR(200),
        status SMALLINT NOT NULL DEFAULT 1,
        message_count INT NOT NULL DEFAULT 0,
        total_tokens INT NOT NULL DEFAULT 0,
        started_at TIMESTAMP,
        ended_at TIMESTAMP,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP,
        created_by BIGINT,
        updated_by BIGINT,
        deleted SMALLINT NOT NULL DEFAULT 0
    )
''')
print('  OK - CREATE TABLE ai_session')
cur.execute('CREATE INDEX IF NOT EXISTS idx_ai_session_user_id ON ai_session(user_id)')
cur.execute('CREATE INDEX IF NOT EXISTS idx_ai_session_status ON ai_session(status)')
cur.execute('CREATE INDEX IF NOT EXISTS idx_ai_session_created_at ON ai_session(created_at)')
print('  OK - CREATE INDEXES')

print()
print('=== Creating ai_message table ===')
cur.execute('''
    CREATE TABLE IF NOT EXISTS ai_message (
        id BIGSERIAL PRIMARY KEY,
        session_id BIGINT NOT NULL,
        role VARCHAR(20) NOT NULL,
        content TEXT,
        tokens_used INT DEFAULT 0,
        model_id BIGINT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )
''')
print('  OK - CREATE TABLE ai_message')
cur.execute('CREATE INDEX IF NOT EXISTS idx_ai_message_session_id ON ai_message(session_id)')
print('  OK - CREATE INDEXES')

print()
print('=== Inserting AI session permissions ===')
permissions = [
    ('ai:session:read', '查看会话', 'ai', 'read', '查看AI对话会话列表和详情'),
    ('ai:session:manage', '管理会话', 'ai', 'manage', '归档和删除AI对话会话'),
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
            inserted.append((row[0], code))
            print(f'  INSERTED: {code}')
        else:
            cur.execute("SELECT id FROM sys_permission WHERE permission_code = %s", (code,))
            existing = cur.fetchone()
            if existing:
                inserted.append((existing[0], code))
                print(f'  EXISTS: {code}')
    except Exception as e:
        print(f'  FAILED {code}: {e}')

print(f'\nAssigning {len(inserted)} permissions to role_id=1...')
for perm_id, code in inserted:
    try:
        cur.execute("""
            INSERT INTO sys_role_permission (role_id, permission_id, created_at)
            VALUES (1, %s, NOW())
            ON CONFLICT (role_id, permission_id) DO NOTHING
        """, (perm_id,))
        print(f'  ASSIGNED: {code}')
    except Exception as e:
        print(f'  FAILED: {code} -> {e}')

print()
print('=== Verification ===')
for table in ['ai_session', 'ai_message']:
    cur.execute("SELECT column_name FROM information_schema.columns WHERE table_name=%s ORDER BY ordinal_position", (table,))
    cols = [r[0] for r in cur.fetchall()]
    print(f'{table} columns ({len(cols)}):', cols)

cur.close()
conn.close()
print('\nAI session tables setup completed!')