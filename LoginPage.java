import javax.swing.*;
import java.awt.*;

public class LoginPage extends JFrame {
    private JPasswordField pinField;
    private JButton loginBtn, exitBtn;
    private Image bgImg;

    public LoginPage() {
        setTitle("Bank ATM Login");
        setSize(400, 700);  // Mobile size
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Load background image
        ImageIcon bg = new ImageIcon("C:\\Users\\Vinay Bhogal\\OneDrive - Mahavir Education Trust\\Desktop\\Java\\Bank System\\src\\LoginBGs.png");
        bgImg = bg.getImage().getScaledInstance(400, 700, Image.SCALE_SMOOTH);

        // Main panel with background
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bgImg, 0, 0, getWidth(), getHeight(), this);
            }
        };
        panel.setLayout(new GridBagLayout());
        add(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.gridx = 0;
        gbc.gridy = 0;

        // PIN field only, no label
        pinField = new JPasswordField(15);
        pinField.setFont(new Font("Arial", Font.PLAIN, 18));
        panel.add(pinField, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.setOpaque(false);
        loginBtn = new JButton("Login");
        exitBtn = new JButton("Exit");
        btnPanel.add(loginBtn);
        btnPanel.add(exitBtn);

        gbc.gridy = 1;
        panel.add(btnPanel, gbc);

        // Button actions
        loginBtn.addActionListener(e -> doLogin());
        exitBtn.addActionListener(e -> System.exit(0));

        setVisible(true);
    }

    private void doLogin() {
        String pinText = new String(pinField.getPassword());
        if (pinText.isEmpty()) return;

        int pinNum;
        try {
            pinNum = Integer.parseInt(pinText);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "PIN must be numeric");
            return;
        }

        BankService bank = new BankService(pinNum);
        if (bank.getAccountNo() != null) {
            new ATMApp(pinNum); // open ATM window
            dispose(); // close login
        } else {
            JOptionPane.showMessageDialog(this, "Invalid PIN");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginPage::new);
    }
}
