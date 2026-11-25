package sdams.student;


import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BookClassesPanel extends JPanel {

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

    private Student student;
    private ClassRegistrationService service;
    private Map<String, BigDecimal> classPrices;
    private StudentDashboard studentDashboard;

    private JTextField searchField;
    private JTable table;
    private DefaultTableModel tableModel;

    private JButton bookButton;
    private JButton rescheduleButton;
    private JButton cancelButton;

    public BookClassesPanel(Student student, ClassRegistrationService service, Map<String, BigDecimal> classPrices, StudentDashboard studentDashboard) {
        this.student = student;
        this.service = service;
        this.classPrices = classPrices;
        this.studentDashboard = studentDashboard;

        setLayout(new BorderLayout(10, 15));
        setBackground(CONTENT_BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        //Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setOpaque(false);

        JLabel searchLabel = new JLabel("Class ID:");
        searchField = new JTextField(16);

        JButton searchButton = new JButton("Search");
        JButton clearButton = new JButton("Clear");

        searchButton.setBackground(PRIMARY_BTN);
        searchButton.setForeground(Color.BLACK);


        clearButton.setBackground(DISABLED_BTN);
        clearButton.setBackground(MAIN_TEXT);
        clearButton.setFocusPainted(false);
        clearButton.setBorderPainted(false);


        searchPanel.setBackground(CONTENT_BG);
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);

        add(searchPanel, BorderLayout.NORTH);

        //Registration table

        String[] columns = {"Registration ID", "Class ID", "Booking Date", "Status"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        add(new JScrollPane(table), BorderLayout.CENTER);

        //This will update the color when it is booked or not
        table.getColumnModel().getColumn(3).setCellRenderer(
                new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table, Object value, boolean isSelected,
                            boolean hasFocus, int row, int column) {

                        Component c = super.getTableCellRendererComponent(
                                table, value, isSelected, hasFocus, row, column
                        );

                        if (!isSelected) {
                            c.setBackground(Color.WHITE);
                            c.setForeground(MAIN_TEXT);
                        }

                        if (value != null && value.toString().equals("Booked")) {
                            c.setBackground(ACTIVE_STATUS_BG);
                            c.setForeground(ACTIVE_STATUS_TEXT);
                        }

                        return c;
                    }
                }
        );


        //Action Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonPanel.setOpaque(false);

        bookButton = new JButton("Book Class");
        rescheduleButton = new JButton("Reschedule");
        cancelButton = new JButton("Cancel Booking");


        bookButton = new JButton("Book Class");
        rescheduleButton = new JButton("Reschedule");
        cancelButton = new JButton("Cancel Booking");

        bookButton.setBackground(new Color(11, 94, 181));
        bookButton.setForeground(Color.WHITE);
        bookButton.setFont(new Font("Arial", Font.BOLD, 14));
        bookButton.setFocusPainted(false);
        bookButton.setOpaque(true);
        bookButton.setContentAreaFilled(true);
        bookButton.setBorderPainted(false);

        rescheduleButton.setBackground(new Color(232, 236, 243));
        rescheduleButton.setForeground(new Color(16, 24, 40));
        rescheduleButton.setFont(new Font("Arial", Font.BOLD, 14));
        rescheduleButton.setFocusPainted(false);
        rescheduleButton.setOpaque(true);
        rescheduleButton.setContentAreaFilled(true);


        cancelButton.setBackground(new Color(232, 236, 243));
        cancelButton.setForeground(new Color(16, 24, 40));
        cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
        cancelButton.setFocusPainted(false);
        cancelButton.setOpaque(true);
        cancelButton.setContentAreaFilled(true);

        bookButton.setEnabled(true);
        rescheduleButton.setEnabled(false);
        cancelButton.setEnabled(false);



        buttonPanel.add(bookButton);
        buttonPanel.add(rescheduleButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);
        cancelButton.setEnabled(false);
        rescheduleButton.setEnabled(false);

        table.setBackground(WHITE_BG);
        table.setForeground(MAIN_TEXT);
        table.setSelectionBackground(new Color(214, 228, 247));
        table.setSelectionForeground(MAIN_TEXT);
        table.setGridColor(new Color(220, 224, 230));
        table.setRowHeight(30);
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setForeground(MAIN_TEXT);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        searchLabel.setForeground(MAIN_TEXT);

        searchField.setForeground(MAIN_TEXT);
        searchField.setBackground(Color.WHITE);





        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRegistrations();
            }
        });


        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchField.setText("");
                showRegistrations();
            }
        });

        bookButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                bookClass();
            }
        });

        rescheduleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rescheduleBooking();
            }
        });


        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelBooking();
            }
        });

        //Buttons must be enabled only for active registrations
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String status = selectedValue(3);
                boolean bookedClass = "Booked".equals(status);

                rescheduleButton.setEnabled(bookedClass);
                cancelButton.setEnabled(bookedClass);

            }
        });


    }


    private String selectedValue(int columnIndex) {
        int row = table.getSelectedRow();

        if (row == -1) {
            return null;

        }

        return table.getValueAt(row, columnIndex).toString();
    }


    private String chooseClass(String title, String excludedClass) {
        List<String> choices = new ArrayList<>();

        for (String choice : classPrices.keySet()) {
            if (!choice.equals(excludedClass)) {
                choices.add(choice);
            }
        }

        if (choices.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No classes are available", "Warning", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        Object selectedClass = JOptionPane.showInputDialog(this, "Select a class", title, JOptionPane.QUESTION_MESSAGE, null, choices.toArray(), choices.get(0));
        if (selectedClass == null) {
            return null;
        }

        return selectedClass.toString();

    }


    private void bookClass() {
        String classID = chooseClass("Book Class", null);

        if (classID == null) {
            return;
        }

        try {
            service.bookClass(student.getID(), classID);

            searchField.setText("");
            studentDashboard.refreshAll();

            JOptionPane.showMessageDialog(this, "Class " + classID + " booked successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
        catch (IllegalArgumentException | IllegalStateException | RegistrationException e) {
            showWarning(e.getMessage());
        }

        catch (IOException e) {
            showFileError(e.getMessage());
        }

    }

    private void rescheduleBooking() {
        String registrationID = selectedValue(0);

        if (registrationID == null) {
            return;
        }

        String oldClass = selectedValue(1);
        String newClass = chooseClass("Reschedule Class", oldClass);

        if (newClass == null) {
            return;
        }

        try {
            service.rescheduleClass(registrationID, student.getID(), newClass);
            searchField.setText("");
            studentDashboard.refreshAll();
            JOptionPane.showMessageDialog(this, "Class rescheduled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

        }
        catch (IllegalArgumentException | IllegalStateException | RegistrationException e) {
            showWarning(e.getMessage());
        }

        catch (IOException e) {
            showFileError(e.getMessage());
        }
    }


    private void cancelBooking() {
        String registrationID = selectedValue(0);

        if (registrationID == null) {
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel " + registrationID + "?", "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

        if (confirmation != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            service.cancelBooking(registrationID, student.getID());

            studentDashboard.refreshAll();

            JOptionPane.showMessageDialog(this, "Registration cancelled successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
        catch (IllegalArgumentException | IllegalStateException | RegistrationException e) {
            showWarning(e.getMessage());
        }
        catch (IOException e) {
            showFileError(e.getMessage());
        }
    }

    private void showRegistrations() {
        try {
            loadRegistrations(searchField.getText().trim());
        }
        catch (IllegalArgumentException e) {
            showWarning(e.getMessage());
        }
        catch (IOException e) {
            showFileError(e.getMessage());
        }
    }

    public void reloadTable() throws IOException {
        loadRegistrations(null);
    }

    private void loadRegistrations(String classID) throws IOException {

        List<ClassRegistration> registrations =
                service.searchRegistration(classID, student.getID());

        tableModel.setRowCount(0);

        for (ClassRegistration registration : registrations) {
            tableModel.addRow(new Object[]{
                    registration.getRegistrationID(),
                    registration.getClassID(),
                    registration.getBookingDate(),
                    registration.getStatus()
            });
        }

        cancelButton.setEnabled(false);
        rescheduleButton.setEnabled(false);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Action is Not Allowed", JOptionPane.WARNING_MESSAGE);
    }

    private void showFileError(String message) {
        JOptionPane.showMessageDialog(this, message, "File Error", JOptionPane.ERROR_MESSAGE);
    }



}
