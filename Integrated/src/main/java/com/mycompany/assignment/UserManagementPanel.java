package com.mycompany.assignment;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

public class UserManagementPanel extends JPanel {
    private static final String ALL = "ALL";

    private final UserService service;
    private final List<String> roles;
    private final String entityName;

    private final JTextField txtSearch = new JTextField(14);
    private final JComboBox<String> roleFilter = new JComboBox<>();
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Role", "Name", "Contact", "Details"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JLabel lblStatus = new JLabel(" ");

    public UserManagementPanel(UserService service, List<String> roles, String entityName) {
        this.service = service;
        this.roles = new ArrayList<>(roles);
        this.entityName = entityName;
        buildUi();
        refreshTable();
    }

    private void buildUi() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Search User:"));
        top.add(txtSearch);
        if (roles.size() > 1) {
            roleFilter.addItem(ALL);
            for (String r : roles) {
                roleFilter.addItem(r);
            }
            top.add(new JLabel("Role:"));
            top.add(roleFilter);
        }
        JButton btnSearch = new JButton("Search");
        JButton btnShowAll = new JButton("Show All");
        top.add(btnSearch);
        top.add(btnShowAll);
        add(top, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(4, 4));
        JPanel status = new JPanel(new FlowLayout(FlowLayout.CENTER));
        status.add(lblStatus);
        bottom.add(status, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnReset = new JButton("Reset Pass");
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnReset);
        bottom.add(buttons, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        btnSearch.addActionListener(e -> refreshTable());
        txtSearch.addActionListener(e -> refreshTable());
        roleFilter.addActionListener(e -> refreshTable());
        btnShowAll.addActionListener(e -> {
            txtSearch.setText("");
            roleFilter.setSelectedItem(ALL);
            refreshTable();
        });
        btnAdd.addActionListener(e -> onAdd());
        btnUpdate.addActionListener(e -> onUpdate());
        btnDelete.addActionListener(e -> onDelete());
        btnReset.addActionListener(e -> onResetPassword());
    }

