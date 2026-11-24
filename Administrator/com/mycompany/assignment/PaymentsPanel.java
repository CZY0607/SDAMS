package com.mycompany.assignment;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class PaymentsPanel extends JPanel {
    private static final String ANY = "ALL";

    private final PaymentRecordReader reader;
    private final JTextField searchField = new JTextField(14);
    private final JComboBox<String> statusCombo = new JComboBox<>();
    private final JTextField fromField = new JTextField(8);
    private final JTextField toField = new JTextField(8);
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Payment ID", "Registration ID", "Student ID", "Student", "Amount (RM)",
                "Date", "Method", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    private final JLabel summary = new JLabel(" ");

    public PaymentsPanel(PaymentRecordReader reader) {
        this.reader = reader;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Search:"));
        top.add(searchField);
        top.add(new JLabel("Status:"));
        top.add(statusCombo);
        top.add(new JLabel("From (yyyy-mm-dd):"));
        top.add(fromField);
        top.add(new JLabel("To:"));
        top.add(toField);
        JButton filterBtn = new JButton("Filter");
        JButton resetBtn = new JButton("Reset / Refresh");
        top.add(filterBtn);
        top.add(resetBtn);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(summary, BorderLayout.SOUTH);

        filterBtn.addActionListener(e -> apply());
        searchField.addActionListener(e -> apply());
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            fromField.setText("");
            toField.setText("");
            reload();
        });
        reload();
    }

    public void reload() {
        List<PaymentRecordReader.Row> all = reader.loadAll();
        statusCombo.removeAllItems();
        statusCombo.addItem(ANY);
        for (String s : reader.distinctStatuses(all)) {
            statusCombo.addItem(s);
        }
        show(all);
    }

    private void apply() {
        try {
            LocalDate from = parseDate(fromField.getText(), "From");
            LocalDate to = parseDate(toField.getText(), "To");
            if (from != null && to != null && from.isAfter(to)) {
                throw new IllegalArgumentException("'From' date cannot be after 'To' date");
            }
            String status = (String) statusCombo.getSelectedItem();
            List<PaymentRecordReader.Row> rows = reader.filter(reader.loadAll(),
                    searchField.getText(), ANY.equals(status) ? null : status, from, to);
            show(rows);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Please check your input",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static LocalDate parseDate(String text, String label) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(label + " date must look like 2026-11-09");
        }
    }

    private void show(List<PaymentRecordReader.Row> rows) {
        model.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        for (PaymentRecordReader.Row r : rows) {
            model.addRow(new Object[]{r.paymentID, r.registrationID, r.studentID, r.studentName,
                r.amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString(),
                r.date, r.method, r.status});
            total = total.add(r.amount);
        }
        summary.setText(rows.size() + " payment(s), total RM "
                + total.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
    }
}
