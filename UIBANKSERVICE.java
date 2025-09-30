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
        setSize(1000, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(240, 242, 247));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(25, 42, 86));
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("JVC Bank Service Portal", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.add(title, BorderLayout.CENTER);

        accInfoLabel = new JLabel("Not logged in", SwingConstants.CENTER);
        accInfoLabel.setForeground(new Color(200, 210, 220));
        accInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        accInfoLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        header.add(accInfoLabel, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(new Color(240, 242, 247));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(mainPanel, BorderLayout.CENTER);

        JPanel leftPanel = createAmountPanel();
        mainPanel.add(leftPanel, BorderLayout.WEST);

        JPanel centerPanel = createOutputPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel actionsPanel = createActionsPanel();
        add(actionsPanel, BorderLayout.SOUTH);

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
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(220, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230), 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel("Transaction Amount");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(60, 70, 80));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel inputContainer = new JPanel(new BorderLayout(8, 12));
        inputContainer.setBackground(Color.WHITE);

        JLabel amountLabel = new JLabel("Amount (Rs)");
        amountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        amountLabel.setForeground(new Color(90, 100, 110));
        inputContainer.add(amountLabel, BorderLayout.NORTH);

        amountField = createStyledTextField();
        inputContainer.add(amountField, BorderLayout.CENTER);

        JButton clearBtn = createStyledButton("Clear All", new Color(220, 60, 60));
        clearBtn.addActionListener(e -> clearFields());
        inputContainer.add(clearBtn, BorderLayout.SOUTH);

        panel.add(inputContainer, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createOutputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230), 1),
                BorderFactory.createEmptyBorder(20, 25, 25, 25)
        ));

        JLabel titleLabel = new JLabel("Transaction Details");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(60, 70, 80));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        outputArea.setBackground(new Color(248, 250, 252));
        outputArea.setForeground(new Color(40, 50, 60));
        outputArea.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(230, 235, 240), 1));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 6, 15, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(240, 242, 247));

        JButton balBtn = createActionButton("Check Balance");
        JButton depBtn = createActionButton("Deposit");
        JButton witBtn = createActionButton("Withdraw");
        JButton noteBtn = createActionButton("Withdraw Notes");
        JButton miniBtn = createActionButton("Mini Statement");
        JButton exitBtn = createActionButton("Exit");
        exitBtn.setBackground(new Color(200, 70, 70));

        balBtn.addActionListener(e -> checkBalance());
        depBtn.addActionListener(e -> depositMoney());
        witBtn.addActionListener(e -> withdrawMoney());
        noteBtn.addActionListener(e -> withdrawWithNotes());
        miniBtn.addActionListener(e -> miniStatement());
        exitBtn.addActionListener(e -> System.exit(0));

        panel.add(balBtn);
        panel.add(depBtn);
        panel.add(witBtn);
        panel.add(noteBtn);
        panel.add(miniBtn);
        panel.add(exitBtn);

        return panel;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(180, 38));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 220), 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        field.setBackground(new Color(250, 252, 255));
        field.setForeground(new Color(40, 50, 60));

        field.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(72, 133, 237), 2),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }

            public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(200, 210, 220), 2),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));
            }
        });

        return field;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(180, 35));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            final Color originalColor = btn.getBackground();

            public void mouseEntered(MouseEvent e) {
                btn.setBackground(originalColor.darker());
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(originalColor);
            }
        });

        return btn;
    }

    private JButton createActionButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(150, 50));
        btn.setBackground(new Color(72, 133, 237));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(58, 110, 200));
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(72, 133, 237));
            }
        });

        return btn;
    }

    private void connectAccount(int pin) {
        try {
            bankService = new BankService(pin);
            accNo = bankService.getAccountNo();
            if (accNo != null) {
                String details = bankService.getAccountInfo();
                accInfoLabel.setText(details);
                outputArea.setText("Welcome to Bank Service Portal!\n\n" + details + "\n\nPlease select a service from the options below.");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid PIN or account not found!", "Authentication Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error connecting to account!", "Connection Error", JOptionPane.ERROR_MESSAGE);
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
            accInfoLabel.setText(details);
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
