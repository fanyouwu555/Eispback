import psycopg2

conn = psycopg2.connect(host='localhost', database='aeisp', user='postgres', password='postgres')
conn.autocommit = True
cur = conn.cursor()

print('=== Step 1: Extend sys_permission table ===')
alter_columns = [
    ('parent_id', 'BIGINT DEFAULT 0'),
    ('menu_type', 'SMALLINT DEFAULT 0'),
    ('sort_order', 'INT DEFAULT 0'),
    ('icon', 'VARCHAR(100)'),
    ('route_path', 'VARCHAR(200)'),
    ('component', 'VARCHAR(255)'),
    ('is_visible', 'SMALLINT DEFAULT 1'),
    ('is_cache', 'SMALLINT DEFAULT 1'),
]
for col_name, col_type in alter_columns:
    try:
        cur.execute(f'ALTER TABLE sys_permission ADD COLUMN IF NOT EXISTS {col_name} {col_type}')
        print(f'  OK - ADD COLUMN {col_name}')
    except Exception as e:
        print(f'  FAIL - {col_name}: {e}')

print()
print('=== Step 2: Create sys_dict_type table ===')
try:
    cur.execute('''
        CREATE TABLE IF NOT EXISTS sys_dict_type (
            id BIGSERIAL PRIMARY KEY,
            dict_name VARCHAR(100) NOT NULL,
            dict_code VARCHAR(100) NOT NULL,
            description VARCHAR(500),
            status SMALLINT NOT NULL DEFAULT 1,
            is_system SMALLINT NOT NULL DEFAULT 0,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP,
            created_by BIGINT,
            updated_by BIGINT,
            deleted SMALLINT NOT NULL DEFAULT 0,
            CONSTRAINT uk_sys_dict_type_code UNIQUE (dict_code)
        )
    ''')
    cur.execute('CREATE INDEX IF NOT EXISTS idx_sys_dict_type_status ON sys_dict_type(status)')
    print('  OK - CREATE TABLE sys_dict_type')
except Exception as e:
    print(f'  FAIL - {e}')

print()
print('=== Step 3: Create sys_dict_data table ===')
try:
    cur.execute('''
        CREATE TABLE IF NOT EXISTS sys_dict_data (
            id BIGSERIAL PRIMARY KEY,
            dict_code VARCHAR(100) NOT NULL,
            item_label VARCHAR(100) NOT NULL,
            item_value VARCHAR(100) NOT NULL,
            sort_order INT NOT NULL DEFAULT 0,
            status SMALLINT NOT NULL DEFAULT 1,
            color VARCHAR(50),
            is_default SMALLINT NOT NULL DEFAULT 0,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP,
            created_by BIGINT,
            updated_by BIGINT,
            deleted SMALLINT NOT NULL DEFAULT 0
        )
    ''')
    cur.execute('CREATE INDEX IF NOT EXISTS idx_sys_dict_data_dict_code ON sys_dict_data(dict_code)')
    cur.execute('CREATE INDEX IF NOT EXISTS idx_sys_dict_data_sort_order ON sys_dict_data(sort_order)')
    print('  OK - CREATE TABLE sys_dict_data')
except Exception as e:
    print(f'  FAIL - {e}')

print()
print('=== Step 4: Create usr_user_permission table ===')
try:
    cur.execute('''
        CREATE TABLE IF NOT EXISTS usr_user_permission (
            id BIGSERIAL PRIMARY KEY,
            user_id BIGINT NOT NULL,
            perm_key VARCHAR(100) NOT NULL,
            perm_value VARCHAR(500) NOT NULL DEFAULT '',
            effective_at TIMESTAMP,
            expire_at TIMESTAMP,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP,
            created_by BIGINT,
            updated_by BIGINT,
            deleted SMALLINT NOT NULL DEFAULT 0,
            CONSTRAINT uk_usr_user_perm UNIQUE (user_id, perm_key)
        )
    ''')
    cur.execute('CREATE INDEX IF NOT EXISTS idx_usr_user_permission_user_id ON usr_user_permission(user_id)')
    print('  OK - CREATE TABLE usr_user_permission')
except Exception as e:
    print(f'  FAIL - {e}')

print()
print('=== Verification ===')
cur.execute("SELECT table_name FROM information_schema.tables WHERE table_schema='public' ORDER BY table_name")
tables = [r[0] for r in cur.fetchall()]
print('All tables:', tables)

