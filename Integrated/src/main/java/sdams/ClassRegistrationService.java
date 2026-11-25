package sdams;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Class diagram: ClassRegistrationService (Member 3 owns it).
 * Only the READ side that the Manager's Revenue Report needs is here.
 *
 * registrations.txt:  registrationID|studentID|classID|bookingDate|status
 * Assumption to confirm with Member 3: cancelled bookings have the status CANCELLED.
 */
public class ClassRegistrationService {
    public static final String CANCELLED_STATUS = "CANCELLED";

    private final FileStore store = new FileStore("registrations.txt");

    private List<Object[]> load() {
        List<Object[]> rows = new ArrayList<>();
        for (String[] f : store.readAll()) {
            try {
                rows.add(new Object[] {LocalDate.parse(f[3].trim()), f[4].trim()});
            } catch (RuntimeException e) {
                System.err.println("registrations.txt: skipped bad record \"" + f[0] + "\" (" + e.getMessage() + ")");
            }
        }
        return rows;
    }

    /** Registrations made in each month of a year, counted by booking date. Index 0 = January. */
    public int[] getMonthlyRegistrations(int year) {
        int[] months = new int[12];
        for (Object[] r : load()) {
            LocalDate d = (LocalDate) r[0];
            if (d.getYear() == year) {
                months[d.getMonthValue() - 1]++;
            }
        }
        return months;
    }

    /** Of those, how many were cancelled. */
    public int[] getMonthlyCancelled(int year) {
        int[] months = new int[12];
        for (Object[] r : load()) {
            LocalDate d = (LocalDate) r[0];
            if (d.getYear() == year && CANCELLED_STATUS.equalsIgnoreCase((String) r[1])) {
                months[d.getMonthValue() - 1]++;
            }
        }
        return months;
    }
}
