import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ATMApp extends JFrame {
    private JTextField accField;
    private JTextArea resultArea;
    private int loginPin;
    private Connection con;

    public ATMApp(int pin) {
        loginPin = pin;

        // Database connection
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/vinay", "root", "vinay");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage());
            return;
        }

        setTitle("ATM Simulator");
        setSize(700, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));

        // Header
        JLabel header = new JLabel("💳 ATM Simulator", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 30));
        header.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(header, BorderLayout.NORTH);

        // Account info panel
        JPanel accountPanel = new JPanel();
        accountPanel.setLayout(new BoxLayout(accountPanel, BoxLayout.Y_AXIS));
        accountPanel.setBorder(BorderFactory.createTitledBorder("Account Info"));
        accountPanel.setBackground(new Color(230, 230, 250)); // light lavender

        JPanel accNoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        accNoPanel.add(new JLabel("Account No:"));
        accField = new JTextField(20);
        accField.setEditable(false);
        accNoPanel.add(accField);

        accountPanel.add(accNoPanel);
        add(accountPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 2, 20, 20));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Actions"));

        JButton balanceBtn = new JButton("Check Balance");
        JButton withdrawBtn = new JButton("Withdraw");
        JButton depositBtn = new JButton("Deposit");
        JButton exitBtn = new JButton("Exit");

        balanceBtn.setFont(new Font("Arial", Font.BOLD, 18));
        withdrawBtn.setFont(new Font("Arial", Font.BOLD, 18));
        depositBtn.setFont(new Font("Arial", Font.BOLD, 18));
        exitBtn.setFont(new Font("Arial", Font.BOLD, 18));

        buttonPanel.add(balanceBtn);
        buttonPanel.add(withdrawBtn);
        buttonPanel.add(depositBtn);
        buttonPanel.add(exitBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        // Result area
        resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 16));
        resultArea.setBorder(BorderFactory.createTitledBorder("Result"));
        JScrollPane scroll = new JScrollPane(resultArea);
        add(scroll, BorderLayout.EAST);

        // Load account info automatically
        loadAccountInfo();

        // Button actions
        balanceBtn.addActionListener(e -> showBalance());
        withdrawBtn.addActionListener(e -> performTransaction("withdraw"));
        depositBtn.addActionListener(e -> performTransaction("deposit"));
        exitBtn.addActionListener(e -> {
            try { if (con != null) con.close(); } catch (Exception ignored) {}
            System.exit(0);
        });

        setVisible(true);
    }

    private void loadAccountInfo() {
        try {
            PreparedStatement stmt = con.prepareStatement("SELECT acc_no FROM account WHERE pin = ?");
            stmt.setInt(1, loginPin);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                accField.setText(String.valueOf(rs.getInt("acc_no")));
                showBalance(); // automatically display name and balance
            } else {
                resultArea.setText("❌ Account not found for PIN " + loginPin);
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            resultArea.setText("⚠ Error: " + ex.getMessage());
        }
    }

    private void showBalance() {
        try {
            int acc_no = Integer.parseInt(accField.getText());
            PreparedStatement stmt = con.prepareStatement("SELECT name, balance FROM account WHERE acc_no = ? AND pin = ?");
            stmt.setInt(1, acc_no);
            stmt.setInt(2, loginPin);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                resultArea.setText("👤 Account Holder: " + rs.getString("name") + "\n");
                resultArea.append("💰 Current Balance: " + rs.getDouble("balance"));
            } else {
                resultArea.setText("❌ Wrong PIN or account not found!");
            }
            rs.close();
            stmt.close();
        } catch (SQLException ex) {
            resultArea.setText("⚠ Error: " + ex.getMessage());
        }
    }

    private void performTransaction(String type) {
        try {
            int acc_no = Integer.parseInt(accField.getText());
            String input = JOptionPane.showInputDialog(this, "Enter amount:");
            if (input == null || input.trim().isEmpty()) return;
            double amount = Double.parseDouble(input.trim());

            String query;
            if (type.equals("withdraw")) {
                query = "UPDATE account SET balance = balance - ? WHERE acc_no = ? AND pin = ? AND balance >= ?";
            } else {
                query = "UPDATE account SET balance = balance + ? WHERE acc_no = ? AND pin = ?";
            }

            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setDouble(1, amount);
            stmt.setInt(2, acc_no);
            stmt.setInt(3, loginPin);
            if (type.equals("withdraw")) stmt.setDouble(4, amount);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                resultArea.setText("✅ " + (type.equals("withdraw") ? "Withdrawal" : "Deposit") + " successful!\n");
                showBalance();
            } else {
                resultArea.setText("❌ Transaction failed! (Wrong PIN or insufficient balance)");
            }

            stmt.close();
        } catch (Exception ex) {
            resultArea.setText("⚠ Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ATMApp(2006)); // Example PIN, replace with actual
    }
}
