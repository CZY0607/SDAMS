package sdams;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Class diagram: Main - the application entry point.
 * It opens the login window and, after a successful login, opens the window that belongs to the
 * user's role (role-based navigation).
 *
 * HOW THE TEAM PLUGS IN: when your own frame is finished, add ONE line to the static block below, e.g.
 *     FRAMES.put(Roles.STUDENT, (app, user) -> new StudentFrame(app, user));
 * and add a "Log out" button in your frame that calls app.logout(thisFrame).
 */
public class Main {

    /** Creates the main window for one role. */
    public interface FrameFactory {
        JFrame create(Main app, User user);
    }

    private static final Map<String, FrameFactory> FRAMES = new HashMap<>();

    static {
        FRAMES.put(Roles.MANAGER, (app, user) -> new ManagerFrame(app, user));
        // FRAMES.put(Roles.ADMINISTRATOR, (app, user) -> new AdministratorFrame(app, user));
        // FRAMES.put(Roles.STUDENT, (app, user) -> new StudentFrame(app, user));
        // FRAMES.put(Roles.INSTRUCTOR, (app, user) -> new InstructorFrame(app, user));
        // FRAMES.put(Roles.FACILITIES_OFFICER, (app, user) -> new FacilitiesOfficerFrame(app, user));
        // FRAMES.put(Roles.ATTENDANCE_OFFICER, (app, user) -> new AttendanceOfficerFrame(app, user));
    }

    public static void registerFrame(String role, FrameFactory factory) {
        FRAMES.put(role, factory);
    }

    private final UserService userService = new UserService();
    private final PaymentService paymentService = new PaymentService();
    private final ClassRegistrationService registrationService = new ClassRegistrationService();
    private final AuthenticationService authService = new AuthenticationService(userService);

    public UserService getUserService() {
        return userService;
    }

    public PaymentService getPaymentService() {
        return paymentService;
    }

    public ClassRegistrationService getRegistrationService() {
        return registrationService;
    }

    public AuthenticationService getAuthenticationService() {
        return authService;
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

    public void start() {
        ensureFirstManager();
        showLogin();
    }

    /** With an empty users.txt nobody could ever log in, so the first run creates one Manager. */
    private void ensureFirstManager() {
        if (userService.getAllUsers().isEmpty()) {
            userService.addUser(new Manager("M001", "Default Manager", "012-3456789", "manager123"));
            System.out.println("No users found: created Manager M001 with password \"manager123\". Change it after your first login.");
        }
    }

    public void showLogin() {
        new LoginFrame(this, authService).setVisible(true);
    }

    /** Called by LoginFrame after a successful login. */
    public void openFrameFor(LoginFrame login, User user) {
        FrameFactory factory = FRAMES.get(user.getRole());
        if (factory == null) {
            JOptionPane.showMessageDialog(login,
                    "The " + Roles.label(user.getRole()) + " screen has not been connected to the main system yet.",
                    "Not available yet", JOptionPane.INFORMATION_MESSAGE);
            authService.logout();
            return;
        }
        login.dispose();
        factory.create(this, user).setVisible(true);
    }

    /** Called by every role's "Log out" button. */
    public void logout(JFrame currentFrame) {
        authService.logout();
        currentFrame.dispose();
        showLogin();
    }
}
