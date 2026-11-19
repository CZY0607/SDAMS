package sdams;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Class diagram: ManagerFrame - the Manager's main window.
 * Three tabs, each shown only if AuthenticationService.hasAccess() allows it:
 *   Administrators (Add, View, Search, Update, Delete) | Staff report | Revenue report
 */
public class ManagerFrame extends JFrame {
    private static final Color ERROR_RED = new Color(168, 50, 45);

    private final Main app;
    private final User manager;
    private final UserService userService;
    private final PaymentService paymentService;
    private final ClassRegistrationService registrationService;

    private final JLabel statusLabel = new JLabel(" ");

    public ManagerFrame(Main app, User manager) {
        super("SDAMS - Manager");
        this.app = app;
        this.manager = manager;
        this.userService = app.getUserService();
        this.paymentService = app.getPaymentService();
        this.registrationService = app.getRegistrationService();
        buildScreen();
    }

    private void buildScreen() {
        AuthenticationService auth = app.getAuthenticationService();

        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JLabel title = new JLabel("SDAMS  Manager");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(new JLabel(manager.getName() + " (" + manager.getID() + ")"));
        JButton logout = new JButton("Log out");
        logout.addActionListener(e -> app.logout(this));
        right.add(logout);
        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        if (auth.hasAccess(manager, "MANAGE_ADMINISTRATORS")) {
            tabs.addTab("Administrators", new AdministratorsPanel());
        }
        if (auth.hasAccess(manager, "STAFF_REPORT")) {
            tabs.addTab("Staff report", new StaffReportPanel());
        }
        if (auth.hasAccess(manager, "REVENUE_REPORT")) {
            tabs.addTab("Revenue report", new RevenueReportPanel());
        }
        tabs.addChangeListener(e -> say("", false));

        statusLabel.setBorder(BorderFactory.createEmptyBorder(4, 12, 6, 12));

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(header, BorderLayout.NORTH);
        getContentPane().add(tabs, BorderLayout.CENTER);
        getContentPane().add(statusLabel, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(860, 600);
        setMinimumSize(new Dimension(700, 480));
        setLocationRelativeTo(null);
    }

    private void say(String message, boolean isError) {
        statusLabel.setText(message.isEmpty() ? " " : message);
        statusLabel.setForeground(isError ? ERROR_RED : new Color(31, 78, 95));
    }

    // ---- small helpers shared by the three tabs ------------------------------------------------
    private static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private static JTable makeTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        return table;
    }

