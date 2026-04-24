package flc.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a member of Furzefield Leisure Centre.
 */
public class Member {
    private int memberId;
    private String name;
    private String email;
    private List<Booking> bookings;

    public Member(int memberId, String name, String email) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.bookings = new ArrayList<>();
    }

    public int getMemberId()           { return memberId; }
    public String getName()            { return name; }
    public String getEmail()           { return email; }
    public List<Booking> getBookings() { return bookings; }

    public void addBooking(Booking booking)    { bookings.add(booking); }
    public void removeBooking(Booking booking) { bookings.remove(booking); }

    /**
     * Checks if this member has an active (non-cancelled) booking
     * on the same WEEK, DAY and TIMESLOT — preventing double-booking
     * within the same weekend slot.
     */
    public boolean hasTimeConflict(String day, TimeSlot timeSlot, int weekNumber) {
        for (Booking b : bookings) {
            if (!b.isCancelled()
                    && b.getLesson().getWeekNumber() == weekNumber
                    && b.getLesson().getDay().equalsIgnoreCase(day)
                    && b.getLesson().getTimeSlot() == timeSlot) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the active booking for a specific lesson, or null.
     */
    public Booking getActiveBookingForLesson(Lesson lesson) {
        for (Booking b : bookings) {
            if (b.getLesson() == lesson && b.isActive()) return b;
        }
        return null;
    }

    @Override
    public String toString() {
        return "Member{id=" + memberId + ", name='" + name + "'}";
    }
}
