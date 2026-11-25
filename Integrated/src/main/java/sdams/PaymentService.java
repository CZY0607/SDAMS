package sdams;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Class diagram: PaymentService (Member 3 owns it).
 * Only the READ side that the Manager's Revenue Report needs is here.
 *
 * payments.txt:  paymentID|registrationID|amount|paymentDate|paymentMethod|status
 * Assumption to confirm with Member 3: a payment counts as revenue when its status is PAID.
 */
public class PaymentService {
    public static final String REVENUE_STATUS = "PAID";

    private final FileStore store = new FileStore("payments.txt");

    /** One parsed line of payments.txt (Member 3's Payment class can replace this). */
    private static class Row {
        final BigDecimal amount;
        final LocalDate date;
        final String status;

        Row(BigDecimal amount, LocalDate date, String status) {
            this.amount = amount;
            this.date = date;
            this.status = status;
        }
    }

    private List<Row> load() {
        List<Row> rows = new ArrayList<>();
        for (String[] f : store.readAll()) {
            try {
                int offset = f.length == 7 ? 1 : 0;
                BigDecimal amount = new BigDecimal(f[2 + offset].trim());
                if (amount.signum() < 0) {
                    throw new NumberFormatException("negative amount");
                }
                rows.add(new Row(amount, LocalDate.parse(f[3 + offset].trim()), f[5 + offset].trim().toUpperCase(java.util.Locale.ROOT)));
            } catch (RuntimeException e) {
                System.err.println("payments.txt: skipped bad record \"" + f[0] + "\" (" + e.getMessage() + ")");
            }
        }
        return rows;
    }

    /** Revenue for each month of a year. Index 0 = January ... 11 = December. */
    public BigDecimal[] getMonthlyRevenue(int year) {
        BigDecimal[] months = new BigDecimal[12];
        java.util.Arrays.fill(months, BigDecimal.ZERO.setScale(2));
        for (Row r : load()) {
            if (REVENUE_STATUS.equals(r.status) && r.date.getYear() == year) {
                int i = r.date.getMonthValue() - 1;
                months[i] = months[i].add(r.amount);
            }
        }
        return months;
    }

    /** Number of revenue-counting payments for each month of a year. */
    public int[] getMonthlyPaymentCount(int year) {
        int[] months = new int[12];
        for (Row r : load()) {
            if (REVENUE_STATUS.equals(r.status) && r.date.getYear() == year) {
                months[r.date.getMonthValue() - 1]++;
            }
        }
        return months;
    }

    /** Diagram signature: generateRevenueReport(month, year): String */
    public String generateRevenueReport(Integer month, Integer year) {
        if (month == null || month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be 1-12");
        }
        BigDecimal revenue = getMonthlyRevenue(year)[month - 1].setScale(2);
        int count = getMonthlyPaymentCount(year)[month - 1];
        return "REVENUE REPORT - " + java.time.Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year
                + "\nPayments received: " + count
                + "\nTotal revenue: " + revenue.toPlainString();
    }
}
