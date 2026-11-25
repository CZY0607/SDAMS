
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LoginFrame extends JFrame {

    // Store all users, regardless of their roles
    private List<User> users;

    private JTextField idField;
    private JPasswordField passwordField;

    private JButton loginButton;
    private JButton clearButton;
    private JCheckBox showPasswordCheckBox;

    public LoginFrame(List<User> users) {

        this.users = users;

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

        String userID = idField.getText().trim().toUpperCase();
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

        User loggedInUser = null;


        for (User user : users) {

            if (user.getID().equals(userID)
                    && user.verifyPassword(password)) {

                loggedInUser = user;
                break;
            }
        }

        if (loggedInUser == null) {
            passwordField.setText("");

            JOptionPane.showMessageDialog(
                    this,
                    "Incorrect user ID or password.",
                    "Invalid Credentials",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }


        openDashboard(loggedInUser);
    }

    private void openDashboard(User user) {

        String role = user.getRole();

        if (role.equals("Student")) {

            Student student = (Student) user;

            if (!student.getEnrollmentStatus().equals("Active")) {
                JOptionPane.showMessageDialog(
                        this,
                        "Your student account is inactive.",
                        "Inactive Account",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            StudentDashboard dashboard = new StudentDashboard(
                    student,
                    createPrices(),
                    createInstructors(),
                    users
            );

            dashboard.setVisible(true);
            dispose();

        } else if (role.equals("Manager")) {

            // Connect the team's Manager dashboard here.

            JOptionPane.showMessageDialog(
                    this,
                    "Manager login successful, but the Manager dashboard " +
                            "has not been connected yet.",
                    "Dashboard Unavailable",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } else if (role.equals("Instructor")) {

            // Connect the team's Instructor dashboard here.

            JOptionPane.showMessageDialog(
                    this,
                    "Instructor login successful, but the Instructor dashboard " +
                            "has not been connected yet.",
                    "Dashboard Unavailable",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } else if (role.equals("Attendance Officer")) {

            //Connect the Attendance Officer dashboard here.

            JOptionPane.showMessageDialog(
                    this,
                    "Attendance Officer login successful, but the dashboard " +
                            "has not been connected yet.",
                    "Dashboard Unavailable",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "No dashboard is configured for role: " + role,
                    "Unsupported Role",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    // Temporary class prices
    private Map<String, BigDecimal> createPrices() {

        Map<String, BigDecimal> prices = new LinkedHashMap<>();

        prices.put("C001", new BigDecimal("100.00"));
        prices.put("C002", new BigDecimal("120.00"));
        prices.put("C003", new BigDecimal("90.00"));

        return prices;
    }

    // Temporary instructor assignments
    private Map<String, String> createInstructors() {

        Map<String, String> instructors = new LinkedHashMap<>();

        instructors.put("C001", "I001");
        instructors.put("C002", "I002");
        instructors.put("C003", "I003");

        return instructors;
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {

                List<User> users = new ArrayList<>();

                // Demonstration student account
                users.add(new Student(
                        "S001",
                        "John Doe",
                        "123456789",
                        "student123",
                        "Active"
                ));

                // Add Manager, Instructor and other User objects
                // when those classes are provided by your team.

                LoginFrame loginFrame = new LoginFrame(users);
                loginFrame.setVisible(true);
            }
        });
    }
}