    private static void onTextChange(JTextField field, Runnable action) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                action.run();
            }
        });
    }

    // ============================================================================================
    // Tab 1: manage administrators
    // ============================================================================================
    private class AdministratorsPanel extends JPanel {
        private final JTextField searchField = new JTextField(22);
        private final DefaultTableModel model = readOnlyModel("ID", "Name", "Contact number");
        private final JTable table = makeTable(model);
        private final List<User> shown = new ArrayList<>(); // same order as the table rows

        AdministratorsPanel() {
            super(new BorderLayout(0, 8));
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            JPanel top = new JPanel(new BorderLayout());
            JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            JLabel searchLabel = new JLabel("Search");
            searchLabel.setLabelFor(searchField);
            searchField.setToolTipText("Search by ID, name or contact number");
            searchRow.add(searchLabel);
            searchRow.add(searchField);
            JButton add = new JButton("Add administrator");
            add.addActionListener(e -> openForm(null));
            top.add(searchRow, BorderLayout.WEST);
            top.add(add, BorderLayout.EAST);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            JButton edit = new JButton("Edit selected");
            edit.addActionListener(e -> editSelected());
            JButton delete = new JButton("Delete selected");
            delete.addActionListener(e -> deleteSelected());
            bottom.add(edit);
            bottom.add(delete);

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        editSelected();
                    }
                }
            });
            onTextChange(searchField, this::reload);

            add(top, BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);
            add(bottom, BorderLayout.SOUTH);
            reload();
        }

        private void reload() {
            model.setRowCount(0);
            shown.clear();
            for (User u : userService.searchUser(searchField.getText())) {
                if (Roles.ADMINISTRATOR.equals(u.getRole())) {
                    shown.add(u);
                    model.addRow(new Object[] {u.getID(), u.getName(), u.getContact()});
                }
            }
        }

        private User selectedAdministrator() {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select an administrator in the table first.",
                        "Nothing selected", JOptionPane.INFORMATION_MESSAGE);
                return null;
            }
            return shown.get(row);
        }

        private void editSelected() {
            User selected = selectedAdministrator();
            if (selected != null) {
                openForm(selected);
            }
        }

        private void deleteSelected() {
            User selected = selectedAdministrator();
            if (selected == null) {
                return;
            }
            int answer = JOptionPane.showConfirmDialog(this,
                    "Delete administrator " + selected.getName() + " (" + selected.getID() + ")?\nThis cannot be undone.",
                    "Delete administrator", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (answer != JOptionPane.YES_OPTION) {
                return;
            }
            try {
                userService.deleteUser(selected.getID());
                say("Deleted " + selected.getName() + ".", false);
                reload();
            } catch (AppException ex) {
                say(ex.getMessage(), true);
            }
        }

        /** Add form when existing is null, otherwise the edit form. Re-opens with the typed values if the input is invalid. */
        private void openForm(User existing) {
            JTextField idField = new JTextField(existing == null ? "" : existing.getID(), 20);
            idField.setEnabled(existing == null);
            idField.setToolTipText(existing == null ? "Leave blank to generate the next ID" : "The ID cannot be changed");
            JTextField nameField = new JTextField(existing == null ? "" : existing.getName(), 20);
            JTextField contactField = new JTextField(existing == null ? "" : existing.getContact(), 20);
            JPasswordField passwordField = new JPasswordField(20);

            JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
            form.add(new JLabel(existing == null ? "User ID (blank = auto)" : "User ID"));
            form.add(idField);
            form.add(new JLabel("Full name"));
            form.add(nameField);
            form.add(new JLabel("Contact number"));
            form.add(contactField);
            form.add(new JLabel(existing == null ? "Password" : "New password (blank = keep)"));
            form.add(passwordField);

            String title = existing == null ? "Add administrator" : "Edit administrator";
            while (true) {
                int answer = JOptionPane.showConfirmDialog(this, form, title,
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                if (answer != JOptionPane.OK_OPTION) {
                    return;
                }
                try {
                    String password = new String(passwordField.getPassword());
                    if (existing == null) {
                        String id = idField.getText().trim().isEmpty()
                                ? userService.generateUserID(Roles.ADMINISTRATOR) : idField.getText().trim();
                        userService.addUser(new Administrator(id, nameField.getText(), contactField.getText(), password));
                        say("Added " + nameField.getText().trim() + " (" + id + ").", false);
                    } else {
                        String keptOrNew = password.isEmpty() ? existing.exportPassword() : password;
                        userService.updateUser(new Administrator(existing.getID(), nameField.getText(), contactField.getText(), keptOrNew));
                        say("Saved changes to " + nameField.getText().trim() + ".", false);
                    }
                    reload();
                    return;
                } catch (AppException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Please check the form", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }

    // ============================================================================================
    // Tab 2: staff report
    // ============================================================================================
    private class StaffReportPanel extends JPanel {
        private final List<String> roleValues = new ArrayList<>(); // "" = all roles; same order as the combo box
        private final JComboBox<String> roleBox = new JComboBox<>();
        private final JTextField nameField = new JTextField(18);
        private final DefaultTableModel model = readOnlyModel("ID", "Name", "Role", "Contact number");
        private final JLabel totalLabel = new JLabel(" ");
        private final JLabel breakdownLabel = new JLabel(" ");

        StaffReportPanel() {
            super(new BorderLayout(0, 8));
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            roleValues.add("");
            roleBox.addItem("All roles");
            for (String role : Roles.STAFF_ROLES) {
                roleValues.add(role);
                roleBox.addItem(Roles.label(role));
            }

            JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            JLabel roleLabel = new JLabel("Role");
            roleLabel.setLabelFor(roleBox);
            JLabel nameLabel = new JLabel("Name");
            nameLabel.setLabelFor(nameField);
            filters.add(roleLabel);
            filters.add(roleBox);
            filters.add(nameLabel);
            filters.add(nameField);

            totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 16f));
            JPanel totals = new JPanel(new GridLayout(2, 1));
            totals.add(totalLabel);
            totals.add(breakdownLabel);

            JPanel top = new JPanel(new BorderLayout(0, 8));
            top.add(filters, BorderLayout.NORTH);
            top.add(totals, BorderLayout.SOUTH);

            roleBox.addActionListener(e -> reload());
            onTextChange(nameField, this::reload);

            add(top, BorderLayout.NORTH);
            add(new JScrollPane(makeTable(model)), BorderLayout.CENTER);
            reload();
        }

        private void reload() {
            String role = roleValues.get(Math.max(0, roleBox.getSelectedIndex()));
            List<User> staff = userService.viewStaff(role, nameField.getText());
            model.setRowCount(0);
            for (User u : staff) {
                model.addRow(new Object[] {u.getID(), u.getName(), Roles.label(u.getRole()), u.getContact()});
            }
            totalLabel.setText("Total staff shown: " + staff.size());

            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Integer> e : userService.countByRole(staff).entrySet()) {
                if (e.getValue() > 0) {
                    sb.append(sb.length() > 0 ? ",  " : "").append(Roles.label(e.getKey())).append(" ").append(e.getValue());
                }
            }
            breakdownLabel.setText(sb.length() == 0 ? "No staff match these filters." : sb.toString());
        }
    }

    // ============================================================================================
    // Tab 3: revenue report (monthly / yearly revenue and registration counts)
    // ============================================================================================
    private class RevenueReportPanel extends JPanel {
        private final JSpinner yearSpinner =
                new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2000, 2100, 1));
        private final JComboBox<String> monthBox = new JComboBox<>();
        private final DefaultTableModel model =
                readOnlyModel("Month", "Revenue", "Payments", "Registrations", "Cancelled");
        private final JTable table = makeTable(model);
        private final JLabel headline = new JLabel(" ");
        private final JLabel detail = new JLabel(" ");

        RevenueReportPanel() {
            super(new BorderLayout(0, 8));
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            yearSpinner.setEditor(new JSpinner.NumberEditor(yearSpinner, "#")); // 2026, not 2,026
            monthBox.addItem("Whole year");
            for (Month m : Month.values()) {
                monthBox.addItem(m.getDisplayName(TextStyle.FULL, Locale.ENGLISH));
            }

            JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            JLabel yearLabel = new JLabel("Year");
            yearLabel.setLabelFor(yearSpinner);
            JLabel monthLabel = new JLabel("Month");
            monthLabel.setLabelFor(monthBox);
            filters.add(yearLabel);
            filters.add(yearSpinner);
            filters.add(monthLabel);
            filters.add(monthBox);

            headline.setFont(headline.getFont().deriveFont(Font.BOLD, 20f));
            JPanel summary = new JPanel(new GridLayout(2, 1));
            summary.add(headline);
            summary.add(detail);

            JPanel top = new JPanel(new BorderLayout(0, 8));
            top.add(filters, BorderLayout.NORTH);
            top.add(summary, BorderLayout.SOUTH);

            DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                                                               boolean focus, int row, int col) {
                    Component c = super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                    setHorizontalAlignment(col == 0 ? LEFT : RIGHT);
                    setFont(getFont().deriveFont(row == 12 ? Font.BOLD : Font.PLAIN)); // last row is the year total
                    return c;
                }
            };
            for (int i = 0; i < table.getColumnCount(); i++) {
                table.getColumnModel().getColumn(i).setCellRenderer(renderer);
            }

            JLabel note = new JLabel("Revenue counts payments with status PAID, by payment date. "
                    + "Registrations are counted by booking date.");
            note.setForeground(Color.GRAY);

            yearSpinner.addChangeListener(e -> reload());
            monthBox.addActionListener(e -> reload());

            add(top, BorderLayout.NORTH);
            add(new JScrollPane(table), BorderLayout.CENTER);
            add(note, BorderLayout.SOUTH);
            reload();
        }

        private void reload() {
            int year = (Integer) yearSpinner.getValue();
            int selectedMonth = monthBox.getSelectedIndex(); // 0 = whole year, 1..12 = month

            BigDecimal[] revenue = paymentService.getMonthlyRevenue(year);
            int[] payments = paymentService.getMonthlyPaymentCount(year);
            int[] registrations = registrationService.getMonthlyRegistrations(year);
            int[] cancelled = registrationService.getMonthlyCancelled(year);

            BigDecimal totalRevenue = BigDecimal.ZERO;
            int totalPayments = 0;
            int totalRegistrations = 0;
            int totalCancelled = 0;

            model.setRowCount(0);
            for (int i = 0; i < 12; i++) {
                model.addRow(new Object[] {
                    Month.of(i + 1).getDisplayName(TextStyle.FULL, Locale.ENGLISH),
                    revenue[i].setScale(2).toPlainString(), payments[i], registrations[i], cancelled[i]});
                totalRevenue = totalRevenue.add(revenue[i]);
                totalPayments += payments[i];
                totalRegistrations += registrations[i];
                totalCancelled += cancelled[i];
            }
            model.addRow(new Object[] {"Total " + year, totalRevenue.setScale(2).toPlainString(),
                totalPayments, totalRegistrations, totalCancelled});

            if (selectedMonth == 0) {
                headline.setText("Revenue " + year + ": " + totalRevenue.setScale(2).toPlainString());
                detail.setText(totalPayments + " payments received,  " + totalRegistrations
                        + " registrations (" + totalCancelled + " cancelled)");
                table.clearSelection();
            } else {
                int i = selectedMonth - 1;
                headline.setText("Revenue " + Month.of(selectedMonth).getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                        + " " + year + ": " + revenue[i].setScale(2).toPlainString());
                detail.setText(payments[i] + " payments received,  " + registrations[i]
                        + " registrations (" + cancelled[i] + " cancelled)");
                table.setRowSelectionInterval(i, i);
            }
        }
    }
}
