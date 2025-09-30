import java.sql.*;
import java.time.LocalDate;

public class BankService {
    private Connection con;
    private final int pin;
    private static final double DAILY_LIMIT = 25000.0;

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
        try (PreparedStatement stmt = con.prepareStatement("SELECT acc_no FROM account WHERE pin = ?")) {
            stmt.setInt(1, pin);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("acc_no");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getBalance(String accNo) {
        try (PreparedStatement stmt = con.prepareStatement("SELECT name, balance FROM account WHERE acc_no = ? AND pin = ?")) {
            stmt.setInt(1, Integer.parseInt(accNo));
            stmt.setInt(2, pin);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return "Account Holder: " + rs.getString("name") +
                        "\nBalance: ₹" + rs.getDouble("balance");
            }
        } catch (SQLException e) {
            return "Error fetching balance: " + e.getMessage();
        }
        return "Account not found!";
    }

    public String deposit(String accNo, double amount) {
        try {
            double current = fetchBalance(accNo);
            double newBal = current + amount;   // math in Java

            updateBalance(accNo, newBal);
            saveTxn(accNo, "deposit", amount);

            return "Deposit successful! New Balance: ₹" + newBal;
        } catch (Exception e) {
            return "Error in deposit: " + e.getMessage();
        }
    }

    public String withdraw(String accNo, double amount) {
        try {
            double current = fetchBalance(accNo);

            if (!checkLimit(accNo, amount))
                return "Daily withdrawal limit exceeded.";

            if (current < amount)
                return "Insufficient balance! Current: ₹" + current;

            double newBal = current - amount;   // math in Java
            updateBalance(accNo, newBal);
            saveTxn(accNo, "withdraw", amount);

            return "Withdraw successful! New Balance: ₹" + newBal;
        } catch (Exception e) {
            return "Error in withdrawal: " + e.getMessage();
        }
    }

    public String getMiniStatement(String accNo) {
        StringBuilder sb = new StringBuilder("Last 5 Transactions:\n\n");
        try (PreparedStatement stmt = con.prepareStatement(
                "SELECT type, amount, date FROM transactions WHERE acc_no = ? ORDER BY date DESC LIMIT 5")) {
            stmt.setInt(1, Integer.parseInt(accNo));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                sb.append(rs.getString("date")).append(" - ")
                        .append(rs.getString("type").toUpperCase())
                        .append(" ₹").append(rs.getDouble("amount")).append("\n");
            }
        } catch (SQLException e) {
            return "Error fetching statement: " + e.getMessage();
        }
        return sb.toString();
    }

    private double fetchBalance(String accNo) throws SQLException {
        PreparedStatement stmt = con.prepareStatement("SELECT balance FROM account WHERE acc_no = ? AND pin = ?");
        stmt.setInt(1, Integer.parseInt(accNo));
        stmt.setInt(2, pin);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) return rs.getDouble("balance");
        throw new SQLException("Account not found.");
    }

    private void updateBalance(String accNo, double newBal) throws SQLException {
        PreparedStatement stmt = con.prepareStatement("UPDATE account SET balance = ? WHERE acc_no = ? AND pin = ?");
        stmt.setDouble(1, newBal);
        stmt.setInt(2, Integer.parseInt(accNo));
        stmt.setInt(3, pin);
        stmt.executeUpdate();
    }

    private boolean checkLimit(String accNo, double amt) throws SQLException {
        LocalDate today = LocalDate.now();
        PreparedStatement stmt = con.prepareStatement(
                "SELECT SUM(amount) FROM transactions WHERE acc_no = ? AND type = 'withdraw' AND DATE(date) = ?");
        stmt.setInt(1, Integer.parseInt(accNo));
        stmt.setString(2, today.toString());
        ResultSet rs = stmt.executeQuery();
        double total = 0;
        if (rs.next()) total = rs.getDouble(1);
        return (total + amt) <= DAILY_LIMIT;
    }

    private void saveTxn(String accNo, String type, double amt) throws SQLException {
        PreparedStatement stmt = con.prepareStatement(
                "INSERT INTO transactions (acc_no, type, amount, date) VALUES (?, ?, ?, NOW())");
        stmt.setInt(1, Integer.parseInt(accNo));
        stmt.setString(2, type);
        stmt.setDouble(3, amt);
        stmt.executeUpdate();
    }
}
