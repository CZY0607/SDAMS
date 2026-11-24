package sdams;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Plain-Java checks (no JUnit needed).  Run with:  java -cp out sdams.ManagerTests
 * Uses a temporary data folder, so your real data/ is never touched.
 * If a screen is available it also drives the real Swing windows and saves screenshots to build/shots.
 */
public class ManagerTests {
    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("  ok    " + name);
        } else {
            failed++;
            System.out.println("  FAIL  " + name);
        }
    }

    private static boolean throwsExpected(Class<? extends RuntimeException> type, Runnable r, String messagePart) {
        try {
            r.run();
            return false;
        } catch (RuntimeException e) {
            return type.isInstance(e) && e.getMessage().contains(messagePart);
        }
    }

    public static void main(String[] args) throws Exception {
        Path temp = Files.createTempDirectory("sdams-test-");
        System.setProperty("sdams.data", temp.toString());

        new FileStore("users.txt").writeAll(Arrays.asList(
                new String[] {"M001", "Mei Ling Tan", "012-3456789", "manager123", "MANAGER", ""},
                new String[] {"AD001", "Farah Hassan", "013-2223344", "admin123", "ADMINISTRATOR", ""},
                new String[] {"IN001", "Rajesh | Kumar", "017-1112233", "instr123", "INSTRUCTOR", "Swimming;Diving"},
                new String[] {"ST001", "Amir Zulkifli", "012-9990001", "stud123", "STUDENT", "ACTIVE"}));
        new FileStore("payments.txt").writeAll(Arrays.asList(
                new String[] {"PAY1", "REG1", "50.10", "2026-03-05", "CARD", "PAID"},
                new String[] {"PAY2", "REG2", "20.20", "2026-03-28", "CASH", "PAID"},
                new String[] {"PAY3", "REG3", "99.00", "2026-03-29", "CASH", "PENDING"},
                new String[] {"PAY4", "REG4", "10.00", "2025-03-01", "CASH", "PAID"},
                new String[] {"PAY5", "REG5", "abc", "2026-03-01", "CASH", "PAID"}));
        new FileStore("registrations.txt").writeAll(Arrays.asList(
                new String[] {"REG1", "ST001", "CL1", "2026-03-04", "CONFIRMED"},
                new String[] {"REG2", "ST001", "CL1", "2026-03-27", "CANCELLED"},
                new String[] {"REG3", "ST001", "CL2", "2026-04-01", "CONFIRMED"}));

        System.out.println("FileStore");
        FileStore rt = new FileStore("roundtrip.txt");
        String[] tricky = {"a|b", "c\\d", "line1\nline2", ""};
        rt.writeAll(Arrays.<String[]>asList(tricky));
        check("pipes, backslashes and newlines survive a save and load",
                Arrays.equals(rt.readAll().get(0), tricky));

        System.out.println("User and Manager");
        check("Manager reports role MANAGER", new Manager("M9", "Ann Lee", "0123456789", "secret1").getRole().equals("MANAGER"));
        check("short name rejected", throwsExpected(IllegalArgumentException.class, () -> new Manager("M9", "A", "0123456789", "secret1"), "Name"));
        check("bad contact rejected", throwsExpected(IllegalArgumentException.class, () -> new Manager("M9", "Ann Lee", "abc", "secret1"), "Contact"));
        check("short password rejected", throwsExpected(IllegalArgumentException.class, () -> new Manager("M9", "Ann Lee", "0123456789", "123"), "Password"));
        check("bad ID rejected", throwsExpected(IllegalArgumentException.class, () -> new Manager("a b", "Ann Lee", "0123456789", "secret1"), "User ID"));

        System.out.println("AuthenticationService");
        UserService us = new UserService();
        AuthenticationService auth = new AuthenticationService(us);
        check("wrong password refused", throwsExpected(IllegalArgumentException.class, () -> auth.login("M001", "wrong"), "Incorrect"));
        check("unknown ID gives the same message", throwsExpected(IllegalArgumentException.class, () -> auth.login("NOPE", "x"), "Incorrect"));
        User m = auth.login("m001", "manager123");
        check("ID is case-insensitive and currentUser is set", auth.getCurrentUser() == m);
        check("Manager may open REVENUE_REPORT", auth.hasAccess(m, "REVENUE_REPORT"));
        check("Manager may not open MAKE_PAYMENT", !auth.hasAccess(m, "MAKE_PAYMENT"));
        auth.logout();
        check("logout clears currentUser", auth.getCurrentUser() == null);
        AuthenticationService lock = new AuthenticationService(us);
        for (int i = 0; i < 5; i++) {
            try {
                lock.login("AD001", "bad");
            } catch (IllegalArgumentException ignored) {
                // expected
            }
        }
        check("5 failed attempts lock the account", throwsExpected(IllegalStateException.class, () -> lock.login("AD001", "admin123"), "Too many"));

        System.out.println("UserService (administrators)");
        String newId = us.generateUserID(Roles.ADMINISTRATOR);
        check("next administrator ID is AD002", newId.equals("AD002"));
        us.addUser(new Administrator(newId, "Daniel Wong", "014-5556677", "secret12"));
        check("duplicate ID rejected (even in another case)",
                throwsExpected(IllegalArgumentException.class, () -> us.addUser(new Administrator("ad002", "Dup Licate", "014-5556677", "secret12")), "already exists"));
        check("search finds by name", us.searchUser("wong").size() == 1);
        us.updateUser(new Administrator("AD002", "Daniel W. Wong", "014-5556677", "secret12"));
        check("update changes the name", us.getUserById("AD002").getName().equals("Daniel W. Wong"));
        us.deleteUser("AD002");
        check("delete removes the user", us.getUserById("AD002") == null);
        check("deleting a missing user fails", throwsExpected(IllegalArgumentException.class, () -> us.deleteUser("AD002"), "not found"));

        System.out.println("Staff report");
        check("students are not staff", us.viewStaff("", "").size() == 3);
        check("role filter", us.viewStaff(Roles.ADMINISTRATOR, "").size() == 1);
        check("name filter (and a name containing |)", us.viewStaff("", "kumar").get(0).getID().equals("IN001"));
        check("student is not a staff role", throwsExpected(IllegalArgumentException.class, () -> us.viewStaff(Roles.STUDENT, ""), "not a staff role"));
        check("totals by role", us.countByRole(us.viewStaff("", "")).get(Roles.INSTRUCTOR) == 1);
        check("text report has the total", us.generateStaffReport("", "").contains("Total staff: 3"));

        System.out.println("Revenue report");
        PaymentService ps = new PaymentService();
        ClassRegistrationService crs = new ClassRegistrationService();
        check("March 2026 revenue = 70.30 (PAID only, bad row skipped)",
                ps.getMonthlyRevenue(2026)[2].compareTo(new BigDecimal("70.30")) == 0);
        check("March 2026 payment count = 2", ps.getMonthlyPaymentCount(2026)[2] == 2);
        check("other years are separate", ps.getMonthlyRevenue(2025)[2].compareTo(new BigDecimal("10.00")) == 0);
        check("March registrations = 2, cancelled = 1",
                crs.getMonthlyRegistrations(2026)[2] == 2 && crs.getMonthlyCancelled(2026)[2] == 1);
        check("text report", ps.generateRevenueReport(3, 2026).contains("Total revenue: 70.30"));
        check("bad month rejected", throwsExpected(IllegalArgumentException.class, () -> ps.generateRevenueReport(13, 2026), "Month"));

        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("(no screen available: window tests skipped)");
        } else {
            guiTests(temp);
        }

        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
        System.exit(failed == 0 ? 0 : 1);
    }

    // ---- window tests --------------------------------------------------------------------------
    private static <T extends Component> List<T> find(Container root, Class<T> type) {
        List<T> found = new ArrayList<>();
        for (Component c : root.getComponents()) {
            if (type.isInstance(c)) {
                found.add(type.cast(c));
            }
            if (c instanceof Container) {
                found.addAll(find((Container) c, type));
            }
        }
        return found;
    }

    private static Frame visibleFrame(Class<?> type) {
        for (Frame f : Frame.getFrames()) {
            if (type.isInstance(f) && f.isShowing()) {
                return f;
            }
        }
        return null;
    }

    private static void shot(Frame f, String name) throws Exception {
        File dir = new File("build/shots");
        dir.mkdirs();
        BufferedImage img = new BufferedImage(f.getWidth(), f.getHeight(), BufferedImage.TYPE_INT_RGB);
        SwingUtilities.invokeAndWait(() -> f.paint(img.getGraphics()));
        ImageIO.write(img, "png", new File(dir, name + ".png"));
    }

    private static void ui(Runnable r) throws Exception {
        SwingUtilities.invokeAndWait(r);
    }

    private static void guiTests(Path temp) throws Exception {
        System.out.println("Swing windows");
        Main app = new Main();
        ui(app::start);
        Frame login = visibleFrame(LoginFrame.class);
        check("login window opens", login != null);
        shot(login, "1-login");

        List<JTextField> idFields = new ArrayList<>(find((Container) ((javax.swing.JFrame) login).getContentPane(), JTextField.class));
        idFields.removeIf(f -> f instanceof JPasswordField);
        JPasswordField pw = find((Container) ((javax.swing.JFrame) login).getContentPane(), JPasswordField.class).get(0);
        JButton signIn = find((Container) ((javax.swing.JFrame) login).getContentPane(), JButton.class).get(0);

        ui(() -> {
            idFields.get(0).setText("M001");
            pw.setText("wrong-password");
            signIn.doClick();
        });
        check("wrong password keeps the login window", visibleFrame(LoginFrame.class) != null && visibleFrame(ManagerFrame.class) == null);
        shot(login, "2-login-error");

        ui(() -> {
            pw.setText("manager123");
            signIn.doClick();
        });
        Frame mf = visibleFrame(ManagerFrame.class);
        check("Manager window opens after login", mf != null);
        check("login window is closed", visibleFrame(LoginFrame.class) == null);

        JTabbedPane tabs = find(((javax.swing.JFrame) mf).getContentPane(), JTabbedPane.class).get(0);
        check("three tabs: Administrators, Staff report, Revenue report", tabs.getTabCount() == 3);
        for (int i = 0; i < tabs.getTabCount(); i++) {
            final int index = i;
            ui(() -> tabs.setSelectedIndex(index));
            Thread.sleep(150);
            JTable table = find((Container) tabs.getComponentAt(i), JTable.class).get(0);
            int expected = i == 0 ? 1 : i == 1 ? 3 : 13;
            check(tabs.getTitleAt(i) + " table shows " + expected + " rows", table.getRowCount() == expected);
            shot(mf, "3-tab" + (i + 1));
        }

        // revenue tab: choose March 2026 and check the headline
        ui(() -> tabs.setSelectedIndex(2));
        JComboBox<?> monthBox = find((Container) tabs.getComponentAt(2), JComboBox.class).get(0);
        javax.swing.JSpinner year = find((Container) tabs.getComponentAt(2), javax.swing.JSpinner.class).get(0);
        ui(() -> {
            year.setValue(2026);
            monthBox.setSelectedIndex(3);
        });
        List<javax.swing.JLabel> labels = find((Container) tabs.getComponentAt(2), javax.swing.JLabel.class);
        boolean headlineOk = false;
        for (javax.swing.JLabel l : labels) {
            if (l.getText().contains("March 2026: 70.30")) {
                headlineOk = true;
            }
        }
        check("revenue headline for March 2026 shows 70.30", headlineOk);
        shot(mf, "4-revenue-march");

        // staff tab: filter by role
        ui(() -> tabs.setSelectedIndex(1));
        JComboBox<?> roleBox = find((Container) tabs.getComponentAt(1), JComboBox.class).get(0);
        ui(() -> roleBox.setSelectedItem("Instructor"));
        JTable staffTable = find((Container) tabs.getComponentAt(1), JTable.class).get(0);
        check("staff filter by role shows only the instructor", staffTable.getRowCount() == 1);

        // log out returns to the login window
        JButton logout = null;
        for (JButton b : find(((javax.swing.JFrame) mf).getContentPane(), JButton.class)) {
            if ("Log out".equals(b.getText())) {
                logout = b;
            }
        }
        final JButton logoutButton = logout;
        ui(logoutButton::doClick);
        check("Log out goes back to the login window", visibleFrame(LoginFrame.class) != null && visibleFrame(ManagerFrame.class) == null);

        // another role is routed to the "not connected yet" notice, not to the Manager window
        check("administrator cannot reach Manager functions",
                !app.getAuthenticationService().hasAccess(us(app).getUserById("AD001"), "REVENUE_REPORT"));
    }

    private static UserService us(Main app) {
        return app.getUserService();
    }
}
