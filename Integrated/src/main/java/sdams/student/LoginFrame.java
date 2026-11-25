package sdams.student;

import com.mycompany.assignment.User;
import sdams.Main;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
public class LoginFrame extends JFrame {

    private final Main app;

    private JTextField idField;
    private JPasswordField passwordField;

    private JButton loginButton;
    private JButton clearButton;
    private JCheckBox showPasswordCheckBox;

    public LoginFrame(Main app) {

        this.app = app;

        setTitle("User Login");
        setSize(500, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(244, 246, 250));
        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(30, 45, 30, 45)
        );

        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        headerPanel.setBackground(new Color(13, 48, 96));

        JLabel titleLabel = new JLabel(
                "USER LOGIN", SwingConstants.CENTER
        );
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel(
                "Sign in to access your dashboard",
                SwingConstants.CENTER
        );
        subtitleLabel.setFont(new Font("Arial", Font.BOLD, 15));
        subtitleLabel.setForeground(Color.WHITE);

        headerPanel.add(titleLabel);
        headerPanel.add(subtitleLabel);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // The main login form
        JPanel formPanel = new JPanel(new GridLayout(6, 1, 5, 7));
        formPanel.setOpaque(false);
        formPanel.setBorder(
                BorderFactory.createEmptyBorder(25, 0, 15, 0)
        );

        JLabel idLabel = new JLabel("User ID");
        idLabel.setFont(new Font("Arial", Font.BOLD, 14));
        idLabel.setForeground(new Color(16, 24, 40));

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        passwordLabel.setForeground(new Color(16, 24, 40));

        idField = new JTextField();
        idField.setFont(new Font("Arial", Font.PLAIN, 15));
        idField.setBackground(Color.WHITE);
        idField.setForeground(new Color(16, 24, 40));

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 15));
        passwordField.setBackground(Color.WHITE);
        passwordField.setForeground(new Color(16, 24, 40));

        // Save the original password masking character
        final char defaultEchoChar = passwordField.getEchoChar();

        showPasswordCheckBox = new JCheckBox("Show Password");
        showPasswordCheckBox.setOpaque(false);
        showPasswordCheckBox.setForeground(new Color(16, 24, 40));

        formPanel.add(idLabel);
        formPanel.add(idField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(showPasswordCheckBox);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(
                BorderFactory.createEmptyBorder(10, 0, 5, 0)
        );

        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 15));
        loginButton.setBackground(new Color(11, 94, 181));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);

        clearButton = new JButton("Clear");
        clearButton.setFont(new Font("Arial", Font.BOLD, 15));
        clearButton.setBackground(new Color(232, 236, 243));
        clearButton.setForeground(new Color(16, 24, 40));
        clearButton.setFocusPainted(false);

        buttonPanel.add(loginButton);
        buttonPanel.add(clearButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);


        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });


        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                idField.setText("");
                passwordField.setText("");
                idField.requestFocusInWindow();
            }
        });


        showPasswordCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (showPasswordCheckBox.isSelected()) {
                    passwordField.setEchoChar((char) 0);
                } else {
                    passwordField.setEchoChar(defaultEchoChar);
                }
            }
        });

        getRootPane().setDefaultButton(loginButton);
    }


    private void login() {

        String userID = idField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (userID.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter your user ID and password.",
                    "Missing Credentials",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            User user = app.getAuthenticationService().login(userID, password);
            passwordField.setText("");
            app.openFrameFor(this, user);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            passwordField.setText("");
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Login", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // the default look and feel is fine
            }
            new Main().start();
        });
    }
}