    private class UserForm {
        final User existing;
        final JComboBox<String> role = new JComboBox<>();
        final JTextField name = new JTextField(20);
        final JTextField contact = new JTextField(20);
        final JPasswordField password = new JPasswordField(20);
        final JTextField specializations = new JTextField(20);
        final JComboBox<String> status = new JComboBox<>(Student.STATUSES.toArray(new String[0]));
        final JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));

        UserForm(User existing) {
            this.existing = existing;
            for (String r : roles) {
                role.addItem(r);
            }
            if (existing == null) {
                panel.add(new JLabel("Role:"));
                if (roles.size() > 1) {
                    panel.add(role);
                } else {
                    panel.add(new JLabel(Roles.display(roles.get(0))));
                }
            } else {
                panel.add(new JLabel("User ID:"));
                panel.add(new JLabel(existing.getID()));
                panel.add(new JLabel("Role:"));
                panel.add(new JLabel(Roles.display(existing.getRole())));
                name.setText(existing.getName());
                contact.setText(existing.getContact());
            }
            panel.add(new JLabel("Name:"));
            panel.add(name);
            panel.add(new JLabel("Contact:"));
            panel.add(contact);
            panel.add(new JLabel(existing == null ? "Password:" : "New password (blank = keep):"));
            panel.add(password);
            if (roles.contains(Roles.INSTRUCTOR)) {
                panel.add(new JLabel("Specializations (comma-separated):"));
                panel.add(specializations);
                if (existing instanceof Instructor) {
                    specializations.setText(existing.getExtraData().replace(";", ", "));
                }
                role.addActionListener(e -> syncEnabled());
            }
            if (roles.contains(Roles.STUDENT)) {
                panel.add(new JLabel("Enrollment status:"));
                panel.add(status);
                if (existing instanceof Student) {
                    status.setSelectedItem(((Student) existing).getEnrollmentStatus());
                }
            }
            syncEnabled();
        }

        String selectedRole() {
            return existing != null ? existing.getRole() : (String) role.getSelectedItem();
        }

        private void syncEnabled() {
            specializations.setEnabled(Roles.INSTRUCTOR.equals(selectedRole()));
        }

        String extra() {
            String r = selectedRole();
            if (Roles.STUDENT.equals(r)) {
                return (String) status.getSelectedItem();
            }
            if (Roles.INSTRUCTOR.equals(r)) {
                return specializations.getText();
            }
            return "";
        }
    }

    private void onAdd() {
        UserForm form = new UserForm(null);
        while (true) {
            int ans = JOptionPane.showConfirmDialog(this, form.panel, "Add New " + entityName,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (ans != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                User u = service.createUser(form.selectedRole(), form.name.getText(), form.contact.getText(),
                        new String(form.password.getPassword()), form.extra());
                service.addUser(u);
                refreshTable();
                JOptionPane.showMessageDialog(this, "User added: " + u.getID());
                return;
            } catch (RuntimeException ex) {
                error(ex.getMessage());
            }
        }
    }

    private void onUpdate() {
        User selected = selectedUser("Select a user to update.");
        if (selected == null) {
            return;
        }
        UserForm form = new UserForm(selected);
        while (true) {
            int ans = JOptionPane.showConfirmDialog(this, form.panel, "Update User: " + selected.getID(),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (ans != JOptionPane.OK_OPTION) {
                return;
            }
            try {
                String name = ValidationUtil.requireName(form.name.getText());
                String contact = ValidationUtil.requireContact(form.contact.getText());
                String newPwd = new String(form.password.getPassword());
                if (!newPwd.isEmpty()) {
                    ValidationUtil.requirePassword(newPwd);
                }
                UserService.applyExtra(selected, form.extra());
                selected.setName(name);
                selected.setContact(contact);
                if (!newPwd.isEmpty()) {
                    selected.resetPassword(newPwd);
                }
                service.updateUser(selected);
                refreshTable();
                JOptionPane.showMessageDialog(this, "User updated.");
                return;
            } catch (RuntimeException ex) {
                error(ex.getMessage());
            }
        }
    }

    private void onDelete() {
        User selected = selectedUser("Select a user to delete.");
        if (selected == null) {
            return;
        }
        int conf = JOptionPane.showConfirmDialog(this,
                "Delete " + selected.getID() + " - " + selected.getName() + "? This cannot be undone.",
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            service.deleteUser(selected.getID());
            refreshTable();
            JOptionPane.showMessageDialog(this, "User deleted.");
        } catch (RuntimeException ex) {
            error(ex.getMessage());
        }
    }

    private void onResetPassword() {
        User selected = selectedUser("Select a user to reset password.");
        if (selected == null) {
            return;
        }
        JPasswordField pf1 = new JPasswordField();
        JPasswordField pf2 = new JPasswordField();
        Object[] msg = {"Enter new password for " + selected.getID() + ":", pf1, "Confirm new password:", pf2};
        while (true) {
            int ans = JOptionPane.showConfirmDialog(this, msg, "Reset Password",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (ans != JOptionPane.OK_OPTION) {
                return;
            }
            String p1 = new String(pf1.getPassword());
            String p2 = new String(pf2.getPassword());
            try {
                if (!p1.equals(p2)) {
                    throw new IllegalArgumentException("Passwords do not match.");
                }
                selected.resetPassword(p1);
                service.updateUser(selected);
                JOptionPane.showMessageDialog(this, "Password reset for " + selected.getID(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                return;
            } catch (RuntimeException ex) {
                error(ex.getMessage());
                pf1.setText("");
                pf2.setText("");
            }
        }
    }

    private User selectedUser(String hint) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, hint);
            return null;
        }
        String id = (String) model.getValueAt(table.convertRowIndexToModel(row), 0);
        User u = service.findById(id);
        if (u == null) {
            JOptionPane.showMessageDialog(this, "User not found.");
            refreshTable();
        }
        return u;
    }

    private void refreshTable() {
        String keyword = txtSearch.getText();
        String selected = roleFilter.getSelectedItem() == null ? ALL : (String) roleFilter.getSelectedItem();
        List<User> found = new ArrayList<>();
        for (String r : roles) {
            if (ALL.equals(selected) || r.equals(selected)) {
                found.addAll(service.searchUser(keyword, r));
            }
        }
        found.sort(Comparator.comparing(User::getID));
        model.setRowCount(0);
        for (User u : found) {
            model.addRow(new Object[]{u.getID(), Roles.display(u.getRole()), u.getName(),
                u.getContact(), details(u)});
        }
        String k = keyword == null ? "" : keyword.trim();
        lblStatus.setText("Showing results for: " + (k.isEmpty() ? "all users" : k)
                + "  (" + found.size() + " record(s))");
    }

    private static String details(User u) {
        if (u instanceof Student) {
            return "Enrollment: " + ((Student) u).getEnrollmentStatus();
        }
        if (u instanceof Instructor) {
            String s = u.getExtraData().replace(";", ", ");
            return s.isEmpty() ? "-" : s;
        }
        return "-";
    }

    private void error(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Please check your input", JOptionPane.ERROR_MESSAGE);
    }
}
