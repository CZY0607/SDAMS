package sdams.general;
import sdams.administrator.AdministratorFrame;
import sdams.manager.ManagerFrame;
import sdams.manager.RegistrationReportReader;
import sdams.manager.RevenueReportReader;
import sdams.student.Student;
import sdams.student.StudentDashboard;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
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
        FRAMES.put(Roles.ADMINISTRATOR, (app, user) -> {
            AdministratorFrame frame = new AdministratorFrame(app.userService, user);
            frame.setOnLogout(() -> app.logout(frame));
            return frame;
        });
        FRAMES.put(Roles.STUDENT, (app, user) -> new StudentDashboard(
                (Student) user, () -> { app.authService.logout(); app.showLogin(); }));

    }

    public static void registerFrame(String role, FrameFactory factory) {
        FRAMES.put(role, factory);
    }

    private final UserService userService = new UserService();
    private final RevenueReportReader paymentService = new RevenueReportReader();
    private final RegistrationReportReader registrationService = new RegistrationReportReader();
    private final AuthenticationService authService = new AuthenticationService(userService);

    public UserService getUserService() {
        return userService;
    }

    public RevenueReportReader getRevenueReportReader() {
        return paymentService;
    }

    public RegistrationReportReader getRegistrationService() {
        return registrationService;
    }

    public AuthenticationService getAuthenticationService() {
        return authService;
    }

    public static void main(String[] args) {
        LoginFrame.main(args);
    }

    public void start() {
        showLogin();
    }

    public void showLogin() {
        new LoginFrame(this).setVisible(true);
    }

    /** Called by LoginFrame after a successful login. */
    public void openFrameFor(LoginFrame login, User user) {
        if (user instanceof Student && !"Active".equals(((Student) user).getEnrollmentStatus())) {
            authService.logout();
            JOptionPane.showMessageDialog(login, "Your student account is inactive.");
            return;
        }
        FrameFactory factory = FRAMES.get(user.getRole());
        if (factory == null) {
            JOptionPane.showMessageDialog(login,
                    "The " + Roles.display(user.getRole()) + " screen has not been connected to the main system yet.",
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
