package flc.service;

import flc.model.*;

import java.util.*;

/**
 * Service for displaying timetable information.
 */
public class TimetableService {

    private BookingService bookingService;

    public TimetableService(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Print timetable for a specific day across all weeks.
     */
    public void printTimetableByDay(String day) {
        System.out.println("\n========================================");
        System.out.println("  TIMETABLE FOR: " + day.toUpperCase());
        System.out.println("========================================");

        List<Lesson> lessons = bookingService.getLessonsByDay(day);
        if (lessons.isEmpty()) {
            System.out.println("  No lessons found for " + day);
            return;
        }

        lessons.sort(Comparator.comparingInt(Lesson::getWeekNumber)
                .thenComparing(Lesson::getTimeSlot));

        int currentWeek = -1;
        for (Lesson l : lessons) {
            if (l.getWeekNumber() != currentWeek) {
                currentWeek = l.getWeekNumber();
                System.out.println("\n  --- Week " + currentWeek + " ---");
            }
            System.out.printf("  %-10s %-12s £%-6.2f  Spaces: %d  Avg Rating: %.1f%n",
                    l.getTimeSlot().getLabel(),
                    l.getExercise().getName(),
                    l.getExercise().getPrice(),
                    l.getAvailableSpaces(),
                    l.getAverageRating());
        }
        System.out.println("========================================\n");
    }

    /**
     * Print timetable for a specific exercise type.
     */
    public void printTimetableByExercise(String exerciseName) {
        System.out.println("\n========================================");
        System.out.println("  TIMETABLE FOR EXERCISE: " + exerciseName.toUpperCase());
        System.out.println("========================================");

        List<Lesson> lessons = bookingService.getLessonsByExercise(exerciseName);
        if (lessons.isEmpty()) {
            System.out.println("  No lessons found for exercise: " + exerciseName);
            return;
        }

        lessons.sort(Comparator.comparingInt(Lesson::getWeekNumber)
                .thenComparing(l -> l.getDay())
                .thenComparing(Lesson::getTimeSlot));

        for (Lesson l : lessons) {
            System.out.printf("  Week %-2d  %-10s %-10s  £%-6.2f  Spaces: %d%n",
                    l.getWeekNumber(),
                    l.getDay(),
                    l.getTimeSlot().getLabel(),
                    l.getExercise().getPrice(),
                    l.getAvailableSpaces());
        }
        System.out.println("========================================\n");
    }

    /**
     * Print full weekly timetable (both days).
     */
    public void printWeeklyTimetable(int week) {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.printf("║        WEEK %d TIMETABLE                  ║%n", week);
        System.out.println("╚══════════════════════════════════════════╝");

        for (String day : new String[]{"Saturday", "Sunday"}) {
            System.out.println("\n  " + day.toUpperCase() + ":");
            System.out.println("  " + "-".repeat(55));
            System.out.printf("  %-10s %-15s %-8s %-8s %-10s%n",
                    "Time", "Exercise", "Price", "Spaces", "Avg Rating");
            System.out.println("  " + "-".repeat(55));

            List<Lesson> dayLessons = bookingService.getLessonsByWeekAndDay(week, day);
            for (Lesson l : dayLessons) {
                System.out.printf("  %-10s %-15s £%-6.2f %-8d %.1f%n",
                        l.getTimeSlot().getLabel(),
                        l.getExercise().getName(),
                        l.getExercise().getPrice(),
                        l.getAvailableSpaces(),
                        l.getAverageRating());
            }
        }
        System.out.println();
    }
}
