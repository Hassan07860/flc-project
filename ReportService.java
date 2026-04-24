package flc.report;

import flc.model.*;
import flc.service.BookingService;

import java.util.*;

/**
 * Generates monthly reports for FLC management.
 * Month 1 = Weeks 1-4, Month 2 = Weeks 5-8.
 */
public class ReportService {

    private BookingService bookingService;

    public ReportService(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    // ─────────────────────────────────────────────────────────────────
    // REPORT 1: Monthly lesson attendance report
    // ─────────────────────────────────────────────────────────────────

    /**
     * Report 1: For a given month, list each lesson with attended member count
     * and average rating. Only attended bookings count.
     */
    public void printMonthlyLessonReport(int month) {
        if (month < 1 || month > 2) {
            System.out.println("ERROR: Month must be 1 or 2.");
            return;
        }

        System.out.println("\n╔══════════════════════════════════════════════════════════════════════╗");
        System.out.printf( "║      REPORT 1: MONTHLY LESSON ATTENDANCE REPORT — MONTH %d          ║%n", month);
        System.out.println("╚══════════════════════════════════════════════════════════════════════╝");
        System.out.printf("  (Weeks %s | Attendance = ATTENDED bookings only)%n",
                month == 1 ? "1–4" : "5–8");

        List<Lesson> monthLessons = bookingService.getLessonsByMonth(month);
        monthLessons.sort(Comparator.comparingInt(Lesson::getWeekNumber)
                .thenComparing(l -> l.getDay())
                .thenComparing(Lesson::getTimeSlot));

        List<Booking> allBookings = bookingService.getAllBookings();

        int currentWeek = -1;
        String currentDay = "";

        for (Lesson l : monthLessons) {
            if (l.getWeekNumber() != currentWeek) {
                currentWeek = l.getWeekNumber();
                System.out.println("\n  ▶ WEEK " + currentWeek);
            }
            if (!l.getDay().equals(currentDay)) {
                currentDay = l.getDay();
                System.out.println("    ── " + currentDay + " ──");
                System.out.printf("    %-12s %-15s %-10s %-12s %-20s%n",
                        "Time", "Exercise", "Attended", "Avg Rating", "Attended Members");
                System.out.println("    " + "-".repeat(70));
            }

            int attendedCount = l.getAttendedCount(allBookings);
            StringBuilder memberNames = new StringBuilder();
            for (Booking b : allBookings) {
                if (b.getLesson() == l && b.isAttended()) {
                    if (memberNames.length() > 0) memberNames.append(", ");
                    memberNames.append(b.getMember().getName().split(" ")[0]);
                }
            }

            String ratingStr = l.getReviews().isEmpty() ? "N/A"
                    : String.format("%.1f/5.0", l.getAverageRating());

            System.out.printf("    %-12s %-15s %-10d %-12s %s%n",
                    l.getTimeSlot().getLabel(),
                    l.getExercise().getName(),
                    attendedCount,
                    ratingStr,
                    memberNames.toString().isEmpty() ? "—" : memberNames.toString());
        }

        // Summary per exercise for this month
        System.out.println("\n  ── MONTH " + month + " SUMMARY ──");
        Map<String, Integer> exAttended = new LinkedHashMap<>();
        Map<String, Double>  exRatings  = new LinkedHashMap<>();
        Map<String, Integer> exReviews  = new LinkedHashMap<>();

        for (Lesson l : monthLessons) {
            String name = l.getExercise().getName();
            exAttended.merge(name, l.getAttendedCount(allBookings), Integer::sum);
            for (Review r : l.getReviews()) {
                exRatings.merge(name, (double) r.getRating(), Double::sum);
                exReviews.merge(name, 1, Integer::sum);
            }
        }

        System.out.printf("  %-15s %-18s %-18s%n", "Exercise", "Total Attended", "Avg Rating");
        System.out.println("  " + "-".repeat(52));
        for (String name : exAttended.keySet()) {
            int cnt = exReviews.getOrDefault(name, 0);
            String avg = cnt > 0
                    ? String.format("%.2f/5.00", exRatings.get(name) / cnt)
                    : "No reviews";
            System.out.printf("  %-15s %-18d %s%n", name, exAttended.get(name), avg);
        }
        System.out.println("═".repeat(72) + "\n");
    }

    // ─────────────────────────────────────────────────────────────────
    // REPORT 2: Monthly champion exercise (highest income) report
    // ─────────────────────────────────────────────────────────────────

    /**
     * Report 2: For a given month, show income per exercise type (attended only).
     * Highlights the champion (highest income) exercise.
     */
    public void printMonthlyIncomeReport(int month) {
        if (month < 1 || month > 2) {
            System.out.println("ERROR: Month must be 1 or 2.");
            return;
        }

        System.out.println("\n╔══════════════════════════════════════════════════════════════════════╗");
        System.out.printf( "║      REPORT 2: CHAMPION EXERCISE INCOME REPORT — MONTH %d           ║%n", month);
        System.out.println("╚══════════════════════════════════════════════════════════════════════╝");
        System.out.printf("  (Income based on ATTENDED bookings only | Weeks %s)%n",
                month == 1 ? "1–4" : "5–8");

        List<Lesson> monthLessons = bookingService.getLessonsByMonth(month);
        List<Booking> allBookings = bookingService.getAllBookings();

        Map<String, Double>  incomeMap   = new LinkedHashMap<>();
        Map<String, Integer> attendedMap = new LinkedHashMap<>();
        Map<String, Double>  priceMap    = new LinkedHashMap<>();

        for (Lesson l : monthLessons) {
            String name = l.getExercise().getName();
            int attended = l.getAttendedCount(allBookings);
            double income = attended * l.getExercise().getPrice();
            incomeMap.merge(name, income, Double::sum);
            attendedMap.merge(name, attended, Integer::sum);
            priceMap.put(name, l.getExercise().getPrice());
        }

        List<Map.Entry<String, Double>> sorted = new ArrayList<>(incomeMap.entrySet());
        sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        System.out.printf("  %-15s %-12s %-12s %-12s%n",
                "Exercise", "Price/Class", "Attended", "Total Income");
        System.out.println("  " + "-".repeat(52));
        for (Map.Entry<String, Double> entry : sorted) {
            String name = entry.getKey();
            System.out.printf("  %-15s £%-11.2f %-12d £%.2f%n",
                    name,
                    priceMap.get(name),
                    attendedMap.get(name),
                    entry.getValue());
        }

        if (!sorted.isEmpty()) {
            String winner = sorted.get(0).getKey();
            double topIncome = sorted.get(0).getValue();
            System.out.println("\n  ★ CHAMPION EXERCISE (Month " + month + "): "
                    + winner + " → £" + String.format("%.2f", topIncome));
        }

        double total = sorted.stream().mapToDouble(Map.Entry::getValue).sum();
        System.out.printf("%n  TOTAL MONTH %d INCOME: £%.2f%n", month, total);
        System.out.println("═".repeat(72) + "\n");
    }

    /**
     * Run both reports for the given month.
     */
    public void printAllReports(int month) {
        printMonthlyLessonReport(month);
        printMonthlyIncomeReport(month);
    }
}
