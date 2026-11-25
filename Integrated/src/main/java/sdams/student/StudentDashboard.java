package sdams.student;

import com.mycompany.assignment.Student;

import java.util.LinkedHashMap;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

public class StudentDashboard extends JFrame {
    private Student student;
    private final Runnable onLogout;
    private BookClassesPanel bookClassesPanel;
    private PaymentsPanel paymentsPanel;
    private RatingPanel ratingPanel;

    private JTabbedPane tabbedPane;
    private JButton logoutButton;

    private static final Color HEADER_BG = new Color(13, 48, 96);
    private static final Color PRIMARY_BTN = new Color(11, 94, 181);
    private static final Color CONTENT_BG = new Color(244, 246, 250);
    private static final Color NAV_BG = new Color(222, 229, 239);
    private static final Color TABLE_HEADER_BG = new Color(228, 236, 247);
    private static final Color WHITE_BG = new Color(255, 255, 255);
    private static final Color ACTIVE_STATUS_BG = new Color(217, 245, 229);
    private static final Color ACTIVE_STATUS_TEXT = new Color(8, 125, 50);
    private static final Color DISABLED_BTN = new Color(232, 236, 243);
    private static final Color MAIN_TEXT = new Color(16, 24, 40);

    public StudentDashboard(Student student, Runnable onLogout) {
        this(student, createPrices(), createInstructors(), onLogout);
    }

    private static Map<String, BigDecimal> createPrices() {

        Map<String, BigDecimal> prices = new LinkedHashMap<>();

        prices.put("C001", new BigDecimal("100.00"));
        prices.put("C002", new BigDecimal("120.00"));
        prices.put("C003", new BigDecimal("90.00"));

        return prices;
    }

    // Temporary instructor assignments
    private static Map<String, String> createInstructors() {

        Map<String, String> instructors = new LinkedHashMap<>();

        instructors.put("C001", "IN001");
        instructors.put("C002", "IN002");
        instructors.put("C003", "IN003");

        return instructors;
    }

    public StudentDashboard(Student student, Map<String, BigDecimal> classPrices, Map<String, String> classInstructors, Runnable onLogout) {
        this.student = student;
        this.onLogout = onLogout;

        //Main JFrame
        setTitle("Main Dashboard");
        setSize(1100, 730);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(CONTENT_BG);


        //Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(CONTENT_BG);

        //The Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(13, 48,96));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(18, 25, 18, 25));

        //Student Information
        JPanel studentPanel = new JPanel(new GridLayout(2,1,0,5));
        studentPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Welcome " + student.getName() + "!");
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 22));
        welcomeLabel.setForeground(Color.WHITE);

        JLabel studentIDLabel = new JLabel("Student ID: " + student.getID() + " | " + " Enrollment Status: " + student.getEnrollmentStatus());
        studentIDLabel.setFont(new Font("Arial", Font.BOLD, 18));
        studentIDLabel.setForeground(Color.WHITE);

        studentPanel.add(welcomeLabel);
        studentPanel.add(studentIDLabel);
        headerPanel.setBackground(HEADER_BG);
        studentPanel.setBackground(HEADER_BG);

        headerPanel.add(studentPanel, BorderLayout.WEST);

        //Logout Button
        logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 18));
        logoutButton.setForeground(MAIN_TEXT);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setOpaque(true);
        logoutButton.setPreferredSize(new Dimension(110, 40));

        headerPanel.add(logoutButton, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        //Create the existing service objects
        ClassRegistrationService registrationService = new ClassRegistrationService();
        PaymentService paymentService = new PaymentService();
        InstructorRatingService ratingService = new InstructorRatingService();


        bookClassesPanel = new BookClassesPanel(student, registrationService, classPrices, this);
        paymentsPanel = new PaymentsPanel(student, registrationService, paymentService, classPrices, this);
        ratingPanel = new RatingPanel(student, registrationService, ratingService, classInstructors, this);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 16));
        tabbedPane.setBackground(NAV_BG);
        tabbedPane.setForeground(MAIN_TEXT);

        tabbedPane.addTab("Book Classes", bookClassesPanel);
        tabbedPane.addTab("Payments", paymentsPanel);
        tabbedPane.addTab("Ratings", ratingPanel);

        // Apply colors after adding the tabs
        updateTabColors();

        // Update colors when switching tabs
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateTabColors();
            }
        });

        mainPanel.add(tabbedPane, BorderLayout.CENTER);


        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        //Logout button
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });


        add(mainPanel);

        //Load data when the dashboard opens
        refreshAll();

    }

    public void refreshAll() {
        try {
            bookClassesPanel.reloadTable();
            paymentsPanel.reloadTable();
            ratingPanel.reloadTable();
        }

        catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
        }

        catch (IllegalArgumentException | IllegalStateException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Invalid data", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void logout() {
        int confirmation = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirmation == JOptionPane.YES_OPTION) {
            onLogout.run();
            dispose();
        }


    }


    private void updateTabColors() {

        for (int i = 0; i < tabbedPane.getTabCount(); i++) {

            if (i == tabbedPane.getSelectedIndex()) {

                // Active tab
                tabbedPane.setBackgroundAt(i, PRIMARY_BTN);
                tabbedPane.setForegroundAt(i, Color.WHITE);

            } else {

                // Inactive tabs
                tabbedPane.setBackgroundAt(i, NAV_BG);
                tabbedPane.setForegroundAt(i, MAIN_TEXT);
            }
        }
    }

}
