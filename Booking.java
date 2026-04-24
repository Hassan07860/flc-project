package flc.model;

/**
 * Represents a booking made by a member for a specific lesson.
 * Tracks booking lifecycle via BookingStatus.
 */
public class Booking {
    private static int counter = 1;

    private int bookingId;
    private Member member;
    private Lesson lesson;
    private BookingStatus status;

    public Booking(Member member, Lesson lesson) {
        this.bookingId = counter++;
        this.member = member;
        this.lesson = lesson;
        this.status = BookingStatus.BOOKED;
    }

    public int getBookingId()        { return bookingId; }
    public Member getMember()        { return member; }
    public Lesson getLesson()        { return lesson; }
    public BookingStatus getStatus() { return status; }

    public void setLesson(Lesson lesson)         { this.lesson = lesson; }
    public void setStatus(BookingStatus status)  { this.status = status; }

    public boolean isAttended()  { return status == BookingStatus.ATTENDED; }
    public boolean isCancelled() { return status == BookingStatus.CANCELLED; }
    public boolean isActive()    { return status == BookingStatus.BOOKED || status == BookingStatus.CHANGED; }

    @Override
    public String toString() {
        return String.format("Booking #%d | %s | %s | [%s]",
                bookingId, member.getName(), lesson, status);
    }

    // For testing: reset counter
    public static void resetCounter() { counter = 1; }
}
