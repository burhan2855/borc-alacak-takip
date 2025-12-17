import sqlite3

def query(sql):
    conn = sqlite3.connect('debt_database')
    conn.row_factory = sqlite3.Row
    cur = conn.cursor()
    cur.execute(sql)
    rows = cur.fetchall()
    conn.close()
    return rows

print('Latest transactions:')
for r in query('SELECT id, title, amount, isDebt, status, category, date FROM transactions ORDER BY id DESC LIMIT 50'):
    print(dict(r))

print('\nLatest partial_payments:')
for r in query('SELECT id, transactionId, amount, date FROM partial_payments ORDER BY id DESC LIMIT 50'):
    print(dict(r))
