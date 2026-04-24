package flc.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a scheduled group exercise lesson.
 * Each lesson has a max capacity of 4 members.
 * Week 1-4 = Month 1, Week 5-8 = Month 2.
 */
public class Lesson {
    private static final int MAX_CAPACITY = 4;

    private int lessonId;
    private Exercise exercise;
    private String day;           // "Saturday" or "Sunday"
    private int weekNumber;       // 1-8
    private int monthNumber;      // 1 or 2 (weeks 1-4 = month 1, weeks 5-8 = month 2)
    private TimeSlot timeSlot;
    private List<Member> enrolledMembers;
    private List<Review> reviews;

    public Lesson(int lessonId, Exercise exercise, String day, int weekNumber, TimeSlot timeSlot) {
        this.lessonId = lessonId;
        this.exercise = exercise;
        this.day = day;
        this.weekNumber = weekNumber;
        this.monthNumber = (weekNumber <= 4) ? 1 : 2;
        this.timeSlot = timeSlot;
        this.enrolledMembers = new ArrayList<>();
        this.reviews = new ArrayList<>();
    }

    public int getLessonId()           { return lessonId; }
    public Exercise getExercise()      { return exercise; }
    public String getDay()             { return day; }
    public int getWeekNumber()         { return weekNumber; }
    public int getMonthNumber()        { return monthNumber; }
    public TimeSlot getTimeSlot()      { return timeSlot; }
    public List<Member> getEnrolledMembers() { return enrolledMembers; }
    public List<Review> getReviews()   { return reviews; }

    public boolean hasSpace() {
        return enrolledMembers.size() < MAX_CAPACITY;
    }

    public int getAvailableSpaces() {
        return MAX_CAPACITY - enrolledMembers.size();
    }

    public boolean enrollMember(Member member) {
        if (!hasSpace()) return false;
        if (enrolledMembers.contains(member)) return false;
        enrolledMembers.add(member);
        return true;
    }

    public boolean removeMember(Member member) {
        return enrolledMembers.remove(member);
    }

    public boolean isMemberEnrolled(Member member) {
        return enrolledMembers.contains(member);
    }

    public void addReview(Review review) {
        reviews.add(review);
    }

    public double getAverageRating() {
        if (reviews.isEmpty()) return 0.0;
        double sum = 0;
        for (Review r : reviews) sum += r.getRating();
        return sum / reviews.size();
    }

    /** Income based only on attended bookings */
    public double getAttendedIncome(List<Booking> allBookings) {
        int attendedCount = 0;
        for (Booking b : allBookings) {
            if (b.getLesson() == this && b.isAttended()) attendedCount++;
        }
        return attendedCount * exercise.getPrice();
    }

    /** Count of attended members for this lesson */
    public int getAttendedCount(List<Booking> allBookings) {
        int count = 0;
        for (Booking b : allBookings) {
            if (b.getLesson() == this && b.isAttended()) count++;
        }
        return count;
    }

    public double getTotalIncome() {
        return enrolledMembers.size() * exercise.getPrice();
    }

    public int getMemberCount() {
        return enrolledMembers.size();
    }

    @Override
    public String toString() {
        return String.format("[M%d W%d %s %s] %s - Spaces: %d/%d - £%.2f",
                monthNumber, weekNumber, day, timeSlot.getLabel(),
                exercise.getName(), getMemberCount(), MAX_CAPACITY,
                exercise.getPrice());
    }
}
