import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class LoginPage extends JFrame {
    private JPasswordField pinField;
    private JButton loginButton, exitButton;
    private Image bgImage; // store background once

    private static final String DB_URL = "jdbc:mysql://localhost:3306/vinay";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "vinay";

    public LoginPage() {
        setTitle("Login");
        setSize(500, 800); // Adjust for mobile ratio
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Load and scale background once
        ImageIcon bg = new ImageIcon("C:\\Users\\Vinay Bhogal\\OneDrive - Mahavir Education Trust\\Desktop\\Java\\Bank System\\src\\LoginBGs.png");
        bgImage = bg.getImage().getScaledInstance(500, 800, Image.SCALE_SMOOTH);

        // Background panel
        JPanel bgPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        bgPanel.setLayout(new GridBagLayout());
        add(bgPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;

        // PIN field
        pinField = new JPasswordField(15);
        pinField.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridy = 0;
        bgPanel.add(pinField, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        loginButton = new JButton("LOGIN");
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setBackground(new Color(30, 144, 255));
        loginButton.setForeground(Color.WHITE);
        buttonPanel.add(loginButton);

        exitButton = new JButton("EXIT");
        exitButton.setFont(new Font("Arial", Font.BOLD, 16));
        exitButton.setBackground(new Color(220, 20, 60));
        exitButton.setForeground(Color.WHITE);
        buttonPanel.add(exitButton);

        gbc.gridy = 1;
        bgPanel.add(buttonPanel, gbc);

        // Button actions
        loginButton.addActionListener(e -> loginAction());
        exitButton.addActionListener(e -> System.exit(0));

        setVisible(true);
    }

    private void loginAction() {
        String pinText = new String(pinField.getPassword()).trim();
        int pinInt;
        try {
            pinInt = Integer.parseInt(pinText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "PIN must be numeric!");
            return;
        }

        if (checkLogin(pinInt)) {
            JOptionPane.showMessageDialog(this, "Login Successful!");
            new ATMApp(pinInt);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid PIN!");
        }
    }

    private boolean checkLogin(int pin) {
        boolean result = false;
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM account WHERE pin = ?")) {
            stmt.setInt(1, pin);
            try (ResultSet rs = stmt.executeQuery()) {
                result = rs.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
        return result;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginPage::new);
    }
}
