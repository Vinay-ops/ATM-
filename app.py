import sqlite3
from flask import Flask, render_template, request, redirect, url_for, session, flash
from datetime import datetime

app = Flask(__name__)
app.secret_key = 'bkv_bank_super_secret_key'  # Used for session management

def get_db_connection():
    conn = sqlite3.connect('bkv_bank.db')
    conn.row_factory = sqlite3.Row
    return conn

@app.route('/', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        pin = request.form.get('pin')
        acc_no = request.form.get('acc_no')
        
        if not pin or not acc_no:
            flash('Please enter both Account Number and PIN.', 'danger')
            return render_template('login.html')

        conn = get_db_connection()
        user = conn.execute('SELECT * FROM account WHERE acc_no = ? AND pin = ?', (acc_no, pin)).fetchone()
        conn.close()

        if user:
            session['acc_no'] = user['acc_no']
            session['name'] = user['name']
            return redirect(url_for('dashboard'))
        else:
            flash('Invalid Account Number or PIN.', 'danger')
    
    # If already logged in
    if 'acc_no' in session:
        return redirect(url_for('dashboard'))
        
    return render_template('login.html')

@app.route('/dashboard')
def dashboard():
    if 'acc_no' not in session:
        return redirect(url_for('login'))
        
    conn = get_db_connection()
    user = conn.execute('SELECT * FROM account WHERE acc_no = ?', (session['acc_no'],)).fetchone()
    transactions = conn.execute('SELECT * FROM transactions WHERE acc_no = ? ORDER BY date DESC LIMIT 5', (session['acc_no'],)).fetchall()
    conn.close()
    
    return render_template('dashboard.html', user=user, transactions=transactions)

@app.route('/transaction', methods=['POST'])
def transaction():
    if 'acc_no' not in session:
        return redirect(url_for('login'))

    action = request.form.get('action')
    try:
        amount = float(request.form.get('amount'))
    except ValueError:
        flash('Invalid amount.', 'danger')
        return redirect(url_for('dashboard'))

    if amount <= 0:
        flash('Amount must be greater than zero.', 'danger')
        return redirect(url_for('dashboard'))

    acc_no = session['acc_no']
    conn = get_db_connection()
    user = conn.execute('SELECT balance FROM account WHERE acc_no = ?', (acc_no,)).fetchone()
    current_balance = user['balance']

    if action == 'deposit':
        new_balance = current_balance + amount
        conn.execute('UPDATE account SET balance = ? WHERE acc_no = ?', (new_balance, acc_no))
        conn.execute('INSERT INTO transactions (acc_no, type, amount) VALUES (?, ?, ?)', (acc_no, 'Deposit', amount))
        conn.commit()
        flash(f'Successfully deposited ₹{amount:.2f}', 'success')

    elif action == 'withdraw':
        if amount > current_balance:
            flash('Insufficient balance!', 'danger')
        else:
            new_balance = current_balance - amount
            conn.execute('UPDATE account SET balance = ? WHERE acc_no = ?', (new_balance, acc_no))
            conn.execute('INSERT INTO transactions (acc_no, type, amount) VALUES (?, ?, ?)', (acc_no, 'Withdrawal', amount))
            conn.commit()
            flash(f'Successfully withdrew ₹{amount:.2f}', 'success')

    conn.close()
    return redirect(url_for('dashboard'))

@app.route('/logout')
def logout():
    session.clear()
    flash('You have been logged out.', 'info')
    return redirect(url_for('login'))

import db_setup

if __name__ == '__main__':
    db_setup.init_db()
    app.run(debug=True, port=5000)
