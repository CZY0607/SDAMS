package sdams;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/** Class diagram: LoginFrame - the sign-in window shared by all six roles. */
public class LoginFrame extends JFrame {
    private final Main app;
    private final AuthenticationService authService;

    private final JTextField userIdField = new JTextField(18);
    private final JPasswordField passwordField = new JPasswordField(18);
    private final JLabel errorLabel = new JLabel(" ");

    public LoginFrame(Main app, AuthenticationService authService) {
        super("SDAMS - Sign in");
        this.app = app;
        this.authService = authService;
        buildScreen();
    }

    private void buildScreen() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridwidth = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 0, 2, 0);

        JLabel title = new JLabel("SDAMS");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        c.gridy = 0;
        form.add(title, c);

        JLabel hint = new JLabel("Sign in with your user ID. Your role decides which menu you see.");
        hint.setForeground(Color.GRAY);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 14, 0);
        form.add(hint, c);

        c.insets = new Insets(8, 0, 2, 0);
        JLabel idLabel = new JLabel("User ID");
        idLabel.setLabelFor(userIdField);
        idLabel.setDisplayedMnemonic('U');
        c.gridy = 2;
        form.add(idLabel, c);
        c.insets = new Insets(2, 0, 2, 0);
        c.gridy = 3;
        form.add(userIdField, c);

        c.insets = new Insets(8, 0, 2, 0);
        JLabel pwLabel = new JLabel("Password");
        pwLabel.setLabelFor(passwordField);
        pwLabel.setDisplayedMnemonic('P');
        c.gridy = 4;
        form.add(pwLabel, c);
        c.insets = new Insets(2, 0, 2, 0);
        c.gridy = 5;
        form.add(passwordField, c);

        errorLabel.setForeground(new Color(168, 50, 45));
        c.insets = new Insets(8, 0, 4, 0);
        c.gridy = 6;
        form.add(errorLabel, c);

        JButton signIn = new JButton("Sign in");
        signIn.addActionListener(e -> signIn());
        c.insets = new Insets(4, 0, 0, 0);
        c.gridy = 7;
        form.add(signIn, c);

        getRootPane().setDefaultButton(signIn); // pressing Enter signs in
        setContentPane(form);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setMinimumSize(new Dimension(420, getHeight()));
        setLocationRelativeTo(null);
    }

    private void signIn() {
        errorLabel.setText(" ");
        try {
            User user = authService.login(userIdField.getText(), new String(passwordField.getPassword()));
            passwordField.setText("");
            app.openFrameFor(this, user);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            errorLabel.setText(ex.getMessage());
            passwordField.setText("");
            passwordField.requestFocusInWindow();
        }
    }
}
