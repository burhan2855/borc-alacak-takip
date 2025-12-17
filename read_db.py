import sqlite3
conn = sqlite3.connect('debt_database_copy')
c = conn.cursor()
print('Transactions:')
c.execute('SELECT id, title, amount, isDebt, status, category, documentId FROM transactions ORDER BY id DESC LIMIT 50')
rows = c.fetchall()
for r in rows:
    print(r)
print('\nPartial payments:')
c.execute('SELECT id, transactionId, amount, date FROM partial_payments ORDER BY id DESC LIMIT 50')
for r in c.fetchall():
    print(r)
conn.close()
