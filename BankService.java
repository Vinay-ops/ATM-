import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BankService {
    private static final double DAILY_LIMIT = 25000.0;
    private final int pin;
    private Connection con;

    public BankService(int pin) {
        this.pin = pin;
        connect();
    }

    private void connect() {
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/vinay", "root", "vinay");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getAccountNo() {
        try (PreparedStatement stmt = con.prepareStatement("SELECT acc_no FROM account WHERE pin = ? LIMIT 1")) {
            stmt.setInt(1, pin);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("acc_no");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getAccountInfo() {
        try (PreparedStatement stmt = con.prepareStatement("SELECT name, acc_no, balance FROM account WHERE pin = ?")) {
            stmt.setInt(1, pin);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return "Account Holder: " + rs.getString("name") + " | Acc No: " + rs.getString("acc_no") + " | Balance: Rs" + rs.getDouble("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getBalance(String accNo) {
        try (PreparedStatement stmt = con.prepareStatement("SELECT name, balance FROM account WHERE acc_no = ? AND pin = ?")) {
            stmt.setString(1, accNo);
            stmt.setInt(2, pin);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return "Account Holder: " + rs.getString("name") + "\nBalance: Rs" + rs.getDouble("balance");
            }
        } catch (SQLException e) {
            return "Error fetching balance: " + e.getMessage();
        }
        return "Account not found!";
    }

    public String deposit(String accNo, double amount) {
        try {
            double current = fetchBalance(accNo);
            double newBal = current + amount;
            updateBalance(accNo, newBal);
            saveTxn(accNo, "deposit", amount);
            return "Deposit Successful\n\nAmount: Rs" + amount + "\nNew Balance: Rs" + newBal;
        } catch (Exception e) {
            return "Error in deposit: " + e.getMessage();
        }
    }

    public String withdraw(String accNo, double amount) {
        try {
            double current = fetchBalance(accNo);
            if (!checkLimit(accNo, amount)) return "Daily withdrawal limit exceeded.\nLimit: Rs" + DAILY_LIMIT;
            if (current < amount) return "Insufficient balance.\nCurrent Balance: Rs" + current;
            double newBal = current - amount;
            updateBalance(accNo, newBal);
            saveTxn(accNo, "withdraw", amount);
            return "Withdrawal Successful\n\nAmount: Rs" + amount + "\nNew Balance: Rs" + newBal;
        } catch (Exception e) {
            return "Error in withdrawal: " + e.getMessage();
        }
    }

    public String withdrawWithNotes(String accNo, double amount, int noteType) {
        try {
            if (amount % noteType != 0) {
                return "Amount must be a multiple of Rs" + noteType + "\n\nPlease enter a valid amount.";
            }

            double current = fetchBalance(accNo);
            if (current < amount) {
                return "Insufficient balance.\n\nCurrent Balance: Rs" + current + "\nRequested: Rs" + amount;
            }

            if (!checkLimit(accNo, amount)) {
                return "Daily withdrawal limit exceeded.\n\nDaily Limit: Rs" + DAILY_LIMIT;
            }

            int noteCount = (int) (amount / noteType);

            if (!areNotesAvailable(noteType, noteCount)) {
                int availableNotes = getAvailableNotesCount(noteType);
                int maxAmount = availableNotes * noteType;
                return "Sorry! Rs" + noteType + " notes are not available in required quantity.\n\n" +
                        "Requested: " + noteCount + " notes (Rs" + amount + ")\n" +
                        "Available: " + availableNotes + " notes (Rs" + maxAmount + ")\n\n" +
                        "Please try another denomination or reduce the amount.";
            }

            double newBal = current - amount;
            updateBalance(accNo, newBal);
            saveTxn(accNo, "withdraw", amount);
            updateNotesInventory(noteType, noteCount);

            int remainingNotes = getAvailableNotesCount(noteType);

            return "Withdrawal Successful\n\n" +
                    "Amount: Rs" + amount + "\n" +
                    "Denomination: Rs" + noteType + " x " + noteCount + "\n" +
                    "New Balance: Rs" + newBal + "\n\n";

        } catch (Exception e) {
            return "Error in withdrawal with notes: " + e.getMessage();
        }
    }

    public String getMiniStatementLast5(String accNo) {
        StringBuilder sb = new StringBuilder("Last 5 Transactions:\n\n");
        List<Transaction> last5 = getTransactions(accNo);
        if (last5.isEmpty()) {
            sb.append("No transactions found.");
        } else {
            for (int i = 0; i < Math.min(5, last5.size()); i++) {
                Transaction t = last5.get(i);
                sb.append((i + 1)).append(". ").append(t.date).append("\n");
                sb.append("   ").append(t.type.toUpperCase()).append(": Rs").append(t.amount).append("\n\n");
            }
        }
        return sb.toString();
    }

    private boolean areNotesAvailable(int noteType, int requestedCount) throws SQLException {
        String columnName = "note_" + noteType;
        PreparedStatement stmt = con.prepareStatement("SELECT " + columnName + " FROM notes_inventory WHERE id = 1");
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            int available = rs.getInt(columnName);
            return available >= requestedCount;
        }
        return false;
    }

    private int getAvailableNotesCount(int noteType) {
        try {
            String columnName = "note_" + noteType;
            PreparedStatement stmt = con.prepareStatement("SELECT " + columnName + " FROM notes_inventory WHERE id = 1");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(columnName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void updateNotesInventory(int noteType, int usedCount) throws SQLException {
        String columnName = "note_" + noteType;
        PreparedStatement stmt = con.prepareStatement(
                "UPDATE notes_inventory SET " + columnName + " = " + columnName + " - ? WHERE id = 1"
        );
        stmt.setInt(1, usedCount);
        int rowsUpdated = stmt.executeUpdate();

        if (rowsUpdated == 0) {
            throw new SQLException("Failed to update notes inventory");
        }
    }

    private double fetchBalance(String accNo) throws SQLException {
        PreparedStatement stmt = con.prepareStatement("SELECT balance FROM account WHERE acc_no = ? AND pin = ?");
        stmt.setString(1, accNo);
        stmt.setInt(2, pin);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) return rs.getDouble("balance");
        throw new SQLException("Account not found.");
    }

    private void updateBalance(String accNo, double newBal) throws SQLException {
        PreparedStatement stmt = con.prepareStatement("UPDATE account SET balance = ? WHERE acc_no = ? AND pin = ?");
        stmt.setDouble(1, newBal);
        stmt.setString(2, accNo);
        stmt.setInt(3, pin);
        stmt.executeUpdate();
    }

    private boolean checkLimit(String accNo, double amt) throws SQLException {
        LocalDate today = LocalDate.now();
        PreparedStatement stmt = con.prepareStatement("SELECT SUM(amount) FROM transactions WHERE acc_no = ? AND type = 'withdraw' AND DATE(date) = ?");
        stmt.setString(1, accNo);
        stmt.setString(2, today.toString());
        ResultSet rs = stmt.executeQuery();
        double total = 0;
        if (rs.next()) total = rs.getDouble(1);
        return (total + amt) <= DAILY_LIMIT;
    }

    private void saveTxn(String accNo, String type, double amt) throws SQLException {
        PreparedStatement stmt = con.prepareStatement("INSERT INTO transactions (acc_no, type, amount, date) VALUES (?, ?, ?, NOW())");
        stmt.setString(1, accNo);
        stmt.setString(2, type);
        stmt.setDouble(3, amt);
        stmt.executeUpdate();
    }

    private List<Transaction> getTransactions(String accNo) {
        List<Transaction> txns = new ArrayList<>();
        try (PreparedStatement stmt = con.prepareStatement("SELECT type, amount, date FROM transactions WHERE acc_no = ? ORDER BY date DESC LIMIT 5")) {
            stmt.setString(1, accNo);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Transaction t = new Transaction();
                t.type = rs.getString("type");
                t.amount = rs.getDouble("amount");
                t.date = rs.getString("date");
                txns.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return txns;
    }

    private static class Transaction {
        String type;
        String date;
        double amount;
    }
}
