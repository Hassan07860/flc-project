package flc.ui;

import flc.data.DataInitializer;
import flc.model.*;
import flc.report.ReportService;
import flc.service.BookingService;
import flc.service.TimetableService;

import java.util.*;

/**
 * Main entry point for the Furzefield Leisure Centre Booking System.
 * Provides an interactive text-based menu interface.
 */
public class MainApp {

    private static BookingService    bookingService   = new BookingService();
    private static TimetableService  timetableService = new TimetableService(bookingService);
    private static ReportService     reportService    = new ReportService(bookingService);
    private static Scanner           scanner          = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║   FURZEFIELD LEISURE CENTRE                  ║");
        System.out.println("║   Group Exercise Booking System v2.0         ║");
        System.out.println("╚══════════════════════════════════════════════╝");

        DataInitializer.populate(bookingService);

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("Enter choice: ");
            switch (choice) {
                case 1: viewTimetableMenu();     break;
                case 2: bookLessonMenu();         break;
                case 3: changeBookingMenu();      break;
                case 4: cancelBookingMenu();      break;
                case 5: attendLessonMenu();       break;
                case 6: addReviewMenu();          break;
                case 7: viewMemberBookings();     break;
                case 8: reportsMenu();            break;
                case 9: running = false; System.out.println("Goodbye!"); break;
                default: System.out.println("Invalid choice. Try again.");
            }
        }
        scanner.close();
    }

    private static void printMainMenu() {
        System.out.println("\n══════════ MAIN MENU ══════════");
        System.out.println("  1. View Timetable");
        System.out.println("  2. Book a Lesson");
        System.out.println("  3. Change a Booking");
        System.out.println("  4. Cancel a Booking");
        System.out.println("  5. Attend a Lesson");
        System.out.println("  6. Add a Review (for already attended lessons)");
        System.out.println("  7. View My Bookings");
        System.out.println("  8. Monthly Reports");
        System.out.println("  9. Exit");
        System.out.println("═══════════════════════════════");
    }

    // ── Timetable Menu ────────────────────────────────────────────────

    private static void viewTimetableMenu() {
        System.out.println("\n── View Timetable ──");
        System.out.println("  1. By Day (all weeks)");
        System.out.println("  2. By Exercise Name");
        System.out.println("  3. Full Weekly Timetable");
        int choice = readInt("Choice: ");
        switch (choice) {
            case 1:
                String day = readDayChoice();
                timetableService.printTimetableByDay(day);
                break;
            case 2:
                System.out.print("Enter exercise name (Yoga/Zumba/Aquacise/Box Fit/Body Blitz/Pilates): ");
                String ex = scanner.nextLine().trim();
                timetableService.printTimetableByExercise(ex);
                break;
            case 3:
                int week = readInt("Enter week number (1-8): ");
                timetableService.printWeeklyTimetable(week);
                break;
            default:
                System.out.println("Invalid.");
        }
    }

    // ── Book Lesson Menu ──────────────────────────────────────────────

    private static void bookLessonMenu() {
        System.out.println("\n── Book a Lesson ──");
        Member member = selectMember();
        if (member == null) return;

        int week = readInt("Enter week number (1-8): ");
        String day = readDayChoice();

        List<Lesson> lessons = bookingService.getLessonsByWeekAndDay(week, day);
        if (lessons.isEmpty()) { System.out.println("No lessons found."); return; }
        printLessonList(lessons);
        int lessonId = readInt("Enter Lesson ID: ");
        Lesson lesson = bookingService.findLessonById(lessonId);
        if (lesson == null) { System.out.println("Lesson not found."); return; }

        Booking booking = bookingService.bookLesson(member, lesson);
        if (booking != null) System.out.println("✓ Booking confirmed: " + booking);
    }

    // ── Change Booking Menu ───────────────────────────────────────────

    private static void changeBookingMenu() {
        System.out.println("\n── Change a Booking ──");
        Member member = selectMember();
        if (member == null) return;

        List<Booking> active = bookingService.getActiveBookingsByMember(member);
        if (active.isEmpty()) { System.out.println("No active bookings found."); return; }

        System.out.println("Active bookings:");
        for (Booking b : active) System.out.println("  " + b);

        int bookingId = readInt("Enter Booking ID to change: ");
        Booking target = findBookingById(active, bookingId);
        if (target == null) { System.out.println("Booking not found."); return; }

        int week = readInt("New week (1-8): ");
        String day = readDayChoice();
        List<Lesson> lessons = bookingService.getLessonsByWeekAndDay(week, day);
        printLessonList(lessons);
        int newLessonId = readInt("Enter new Lesson ID: ");
        Lesson newLesson = bookingService.findLessonById(newLessonId);
        if (newLesson == null) { System.out.println("Lesson not found."); return; }

        boolean changed = bookingService.changeBooking(target, newLesson);
        System.out.println(changed ? "✓ Booking updated. Status: CHANGED" : "✗ Could not change booking.");
    }

    // ── Cancel Booking Menu ───────────────────────────────────────────

    private static void cancelBookingMenu() {
        System.out.println("\n── Cancel a Booking ──");
        Member member = selectMember();
        if (member == null) return;

        List<Booking> active = bookingService.getActiveBookingsByMember(member);
        if (active.isEmpty()) { System.out.println("No active bookings to cancel."); return; }

        System.out.println("Active bookings:");
        for (Booking b : active) System.out.println("  " + b);

        int bookingId = readInt("Enter Booking ID to cancel: ");
        Booking target = findBookingById(active, bookingId);
        if (target == null) { System.out.println("Booking not found."); return; }

        boolean cancelled = bookingService.cancelBooking(target);
        System.out.println(cancelled ? "✓ Booking cancelled. Status: CANCELLED" : "✗ Could not cancel.");
    }

    // ── Attend Lesson Menu (review incorporated per Slide 12) ────────

    private static void attendLessonMenu() {
        System.out.println("\n── Attend a Lesson ──");
        Member member = selectMember();
        if (member == null) return;

        List<Booking> active = bookingService.getActiveBookingsByMember(member);
        if (active.isEmpty()) { System.out.println("No active bookings to attend."); return; }

        System.out.println("Active bookings:");
        for (int i = 0; i < active.size(); i++) {
            System.out.println("  [" + (i + 1) + "] " + active.get(i));
        }
        int idx = readInt("Select booking number: ") - 1;
        if (idx < 0 || idx >= active.size()) { System.out.println("Invalid selection."); return; }

        Booking booking = active.get(idx);
        boolean attended = bookingService.attendLesson(booking);
        if (!attended) return;

        System.out.println("✓ Attendance recorded: " + booking.getLesson().getExercise().getName()
                + " | Status: ATTENDED");

        // Incorporate review immediately after attending
        System.out.println("\n  ── Leave a Review (optional — press 0 to skip) ──");
        int rating = readInt("  Rating (1=Very Dissatisfied ... 5=Very Satisfied, 0=Skip): ");
        if (rating >= 1 && rating <= 5) {
            System.out.print("  Comment: ");
            String comment = scanner.nextLine().trim();
            Review review = bookingService.addReview(member, booking.getLesson(), rating, comment);
            System.out.println(review != null ? "  ✓ Review saved." : "  ✗ Could not save review.");
        } else {
            System.out.println("  Review skipped.");
        }
    }

    // ── Add Review Menu ───────────────────────────────────────────────

    private static void addReviewMenu() {
        System.out.println("\n── Add a Review (requires ATTENDED status) ──");
        Member member = selectMember();
        if (member == null) return;

        // Show only attended bookings
        List<Booking> all = bookingService.getBookingsByMember(member);
        List<Booking> attended = new ArrayList<>();
        for (Booking b : all) if (b.isAttended()) attended.add(b);

        if (attended.isEmpty()) {
            System.out.println("No attended lessons found. Please attend a lesson first (Option 5).");
            return;
        }

        System.out.println("Attended lessons eligible for review:");
        for (int i = 0; i < attended.size(); i++) {
            System.out.println("  [" + (i + 1) + "] " + attended.get(i).getLesson());
        }
        int idx = readInt("Select lesson number: ") - 1;
        if (idx < 0 || idx >= attended.size()) { System.out.println("Invalid."); return; }
        Lesson lesson = attended.get(idx).getLesson();

        int rating = readInt("Rating (1=Very Dissatisfied ... 5=Very Satisfied): ");
        System.out.print("Comment: ");
        String comment = scanner.nextLine().trim();

        Review review = bookingService.addReview(member, lesson, rating, comment);
        System.out.println(review != null ? "✓ Review added: " + review : "✗ Could not add review.");
    }

    // ── View Member Bookings ──────────────────────────────────────────

    private static void viewMemberBookings() {
        Member member = selectMember();
        if (member == null) return;
        List<Booking> bookings = bookingService.getBookingsByMember(member);
        System.out.println("\nAll bookings for " + member.getName() + ":");
        if (bookings.isEmpty()) System.out.println("  No bookings.");
        else {
            for (Booking b : bookings) System.out.println("  " + b);
        }
    }

    // ── Reports Menu ──────────────────────────────────────────────────

    private static void reportsMenu() {
        System.out.println("\n── Monthly Reports ──");
        System.out.println("  Month 1 = Weeks 1-4 | Month 2 = Weeks 5-8");
        System.out.println("  1. Monthly Lesson Attendance Report");
        System.out.println("  2. Monthly Champion Exercise Income Report");
        System.out.println("  3. Both Reports");
        int choice = readInt("Choice: ");
        int month = readInt("Enter month number (1 or 2): ");

        switch (choice) {
            case 1: reportService.printMonthlyLessonReport(month); break;
            case 2: reportService.printMonthlyIncomeReport(month); break;
            case 3: reportService.printAllReports(month); break;
            default: System.out.println("Invalid.");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private static Member selectMember() {
        System.out.println("\nMembers:");
        for (Member m : bookingService.getAllMembers()) {
            System.out.printf("  [%d] %s%n", m.getMemberId(), m.getName());
        }
        int id = readInt("Enter Member ID: ");
        Member m = bookingService.findMemberById(id);
        if (m == null) System.out.println("Member not found.");
        return m;
    }

    private static String readDayChoice() {
        System.out.println("  1. Saturday  2. Sunday");
        int d = readInt("Day: ");
        return d == 2 ? "Sunday" : "Saturday";
    }

    private static void printLessonList(List<Lesson> lessons) {
        System.out.printf("  %-6s %-12s %-15s %-8s %-8s%n",
                "ID", "Time", "Exercise", "Spaces", "Price");
        System.out.println("  " + "-".repeat(50));
        for (Lesson l : lessons) {
            System.out.printf("  %-6d %-12s %-15s %-8d £%.2f%n",
                    l.getLessonId(),
                    l.getTimeSlot().getLabel(),
                    l.getExercise().getName(),
                    l.getAvailableSpaces(),
                    l.getExercise().getPrice());
        }
    }

    private static Booking findBookingById(List<Booking> bookings, int id) {
        for (Booking b : bookings) if (b.getBookingId() == id) return b;
        return null;
    }

    private static int readInt(String prompt) {
        if (!prompt.isEmpty()) System.out.print(prompt);
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (Exception e) {
            return -1;
        }
    }
}
