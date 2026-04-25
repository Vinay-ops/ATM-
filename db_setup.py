import sqlite3

def init_db():
    conn = sqlite3.connect('bkv_bank.db')
    cursor = conn.cursor()

    # Account Table
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS account (
        acc_no INTEGER PRIMARY KEY,
        name TEXT NOT NULL,
        pin INTEGER NOT NULL,
        balance REAL
    )
    ''')

    # Transactions Table
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS transactions (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        acc_no INTEGER,
        type TEXT,
        amount REAL,
        date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )
    ''')

    # Notes Inventory Table (Optional for web app, but keeping for compatibility)
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS notes_inventory (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        note_500 INTEGER DEFAULT 100,
        note_200 INTEGER DEFAULT 100,
        note_100 INTEGER DEFAULT 100,
        note_50 INTEGER DEFAULT 100,
        note_20 INTEGER DEFAULT 100,
        note_10 INTEGER DEFAULT 100,
        last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    )
    ''')

    # Insert a default account if it doesn't exist
    cursor.execute("SELECT * FROM account WHERE acc_no = 1")
    if not cursor.fetchone():
        cursor.execute("INSERT INTO account (acc_no, name, pin, balance) VALUES (1, 'John Doe', 1234, 5000.00)")
        
    conn.commit()
    conn.close()
    print("Database initialized successfully.")

if __name__ == '__main__':
    init_db()
