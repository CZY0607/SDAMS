package sdams.student;



import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.CookieHandler;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;


public class PaymentsPanel extends JPanel {

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
    private ClassRegistrationService registrationService;
    private PaymentService paymentService;
    private Map<String, BigDecimal> prices;
    private StudentDashboard dashboard;
    private boolean updateRegistrationBox = false;

    private JComboBox<String> methodFilter;
    private JComboBox<String> statusFilter;

    private JComboBox<String> registrationBox;
    private JComboBox<String> paymentMethodBox;
    private JTextField amountField;
    private JTextField startDateField;
    private JTextField endDateField;
    private JTable table;
    private DefaultTableModel tableModel;

    public PaymentsPanel(Student student, ClassRegistrationService registrationService, PaymentService paymentService,
                         Map<String, BigDecimal> prices, StudentDashboard dashboard) {
        this.student = student;
        this.registrationService = registrationService;
        this.paymentService = paymentService;
        this.prices = prices;
        this.dashboard = dashboard;

        setLayout(new BorderLayout(10, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(CONTENT_BG);

        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setBackground(CONTENT_BG);




        //Payment Section
        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        paymentPanel.setBorder(BorderFactory.createTitledBorder("Make Payment"));
        paymentPanel.setBackground(CONTENT_BG);

        registrationBox = new JComboBox<>();
        registrationBox.setPreferredSize(new Dimension(100, 28));

        paymentMethodBox = new JComboBox<>(new String[] {"Card", "Cash", "Banking", "E-Wallet"});
        amountField = new JTextField(9);
        amountField.setEditable(false);



        JButton payButton = new JButton("Pay");
        payButton.setBackground(new Color(11, 94, 181));
        payButton.setForeground(Color.WHITE);
        payButton.setFont(new Font("Arial", Font.BOLD, 14));
        payButton.setFocusPainted(false);
        payButton.setOpaque(true);
        payButton.setContentAreaFilled(true);
        payButton.setBorderPainted(false);
        payButton.setPreferredSize(new Dimension(120, 40));

        paymentPanel.add(new JLabel("Registration:"));
        paymentPanel.add(registrationBox);
        paymentPanel.add(new JLabel("Payment Amount:"));
        paymentPanel.add(amountField);
        paymentPanel.add(new JLabel("Payment Methods"));
        paymentPanel.add(paymentMethodBox);
        paymentPanel.add(payButton);

        //Search filters
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Payment History Filters"));
        filterPanel.setBackground(CONTENT_BG);

        methodFilter = new JComboBox<>(new String[] {"All", "Card", "Cash", "Banking", "E-Wallet"});
        statusFilter = new JComboBox<>(new String[] {"All", "Paid", "Refunded"});

        startDateField = new JTextField(9);
        endDateField = new JTextField(9);

        JButton searchButton = new JButton("Search");
        JButton clearButton = new JButton("Clear");

        filterPanel.add(new JLabel("Method:"));
        filterPanel.add(methodFilter);

        filterPanel.add(new JLabel("Status:"));
        filterPanel.add(statusFilter);

        filterPanel.add(new JLabel("From:"));
        filterPanel.add(startDateField);

        filterPanel.add(new JLabel("To"));
        filterPanel.add(endDateField);

        filterPanel.add(searchButton);
        filterPanel.add(clearButton);

        topPanel.setLayout(new GridLayout(2, 1, 5, 10));

        topPanel.add(paymentPanel);
        topPanel.add(filterPanel);

        add(topPanel, BorderLayout.NORTH);

        registrationBox.setBackground(Color.WHITE);
        registrationBox.setForeground(MAIN_TEXT);

        paymentMethodBox.setForeground(Color.WHITE);
        paymentMethodBox.setBackground(MAIN_TEXT);

        methodFilter.setBackground(Color.WHITE);
        statusFilter.setBackground(MAIN_TEXT);

        statusFilter.setBackground(Color.WHITE);
        statusFilter.setForeground(MAIN_TEXT);

        amountField.setBackground(Color.WHITE);
        startDateField.setBackground(Color.WHITE);

        startDateField.setForeground(MAIN_TEXT);
        endDateField.setBackground(Color.WHITE);

        endDateField.setForeground(MAIN_TEXT);
        paymentMethodBox.setBackground(Color.WHITE);

        searchButton.setBackground(new Color(11, 94, 181));
        searchButton.setForeground(Color.WHITE);
        searchButton.setFont(new Font("Arial", Font.BOLD, 14));
        searchButton.setFocusPainted(false);
        searchButton.setOpaque(true);
        searchButton.setContentAreaFilled(true);
        searchButton.setBorderPainted(false);
        searchButton.setPreferredSize(new Dimension(120, 40));

        clearButton.setBackground(new Color(232, 236, 243));
        clearButton.setForeground(new Color(16, 24, 40));
        clearButton.setFont(new Font("Arial", Font.BOLD, 14));
        clearButton.setFocusPainted(false);
        clearButton.setOpaque(true);
        clearButton.setContentAreaFilled(true);
        clearButton.setBorderPainted(false);
        clearButton.setPreferredSize(new Dimension(120, 40));


        //The payment history table
        String[] paymentColumns = {"Payment ID", "Registration", "Amount", "Date", "Method", "Status"};

        tableModel = new DefaultTableModel(paymentColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setAutoCreateRowSorter(true);
        table.setBackground(WHITE_BG);
        table.setForeground(MAIN_TEXT);
        table.setSelectionForeground(new Color(214, 228, 247));
        table.setSelectionForeground(MAIN_TEXT);
        table.setGridColor(new Color(220, 224, 230));
        table.setRowHeight(30);

        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setForeground(MAIN_TEXT);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD,14));

        add(new JScrollPane(table), BorderLayout.CENTER);

        //The registration selection event
        registrationBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (updateRegistrationBox) {
                    displayAmount();
                }
            }
    });

        //The payment event for making payment
        payButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                makePayment();
            }
        });

        //Search event to see the history
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHistory();
            }
        });

        //Clear the filters
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                methodFilter.setSelectedItem("All");
                statusFilter.setSelectedItem("All");
                startDateField.setText("");
                endDateField.setText("");
                showHistory();
            }
        });


    }


    private void makePayment() {
        String registrationID = (String) registrationBox.getSelectedItem();

        if (registrationID == null) {
            return;
        }

        try{
            ClassRegistration registration = registrationService.findRegistration(registrationID);
            BigDecimal paymentAmount = prices.get(registration.getClassID());

            if (paymentAmount == null) {
                showWarning("The price for this class is not available.");
                return;
            }

            String method = (String) paymentMethodBox.getSelectedItem();
            paymentService.makePayment(registrationID, student.getID(), paymentAmount, method);

            paymentMethodBox.setSelectedIndex(0);
            dashboard.refreshAll();
            JOptionPane.showMessageDialog(this, "Payment recorded successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

        }
        catch (IllegalArgumentException | IllegalStateException | RegistrationException e) {
            showWarning(e.getMessage());
        }
        catch (IOException e) {
            showFileError(e.getMessage());
        }

    }


    private void displayAmount() {
        amountField.setText("");
        String registrationID = (String) registrationBox.getSelectedItem();

        if (registrationID == null) {
            return;
        }

        try {
            ClassRegistration registration = registrationService.findRegistration(registrationID);
            BigDecimal paymentAmount = prices.get(registration.getClassID());

            if (paymentAmount != null) {
                amountField.setText(paymentAmount.toPlainString());
            }

        }
        catch (IllegalArgumentException | RegistrationException e){
            showWarning(e.getMessage());
        }
        catch (IOException e) {
            showFileError(e.getMessage());
        }

    }

    private LocalDate readDate(String text) {
        if (text.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(text.trim());
        }
        catch (DateTimeException e) {
            throw new IllegalArgumentException("Date must be in YYYY-MM-DD format");
        }
    }


    private void showHistory() {
        try {
            reloadHistory(true);
        }
        catch (IllegalArgumentException e) {
            showWarning(e.getMessage());
        }
        catch (IOException e) {
            showFileError(e.getMessage());
        }

    }

    public void reloadTable() throws IOException {
        reloadUnpaid();
        reloadHistory(false);
    }

    private void reloadUnpaid() throws IOException {
        String previousSelection = (String) registrationBox.getSelectedItem();

        updateRegistrationBox = true;

        try {
            registrationBox.removeAllItems();

            List<ClassRegistration> registrations = registrationService.viewByStudent(student.getID());


            List<Payment> payments = paymentService.viewPaymentRecords(student.getID());

            for (ClassRegistration registration : registrations) {

                if (!registration.getStatus().equals("Booked")) {
                    continue;
                }

                if (!prices.containsKey(registration.getClassID())) {
                    continue;
                }

                boolean alreadyPaid = false;

                for (Payment payment : payments) {

                    if (payment.getRegistrationID().equals(registration.getRegistrationID()) && payment.getStatus().equals("Paid")) {
                        alreadyPaid = true;
                        break;
                    }
                }

                if (!alreadyPaid) {
                    registrationBox.addItem(registration.getRegistrationID());
                }

            }

            if (previousSelection != null) {
                for (int i = 0; i < registrationBox.getItemCount(); i++) {
                    registrationBox.getItemAt(i);
                    registrationBox.setSelectedItem(previousSelection);
                    break;
                }

            }
        }

        finally {
            updateRegistrationBox = false;
        }

        displayAmount();

    }

    private void reloadHistory(boolean applyFilters) throws IOException {

        String method = null;
        String status = null;
        LocalDate startDate = null;
        LocalDate endDate = null;

        if (applyFilters) {

            method = (String) methodFilter.getSelectedItem();
            status = (String) statusFilter.getSelectedItem();

            if ("All".equals(method)) {
                method = null;
            }

            if ("All".equals(status)) {
                status = null;
            }

            startDate = readDate(startDateField.getText());
            endDate = readDate(endDateField.getText());
        }

        List<Payment> paymentList = paymentService.searchPayments(
                student.getID(),
                method,
                status,
                startDate,
                endDate
        );

        tableModel.setRowCount(0);

        for (Payment payment : paymentList) {

            tableModel.addRow(new Object[]{
                    payment.getPaymentID(),
                    payment.getRegistrationID(),
                    payment.getAmount(),
                    payment.getPaymentDate(),
                    payment.getPaymentMethod(),
                    payment.getStatus()
            });
        }
    }


    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Action is Not Allowed", JOptionPane.WARNING_MESSAGE);
    }

    private void showFileError(String message) {
        JOptionPane.showMessageDialog(this, message, "File Error", JOptionPane.ERROR_MESSAGE);
    }



}
