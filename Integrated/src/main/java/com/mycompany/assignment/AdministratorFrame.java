package com.mycompany.assignment;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class AdministratorFrame extends JFrame {
    private final User currentUser;
    private Runnable onLogout;

    public AdministratorFrame(UserService userService, User currentUser) {
        super("SDAMS - Administrator");
        if (currentUser == null || !Roles.ADMINISTRATOR.equals(currentUser.getRole())) {
            throw new IllegalArgumentException("AdministratorFrame needs a logged-in Administrator");
        }
        this.currentUser = currentUser;

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Staff", new UserManagementPanel(userService, Roles.MANAGED_STAFF, "Staff Member"));
        tabs.addTab("Students", new UserManagementPanel(userService, java.util.Arrays.asList(Roles.STUDENT), "Student"));
        PaymentsPanel payments = new PaymentsPanel(new PaymentRecordReader(userService));
        tabs.addTab("Payments", payments);
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedComponent() == payments) {
                payments.reload();
            }
        });

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bar.setBorder(BorderFactory.createEmptyBorder(0, 8, 4, 8));
        bar.add(new JLabel("Logged in as " + currentUser.getName() + " (" + currentUser.getID() + ")"));
        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this, "Confirm Logout?", "System Message",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                if (onLogout != null) {
                    onLogout.run();
                }
            }
        });
        bar.add(logout);

        getContentPane().add(tabs, BorderLayout.CENTER);
        getContentPane().add(bar, BorderLayout.SOUTH);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(900, 620);
        setLocationRelativeTo(null);
    }

    public void setOnLogout(Runnable onLogout) {
        this.onLogout = onLogout;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UserService service = new UserService();
            User admin = service.findById("AD001");
            new AdministratorFrame(service, admin).setVisible(true);
        });
    }
}