for tbl in ['sys_permission', 'sys_dict_type', 'sys_dict_data', 'usr_user_permission']:
    if tbl in tables:
        cur.execute(f"SELECT column_name FROM information_schema.columns WHERE table_name = '{tbl}' ORDER BY ordinal_position")
        cols = [r[0] for r in cur.fetchall()]
        print(f'{tbl} ({len(cols)} fields):', cols)
    else:
        print(f'{tbl}: NOT FOUND')

print()
print('=== Check usr_user columns ===')
cur.execute("SELECT column_name FROM information_schema.columns WHERE table_name='usr_user' ORDER BY ordinal_position")
usr_cols = [r[0] for r in cur.fetchall()]
print('usr_user columns:', usr_cols)

if 'is_competition' not in usr_cols:
    cur.execute('ALTER TABLE usr_user ADD COLUMN is_competition SMALLINT DEFAULT 0')
    print('Added is_competition column')
else:
    print('is_competition already exists')

if 'need_change_password' not in usr_cols:
    cur.execute('ALTER TABLE usr_user ADD COLUMN need_change_password SMALLINT DEFAULT 0')
    print('Added need_change_password column')
else:
    print('need_change_password already exists')

if 'locked_until' not in usr_cols:
    cur.execute('ALTER TABLE usr_user ADD COLUMN locked_until TIMESTAMP')
    print('Added locked_until column')
else:
    print('locked_until already exists')

if 'failed_login_attempts' not in usr_cols:
    cur.execute('ALTER TABLE usr_user ADD COLUMN failed_login_attempts INT DEFAULT 0')
    print('Added failed_login_attempts column')
else:
    print('failed_login_attempts already exists')

if 'invitation_code_used' not in usr_cols:
    cur.execute('ALTER TABLE usr_user ADD COLUMN invitation_code_used VARCHAR(50)')
    print('Added invitation_code_used column')
else:
    print('invitation_code_used already exists')

if 'last_login_time' not in usr_cols:
    cur.execute('ALTER TABLE usr_user ADD COLUMN last_login_time TIMESTAMP')
    print('Added last_login_time column')
else:
    print('last_login_time already exists')

if 'last_login_ip' not in usr_cols:
    cur.execute('ALTER TABLE usr_user ADD COLUMN last_login_ip VARCHAR(64)')
    print('Added last_login_ip column')
else:
    print('last_login_ip already exists')

if 'register_ip' not in usr_cols:
    cur.execute('ALTER TABLE usr_user ADD COLUMN register_ip VARCHAR(64)')
    print('Added register_ip column')
else:
    print('register_ip already exists')

if 'register_device_info' not in usr_cols:
    cur.execute('ALTER TABLE usr_user ADD COLUMN register_device_info TEXT')
    print('Added register_device_info column')
else:
    print('register_device_info already exists')

if 'register_time' not in usr_cols:
    cur.execute('ALTER TABLE usr_user ADD COLUMN register_time TIMESTAMP')
    print('Added register_time column')
else:
    print('register_time already exists')

if 'nickname' not in usr_cols:
    cur.execute('ALTER TABLE usr_user ADD COLUMN nickname VARCHAR(50)')
    print('Added nickname column')
else:
    print('nickname already exists')

if 'avatar_url' not in usr_cols:
    cur.execute('ALTER TABLE usr_user ADD COLUMN avatar_url VARCHAR(255)')
    print('Added avatar_url column')
else:
    print('avatar_url already exists')

if 'status_reason' not in usr_cols:
    cur.execute('ALTER TABLE usr_user ADD COLUMN status_reason VARCHAR(200)')
    print('Added status_reason column')
else:
    print('status_reason already exists')

if 'need_change_password' not in usr_cols:
    cur.execute('ALTER TABLE usr_user ADD COLUMN need_change_password SMALLINT DEFAULT 0')
    print('Added need_change_password column')
else:
    print('need_change_password already exists')

print()
print('Final usr_user columns:')
cur.execute("SELECT column_name FROM information_schema.columns WHERE table_name='usr_user' ORDER BY ordinal_position")
print([r[0] for r in cur.fetchall()])

cur.close()
conn.close()
print()
print('Database fix completed!')