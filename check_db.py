import psycopg2

conn = psycopg2.connect(host='localhost', database='aeisp', user='postgres', password='postgres')
cur = conn.cursor()

cur.execute("SELECT column_name FROM information_schema.columns WHERE table_name = 'sys_role' ORDER BY ordinal_position")
print('sys_role columns:', [r[0] for r in cur.fetchall()])

cur.execute("SELECT column_name FROM information_schema.columns WHERE table_name = 'sys_permission' ORDER BY ordinal_position")
print('sys_permission columns:', [r[0] for r in cur.fetchall()])

cur.execute("SELECT COUNT(*) FROM sys_permission")
print('sys_permission count:', cur.fetchone()[0])

cur.execute("SELECT config_key, config_value, environment FROM sys_config WHERE config_key = 'official_website_url'")
print('official_website_url:', cur.fetchall())

cur.close()
conn.close()
