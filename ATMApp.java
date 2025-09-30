import javax.swing.*;
import java.awt.*;

public class ATMApp extends JFrame {
    private JTextField accField;
    private JTextArea resultArea;
    private int pin;
    private BankService bank;

    private static final Color DARK_BLUE = new Color(10, 20, 70);
    private static final Color BTN_BLUE = new Color(70, 130, 180);
    private static final Color WHITE = Color.WHITE;

    public ATMApp(int pin) {
        this.pin = pin;
        this.bank = new BankService(pin);

        setTitle("JVC BANK ATM");
        setSize(950, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Main panel with background image
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            private final Image bg = new ImageIcon("C:\\Users\\Vinay Bhogal\\OneDrive - Mahavir Education Trust\\Desktop\\Java\\Bank System\\src\\ATM.png").getImage();
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            }
        };
        mainPanel.setOpaque(false);
        setContentPane(mainPanel);

        // Spacer panel to move content down
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(100, 50, 50, 50)); // move down

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(makeAccPanel());
        splitPane.setRightComponent(makeResultPanel());
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.5);
        splitPane.setOpaque(false);

        centerWrapper.add(splitPane, BorderLayout.CENTER);
        mainPanel.add(centerWrapper, BorderLayout.CENTER);

        mainPanel.add(makeBtnPanel(), BorderLayout.SOUTH);

        loadAccInfo();
        setVisible(true);
    }

    private JPanel makeAccPanel() {
        JPanel accPanel = new JPanel();
        accPanel.setBackground(new Color(0, 0, 0, 0));
        accPanel.setLayout(new BoxLayout(accPanel, BoxLayout.Y_AXIS));

        JPanel accNoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        accNoPanel.setOpaque(false);

        JLabel label = new JLabel("Account Number:");
        label.setForeground(Color.WHITE);
        accNoPanel.add(label);

        accField = new JTextField(15);
        accField.setEditable(false);
        accField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        accField.setBackground(new Color(220, 220, 220, 180));
        accNoPanel.add(accField);

        accPanel.add(accNoPanel);
        return accPanel;
    }

    private JScrollPane makeResultPanel() {
        resultArea = new JTextArea(18, 35);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Consolas", Font.PLAIN, 16));
        resultArea.setForeground(WHITE);
        resultArea.setBackground(new Color(0,0,0,0));

        JScrollPane scroll = new JScrollPane(resultArea);
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        return scroll;
    }

    private JPanel makeBtnPanel() {
        JPanel btnPanel = new JPanel(new GridBagLayout());
        btnPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;

        String[] btnNames = {"Check Balance", "Withdraw", "Deposit", "Mini Statement", "Exit"};
        for (int i = 0; i < btnNames.length; i++) {
            JButton btn = makeBtn(btnNames[i]);
            int row = i / 3; // 3 buttons per row
            int col = i % 3;
            gbc.gridx = col;
            gbc.gridy = row;
            btnPanel.add(btn, gbc);

            // Actions
            switch (btnNames[i]) {
                case "Check Balance" -> btn.addActionListener(e -> resultArea.setText(bank.getBalance(accField.getText())));
                case "Withdraw" -> btn.addActionListener(e -> {
                    String input = JOptionPane.showInputDialog(this, "Enter withdrawal amount:");
                    if (input != null) resultArea.setText(bank.withdraw(accField.getText(), Double.parseDouble(input)));
                });
                case "Deposit" -> btn.addActionListener(e -> {
                    String input = JOptionPane.showInputDialog(this, "Enter deposit amount:");
                    if (input != null) resultArea.setText(bank.deposit(accField.getText(), Double.parseDouble(input)));
                });
                case "Mini Statement" -> btn.addActionListener(e -> resultArea.setText(bank.getMiniStatement(accField.getText())));
                case "Exit" -> btn.addActionListener(e -> System.exit(0));
            }
        }

        return btnPanel;
    }

    private JButton makeBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(BTN_BLUE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(200, 50));
        return btn;
    }

    private void loadAccInfo() {
        String accNo = bank.getAccountNo();
        if (accNo != null) {
            accField.setText(accNo);
            resultArea.setText(bank.getBalance(accNo));
        } else {
            resultArea.setText("Account not found for PIN: " + pin);
        }
    }
}
