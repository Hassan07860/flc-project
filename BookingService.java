package flc.service;

import flc.model.*;

import java.util.*;

/**
 * Service class handling all booking operations.
 * Implements core business logic for the FLC booking system.
 */
public class BookingService {

    private List<Member> members;
    private List<Lesson> lessons;
    private List<Booking> bookings;
    private List<Review> reviews;

    public BookingService() {
        this.members  = new ArrayList<>();
        this.lessons  = new ArrayList<>();
        this.bookings = new ArrayList<>();
        this.reviews  = new ArrayList<>();
    }

    // ─── Member Management ───────────────────────────────────────────

    /** Add a member, rejecting duplicate IDs. */
    public boolean addMember(Member member) {
        if (findMemberById(member.getMemberId()) != null) {
            System.out.println("ERROR: Member ID " + member.getMemberId() + " already exists.");
            return false;
        }
        members.add(member);
        return true;
    }

    public Member findMemberById(int id) {
        for (Member m : members) if (m.getMemberId() == id) return m;
        return null;
    }

    public Member findMemberByName(String name) {
        for (Member m : members) if (m.getName().equalsIgnoreCase(name)) return m;
        return null;
    }

    public List<Member> getAllMembers() { return members; }

    // ─── Lesson Management ───────────────────────────────────────────

    public void addLesson(Lesson lesson) { lessons.add(lesson); }
    public List<Lesson> getAllLessons()  { return lessons; }

    public List<Lesson> getLessonsByDay(String day) {
        List<Lesson> r = new ArrayList<>();
        for (Lesson l : lessons) if (l.getDay().equalsIgnoreCase(day)) r.add(l);
        return r;
    }

    public List<Lesson> getLessonsByWeekAndDay(int week, String day) {
        List<Lesson> r = new ArrayList<>();
        for (Lesson l : lessons)
            if (l.getWeekNumber() == week && l.getDay().equalsIgnoreCase(day)) r.add(l);
        r.sort(Comparator.comparing(Lesson::getTimeSlot));
        return r;
    }

    public List<Lesson> getLessonsByMonth(int month) {
        List<Lesson> r = new ArrayList<>();
        for (Lesson l : lessons) if (l.getMonthNumber() == month) r.add(l);
        return r;
    }

    public List<Lesson> getLessonsByExercise(String name) {
        List<Lesson> r = new ArrayList<>();
        for (Lesson l : lessons) if (l.getExercise().getName().equalsIgnoreCase(name)) r.add(l);
        return r;
    }

    public Lesson findLessonById(int id) {
        for (Lesson l : lessons) if (l.getLessonId() == id) return l;
        return null;
    }

    // ─── Booking Operations ──────────────────────────────────────────

    /** Book a lesson. Conflict is checked per week+day+slot. */
    public Booking bookLesson(Member member, Lesson lesson) {
        if (!lesson.hasSpace()) {
            System.out.println("ERROR: No space available in " + lesson.getExercise().getName());
            return null;
        }
        if (member.hasTimeConflict(lesson.getDay(), lesson.getTimeSlot(), lesson.getWeekNumber())) {
            System.out.println("ERROR: Time conflict for " + member.getName()
                    + " on Week " + lesson.getWeekNumber()
                    + " " + lesson.getDay() + " " + lesson.getTimeSlot());
            return null;
        }
        if (lesson.isMemberEnrolled(member)) {
            System.out.println("ERROR: " + member.getName() + " is already enrolled in this lesson.");
            return null;
        }
        lesson.enrollMember(member);
        Booking booking = new Booking(member, lesson);
        member.addBooking(booking);
        bookings.add(booking);
        return booking;
    }

    /** Change a booking. Sets status to CHANGED. */
    public boolean changeBooking(Booking booking, Lesson newLesson) {
        if (booking.isCancelled()) {
            System.out.println("ERROR: Cannot change a cancelled booking.");
            return false;
        }
        Member member    = booking.getMember();
        Lesson oldLesson = booking.getLesson();

        if (!newLesson.hasSpace()) {
            System.out.println("ERROR: No space in new lesson.");
            return false;
        }

        oldLesson.removeMember(member);
        BookingStatus prevStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELLED); // temp to exclude from conflict check

        if (member.hasTimeConflict(newLesson.getDay(), newLesson.getTimeSlot(), newLesson.getWeekNumber())) {
            oldLesson.enrollMember(member);
            booking.setStatus(prevStatus);
            System.out.println("ERROR: Time conflict with new lesson.");
            return false;
        }
        if (newLesson.isMemberEnrolled(member)) {
            oldLesson.enrollMember(member);
            booking.setStatus(prevStatus);
            System.out.println("ERROR: Already enrolled in new lesson.");
            return false;
        }

        newLesson.enrollMember(member);
        booking.setLesson(newLesson);
        booking.setStatus(BookingStatus.CHANGED);
        return true;
    }

    /** Cancel a booking. Keeps record with CANCELLED status.
     *  Cannot cancel an already attended or already cancelled booking. */
    public boolean cancelBooking(Booking booking) {
        if (booking.isCancelled()) {
            System.out.println("ERROR: Booking is already cancelled.");
            return false;
        }
        if (booking.isAttended()) {
            System.out.println("ERROR: Cannot cancel a lesson that has already been attended.");
            return false;
        }
        booking.getLesson().removeMember(booking.getMember());
        booking.setStatus(BookingStatus.CANCELLED);
        return true;
    }

    /** Mark a booking as ATTENDED. Must be active (BOOKED or CHANGED). */
    public boolean attendLesson(Booking booking) {
        if (!booking.isActive()) {
            System.out.println("ERROR: Booking must be active (BOOKED/CHANGED) to attend.");
            return false;
        }
        booking.setStatus(BookingStatus.ATTENDED);
        return true;
    }

    public List<Booking> getAllBookings() { return bookings; }

    public List<Booking> getBookingsByMember(Member member) {
        List<Booking> r = new ArrayList<>();
        for (Booking b : bookings) if (b.getMember().equals(member)) r.add(b);
        return r;
    }

    public List<Booking> getActiveBookingsByMember(Member member) {
        List<Booking> r = new ArrayList<>();
        for (Booking b : bookings) if (b.getMember().equals(member) && !b.isCancelled()) r.add(b);
        return r;
    }

    // ─── Review Operations ───────────────────────────────────────────

    /** Add a review. Member must have ATTENDED the lesson. */
    public Review addReview(Member member, Lesson lesson, int rating, String comment) {
        boolean attended = false;
        for (Booking b : bookings) {
            if (b.getMember().equals(member) && b.getLesson() == lesson && b.isAttended()) {
                attended = true;
                break;
            }
        }
        if (!attended) {
            System.out.println("ERROR: " + member.getName()
                    + " has not attended this lesson. Attend the lesson first.");
            return null;
        }
        Review review = new Review(member, lesson, rating, comment);
        lesson.addReview(review);
        reviews.add(review);
        return review;
    }

    /** For seeding only — bypasses attendance check. */
    public Review addReviewDirect(Member member, Lesson lesson, int rating, String comment) {
        Review review = new Review(member, lesson, rating, comment);
        lesson.addReview(review);
        reviews.add(review);
        return review;
    }

    public List<Review> getAllReviews() { return reviews; }
}
