import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UIBANKSERVICE extends JFrame {
    private JTextField amountField;
    private JTextArea outputArea;
    private final JLabel accInfoLabel;
    private BankService bankService;
    private String accNo;

    public UIBANKSERVICE(int pinNum) {
        setTitle("JVC Bank Service Portal");
        setSize(1100, 700); // Slightly larger for dashboard feel
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Main Content Background
        getContentPane().setBackground(new Color(244, 247, 254)); // Soft Blue-Grey

        // --- Header Section ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        header.setPreferredSize(new Dimension(0, 70));
        
        // Header Title
        JLabel title = new JLabel("  Dashboard", SwingConstants.LEFT);
        title.setForeground(new Color(33, 43, 54));
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
        header.add(title, BorderLayout.WEST);

        // Header User Info
        accInfoLabel = new JLabel("Not logged in  ", SwingConstants.RIGHT);
        accInfoLabel.setForeground(new Color(100, 110, 120));
        accInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        accInfoLabel.setIcon(new ImageIcon()); // Placeholder for user icon if needed
        accInfoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 25));
        header.add(accInfoLabel, BorderLayout.EAST);
        
        add(header, BorderLayout.NORTH);

        // --- Main Content Section ---
        JPanel mainPanel = new JPanel(new BorderLayout(30, 30));
        mainPanel.setBackground(new Color(244, 247, 254));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        add(mainPanel, BorderLayout.CENTER);

        JPanel leftPanel = createAmountPanel();
        mainPanel.add(leftPanel, BorderLayout.WEST);

        JPanel centerPanel = createOutputPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(createSidebar(), BorderLayout.WEST);

        connectAccount(pinNum);
        setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new UIBANKSERVICE(1234));
    }

    private JPanel createAmountPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        // Clean card look
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        JLabel titleLabel = new JLabel("Transaction Input");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(33, 43, 54));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(4, 1, 0, 20));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(25, 0, 0, 0));

        JLabel lbl = new JLabel("Enter Amount");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(100, 116, 139));
        content.add(lbl);

        amountField = createStyledTextField();
        content.add(amountField);

        JButton clearBtn = createStyledButton("Clear Fields", new Color(239, 68, 68)); // Red-500
        clearBtn.addActionListener(e -> clearFields());
        content.add(clearBtn);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createOutputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));

        JLabel titleLabel = new JLabel("Status & History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(33, 43, 54));
        panel.add(titleLabel, BorderLayout.NORTH);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        outputArea.setBackground(new Color(248, 250, 252));
        outputArea.setForeground(new Color(51, 65, 85));
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(30, 41, 59)); // Slate Dark
        sidebar.setPreferredSize(new Dimension(260, 0)); // Slightly wider
        sidebar.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));

        // Logo Area
        JLabel logo = new JLabel("\uD83C\uDFE6 JVC Bank");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(logo);
        sidebar.add(Box.createRigidArea(new Dimension(0, 60)));

        // Menu Items
        addSidebarButton(sidebar, "Check Balance", "\uD83D\uDCB0", e -> checkBalance());
        addSidebarButton(sidebar, "Deposit", "\uD83D\uDCE5", e -> depositMoney());
        addSidebarButton(sidebar, "Withdraw", "\uD83D\uDCE4", e -> withdrawMoney());
        addSidebarButton(sidebar, "Withdraw Notes", "\uD83D\uDCB5", e -> withdrawWithNotes());
        addSidebarButton(sidebar, "Mini Statement", "\uD83D\uDCC4", e -> miniStatement());
        
        sidebar.add(Box.createVerticalGlue());
        
        // Separator
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(200, 1));
        sep.setForeground(new Color(71, 85, 105));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(sep);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

        addSidebarButton(sidebar, "Logout", "\uD83D\uDEAA", e -> {
            dispose();
            new LoginPage();
        });

        return sidebar;
    }

    private void addSidebarButton(JPanel sidebar, String text, String icon, java.awt.event.ActionListener action) {
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 12));
        btnPanel.setBackground(new Color(30, 41, 59));
        btnPanel.setMaximumSize(new Dimension(260, 55));
        btnPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        iconLabel.setForeground(new Color(148, 163, 184)); // Slate 400

        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        textLabel.setForeground(new Color(148, 163, 184));

        btnPanel.add(iconLabel);
        btnPanel.add(textLabel);

        btnPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnPanel.setBackground(new Color(51, 65, 85)); // Slate 700
                iconLabel.setForeground(Color.WHITE);
                textLabel.setForeground(Color.WHITE);
                // Add left border highlight
                btnPanel.setBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(59, 130, 246))); // Blue accent
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btnPanel.setBackground(new Color(30, 41, 59));
                iconLabel.setForeground(new Color(148, 163, 184));
                textLabel.setForeground(new Color(148, 163, 184));
                btnPanel.setBorder(null);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                action.actionPerformed(null);
            }
        });

        sidebar.add(btnPanel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.BOLD, 16));
        field.setPreferredSize(new Dimension(100, 45));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 220), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        
        field.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(52, 152, 219), 2),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 210, 220), 2),
                        BorderFactory.createEmptyBorder(5, 10, 5, 10)
                ));
            }
        });
        return field;
    }
    
    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void connectAccount(int pin) {
        try {
            bankService = new BankService(pin);
            accNo = bankService.getAccountNo();
            if (accNo != null) {
                String details = bankService.getAccountInfo();
                if (accInfoLabel != null) accInfoLabel.setText("User: " + details.split("\n")[0]); // Simplified for header
                outputArea.setText("Welcome Back!\n\n" + details);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid PIN or account not found!", "Error", JOptionPane.ERROR_MESSAGE);
                dispose();
                new LoginPage();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Connection Error", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void checkBalance() {
        if (!isConnected()) return;
        outputArea.setText(bankService.getBalance(accNo));
    }

    private void depositMoney() {
        if (!isConnected()) return;
        try {
            double amt = Double.parseDouble(amountField.getText());
            if (amt <= 0) {
                outputArea.setText("Please enter a valid positive amount.");
                return;
            }
            outputArea.setText(bankService.deposit(accNo, amt));
            updateAccountInfo();
        } catch (NumberFormatException ex) {
            outputArea.setText("Please enter a valid numeric amount.");
        }
    }

    private void withdrawMoney() {
        if (!isConnected()) return;
        try {
            double amt = Double.parseDouble(amountField.getText());
            if (amt <= 0) {
                outputArea.setText("Please enter a valid positive amount.");
                return;
            }
            outputArea.setText(bankService.withdraw(accNo, amt));
            updateAccountInfo();
        } catch (NumberFormatException ex) {
            outputArea.setText("Please enter a valid numeric amount.");
        }
    }

    private void withdrawWithNotes() {
        if (!isConnected()) return;
        try {
            double amt = Double.parseDouble(amountField.getText());
            if (amt <= 0) {
                outputArea.setText("Please enter a valid positive amount.");
                return;
            }
            String[] noteOptions = {"500", "200", "100", "50", "20", "10"};
            String choice = (String) JOptionPane.showInputDialog(this, "Select denomination:", "Choose Notes", JOptionPane.PLAIN_MESSAGE, null, noteOptions, "500");
            if (choice != null) {
                outputArea.setText(bankService.withdrawWithNotes(accNo, amt, Integer.parseInt(choice)));
                updateAccountInfo();
            }
        } catch (NumberFormatException ex) {
            outputArea.setText("Please enter a valid numeric amount.");
        }
    }

    private void miniStatement() {
        if (!isConnected()) return;
        outputArea.setText(bankService.getMiniStatementLast5(accNo));
    }

    private void updateAccountInfo() {
        if (bankService != null && accNo != null) {
            String details = bankService.getAccountInfo();
            // Update header with just the name or account number if possible
            if (accInfoLabel != null) {
                // Assuming details starts with "Name: ..." or similar
                String firstLine = details.split("\n")[0];
                accInfoLabel.setText("User: " + firstLine);
            }
        }
    }

    private boolean isConnected() {
        if (bankService == null || accNo == null) {
            outputArea.setText("Authentication required. Please restart the application.");
            return false;
        }
        return true;
    }

    private void clearFields() {
        amountField.setText("");
        outputArea.setText("All fields cleared. Ready for new transaction.");
    }
}
