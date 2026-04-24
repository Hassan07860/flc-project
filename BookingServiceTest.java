package flc;

import flc.model.*;
import flc.service.BookingService;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;

/**
 * JUnit 4 tests for the FLC Booking System.
 * Covers: Member, Lesson, Booking, BookingStatus, Review, BookingService.
 */
public class BookingServiceTest {

    private BookingService service;
    private Member alice, bob, carol, david;
    private Exercise yoga, zumba;
    private Lesson satMorningYoga, satAfternoonZumba, satMorningZumba;

    @Before
    public void setUp() {
        Booking.resetCounter();
        service = new BookingService();

        yoga  = new Exercise("Yoga",  8.00, "Relaxing yoga");
        zumba = new Exercise("Zumba", 10.00, "Dance fitness");

        alice = new Member(1, "Alice", "alice@test.com");
        bob   = new Member(2, "Bob",   "bob@test.com");
        carol = new Member(3, "Carol", "carol@test.com");
        david = new Member(4, "David", "david@test.com");

        service.addMember(alice);
        service.addMember(bob);
        service.addMember(carol);
        service.addMember(david);

        satMorningYoga    = new Lesson(1, yoga,  "Saturday", 1, TimeSlot.MORNING);
        satAfternoonZumba = new Lesson(2, zumba, "Saturday", 1, TimeSlot.AFTERNOON);
        satMorningZumba   = new Lesson(3, zumba, "Saturday", 1, TimeSlot.MORNING);

        service.addLesson(satMorningYoga);
        service.addLesson(satAfternoonZumba);
        service.addLesson(satMorningZumba);
    }

    // ════════════════════════════════════════════════════════════════
    // MEMBER TESTS
    // ════════════════════════════════════════════════════════════════

    @Test
    public void testFindMemberById() {
        Member found = service.findMemberById(1);
        assertNotNull(found);
        assertEquals("Alice", found.getName());
    }

    @Test
    public void testFindMemberByIdNotFound() {
        assertNull(service.findMemberById(999));
    }

    @Test
    public void testFindMemberByName() {
        Member found = service.findMemberByName("Bob");
        assertNotNull(found);
        assertEquals(2, found.getMemberId());
    }

    @Test
    public void testGetAllMembers() {
        assertEquals(4, service.getAllMembers().size());
    }

    /** Duplicate member ID must be rejected */
    @Test
    public void testDuplicateMemberIdRejected() {
        Member duplicate = new Member(1, "Another Alice", "another@test.com");
        boolean added = service.addMember(duplicate);
        assertFalse("Duplicate member ID should be rejected", added);
        assertEquals(4, service.getAllMembers().size());
    }

    // ════════════════════════════════════════════════════════════════
    // BOOKING STATUS TESTS
    // ════════════════════════════════════════════════════════════════

    @Test
    public void testNewBookingStatusIsBooked() {
        Booking booking = service.bookLesson(alice, satMorningYoga);
        assertNotNull(booking);
        assertEquals(BookingStatus.BOOKED, booking.getStatus());
    }

    @Test
    public void testChangeBookingSetsStatusChanged() {
        Booking booking = service.bookLesson(alice, satMorningYoga);
        assertNotNull(booking);
        boolean changed = service.changeBooking(booking, satAfternoonZumba);
        assertTrue(changed);
        assertEquals(BookingStatus.CHANGED, booking.getStatus());
    }

    @Test
    public void testCancelBookingSetsStatusCancelled() {
        Booking booking = service.bookLesson(alice, satMorningYoga);
        assertNotNull(booking);
        boolean cancelled = service.cancelBooking(booking);
        assertTrue(cancelled);
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
    }

    @Test
    public void testAttendLessonSetsStatusAttended() {
        Booking booking = service.bookLesson(alice, satMorningYoga);
        assertNotNull(booking);
        boolean attended = service.attendLesson(booking);
        assertTrue(attended);
        assertEquals(BookingStatus.ATTENDED, booking.getStatus());
    }

    @Test
    public void testCannotAttendCancelledBooking() {
        Booking booking = service.bookLesson(alice, satMorningYoga);
        service.cancelBooking(booking);
        boolean attended = service.attendLesson(booking);
        assertFalse("Cannot attend a cancelled booking", attended);
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
    }

    // ════════════════════════════════════════════════════════════════
    // BOOKING TESTS
    // ════════════════════════════════════════════════════════════════

    @Test
    public void testSuccessfulBooking() {
        Booking booking = service.bookLesson(alice, satMorningYoga);
        assertNotNull("Booking should be created", booking);
        assertEquals(alice, booking.getMember());
        assertEquals(satMorningYoga, booking.getLesson());
        assertTrue(satMorningYoga.isMemberEnrolled(alice));
        assertEquals(1, alice.getBookings().size());
    }

    @Test
    public void testBookingCapacityLimit() {
        Member eve = new Member(5, "Eve", "eve@test.com");
        service.addMember(eve);
        service.bookLesson(alice, satMorningYoga);
        service.bookLesson(bob,   satMorningYoga);
        service.bookLesson(carol, satMorningYoga);
        service.bookLesson(david, satMorningYoga);
        Booking extra = service.bookLesson(eve, satMorningYoga);
        assertNull("Lesson should be full", extra);
        assertEquals(4, satMorningYoga.getMemberCount());
    }

    @Test
    public void testTimeConflictPreventsBooking() {
        service.bookLesson(alice, satMorningYoga);
        Booking conflict = service.bookLesson(alice, satMorningZumba);
        assertNull("Should not allow time conflict", conflict);
        assertEquals(1, alice.getBookings().size());
    }

    @Test
    public void testNoConflictDifferentTimeSlot() {
        service.bookLesson(alice, satMorningYoga);
        Booking booking2 = service.bookLesson(alice, satAfternoonZumba);
        assertNotNull("Different time slot should succeed", booking2);
        assertEquals(2, alice.getBookings().size());
    }

    @Test
    public void testDuplicateBookingPrevented() {
        service.bookLesson(alice, satMorningYoga);
        Booking duplicate = service.bookLesson(alice, satMorningYoga);
        assertNull("Duplicate booking should be prevented", duplicate);
        assertEquals(1, satMorningYoga.getMemberCount());
    }

    // ════════════════════════════════════════════════════════════════
    // CHANGE BOOKING TESTS
    // ════════════════════════════════════════════════════════════════

    @Test
    public void testChangeBookingSuccess() {
        Booking booking = service.bookLesson(alice, satMorningYoga);
        assertNotNull(booking);
        boolean changed = service.changeBooking(booking, satAfternoonZumba);
        assertTrue("Booking change should succeed", changed);
        assertEquals(satAfternoonZumba, booking.getLesson());
        assertFalse(satMorningYoga.isMemberEnrolled(alice));
        assertTrue(satAfternoonZumba.isMemberEnrolled(alice));
    }

    @Test
    public void testChangeBookingFullLesson() {
        Member eve   = new Member(5, "Eve",   "eve@test.com");
        Member frank = new Member(6, "Frank", "frank@test.com");
        service.addMember(eve); service.addMember(frank);

        Booking aliceBooking = service.bookLesson(alice, satMorningYoga);
        service.bookLesson(bob,   satAfternoonZumba);
        service.bookLesson(carol, satAfternoonZumba);
        service.bookLesson(david, satAfternoonZumba);
        service.bookLesson(eve,   satAfternoonZumba);

        boolean changed = service.changeBooking(aliceBooking, satAfternoonZumba);
        assertFalse("Cannot change to full lesson", changed);
        assertEquals(satMorningYoga, aliceBooking.getLesson());
    }

    @Test
    public void testCannotChangeCancelledBooking() {
        Booking booking = service.bookLesson(alice, satMorningYoga);
        service.cancelBooking(booking);
        boolean changed = service.changeBooking(booking, satAfternoonZumba);
        assertFalse("Cannot change a cancelled booking", changed);
    }

    // ════════════════════════════════════════════════════════════════
    // CANCEL BOOKING TESTS
    // ════════════════════════════════════════════════════════════════

    @Test
    public void testCancelBookingKeepsRecord() {
        Booking booking = service.bookLesson(alice, satMorningYoga);
        assertNotNull(booking);
        service.cancelBooking(booking);
        // Record should still exist, just status changed
        assertEquals(1, service.getAllBookings().size());
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertFalse(satMorningYoga.isMemberEnrolled(alice));
    }

    @Test
    public void testCanBookAfterCancel() {
        Member eve = new Member(5, "Eve", "eve@test.com");
        service.addMember(eve);
        Booking aliceB = service.bookLesson(alice, satMorningYoga);
        service.bookLesson(bob,   satMorningYoga);
        service.bookLesson(carol, satMorningYoga);
        service.bookLesson(david, satMorningYoga);
        service.cancelBooking(aliceB);
        Booking newBooking = service.bookLesson(eve, satMorningYoga);
        assertNotNull("Should be able to book after cancellation", newBooking);
    }

    // ════════════════════════════════════════════════════════════════
    // ATTEND LESSON TESTS
    // ════════════════════════════════════════════════════════════════

    @Test
    public void testAttendLessonSuccess() {
        Booking booking = service.bookLesson(alice, satMorningYoga);
        boolean attended = service.attendLesson(booking);
        assertTrue(attended);
        assertTrue(booking.isAttended());
    }

    @Test
    public void testAttendedCountUpdates() {
        Booking b1 = service.bookLesson(alice, satMorningYoga);
        Booking b2 = service.bookLesson(bob,   satMorningYoga);
        service.attendLesson(b1);
        // Only alice attended, bob did not
        assertEquals(1, satMorningYoga.getAttendedCount(service.getAllBookings()));
    }

    // ════════════════════════════════════════════════════════════════
    // REVIEW TESTS
    // ════════════════════════════════════════════════════════════════

    @Test
    public void testReviewRequiresAttendance() {
        service.bookLesson(alice, satMorningYoga);
        // Not attended yet — review must fail
        Review review = service.addReview(alice, satMorningYoga, 5, "Should fail");
        assertNull("Review should require attendance first", review);
    }

    @Test
    public void testAddReviewAfterAttendance() {
        Booking booking = service.bookLesson(alice, satMorningYoga);
        service.attendLesson(booking);
        Review review = service.addReview(alice, satMorningYoga, 5, "Excellent!");
        assertNotNull(review);
        assertEquals(5, review.getRating());
        assertEquals(1, satMorningYoga.getReviews().size());
    }

    @Test
    public void testReviewNotAllowedWithoutBooking() {
        Review review = service.addReview(alice, satMorningYoga, 4, "Should fail");
        assertNull("Non-enrolled member cannot review", review);
    }

    @Test
    public void testInvalidRatingAboveFiveThrows() {
        try {
            new Review(alice, satMorningYoga, 6, "Invalid");
            fail("Should throw for rating > 5");
        } catch (IllegalArgumentException e) { /* expected */ }
    }

    @Test
    public void testInvalidRatingBelowOneThrows() {
        try {
            new Review(alice, satMorningYoga, 0, "Invalid");
            fail("Should throw for rating < 1");
        } catch (IllegalArgumentException e) { /* expected */ }
    }

    @Test
    public void testAverageRating() {
        Booking b1 = service.bookLesson(alice, satMorningYoga);
        Booking b2 = service.bookLesson(bob,   satMorningYoga);
        service.attendLesson(b1);
        service.attendLesson(b2);
        service.addReview(alice, satMorningYoga, 4, "Good");
        service.addReview(bob,   satMorningYoga, 2, "Disappointing");
        assertEquals(3.0, satMorningYoga.getAverageRating(), 0.001);
    }

    @Test
    public void testAverageRatingNoReviews() {
        assertEquals(0.0, satMorningYoga.getAverageRating(), 0.001);
    }

    // ════════════════════════════════════════════════════════════════
    // LESSON TESTS
    // ════════════════════════════════════════════════════════════════

    @Test
    public void testLessonHasSpace() {
        assertTrue(satMorningYoga.hasSpace());
        service.bookLesson(alice, satMorningYoga);
        service.bookLesson(bob,   satMorningYoga);
        service.bookLesson(carol, satMorningYoga);
        service.bookLesson(david, satMorningYoga);
        assertFalse(satMorningYoga.hasSpace());
    }

    @Test
    public void testGetAvailableSpaces() {
        assertEquals(4, satMorningYoga.getAvailableSpaces());
        service.bookLesson(alice, satMorningYoga);
        assertEquals(3, satMorningYoga.getAvailableSpaces());
    }

    @Test
    public void testLessonMonthNumber() {
        Lesson week1Lesson = new Lesson(10, yoga, "Saturday", 1, TimeSlot.MORNING);
        Lesson week5Lesson = new Lesson(11, yoga, "Saturday", 5, TimeSlot.MORNING);
        assertEquals(1, week1Lesson.getMonthNumber());
        assertEquals(2, week5Lesson.getMonthNumber());
    }

    @Test
    public void testTotalIncome() {
        service.bookLesson(alice, satMorningYoga);
        service.bookLesson(bob,   satMorningYoga);
        assertEquals(16.0, satMorningYoga.getTotalIncome(), 0.001);
    }

    @Test
    public void testAttendedIncomeCountsOnlyAttended() {
        Booking b1 = service.bookLesson(alice, satMorningYoga);
        service.bookLesson(bob, satMorningYoga);
        service.attendLesson(b1); // only alice attended
        double income = satMorningYoga.getAttendedIncome(service.getAllBookings());
        assertEquals(8.0, income, 0.001); // only 1 attended × £8
    }

    // ════════════════════════════════════════════════════════════════
    // QUERY / TIMETABLE TESTS
    // ════════════════════════════════════════════════════════════════

    @Test
    public void testGetLessonsByDay() {
        Lesson sunLesson = new Lesson(10, yoga, "Sunday", 1, TimeSlot.MORNING);
        service.addLesson(sunLesson);
        assertEquals(3, service.getLessonsByDay("Saturday").size());
        assertEquals(1, service.getLessonsByDay("Sunday").size());
    }

    @Test
    public void testGetLessonsByExercise() {
        assertEquals(1, service.getLessonsByExercise("Yoga").size());
        assertEquals(2, service.getLessonsByExercise("Zumba").size());
    }

    @Test
    public void testGetLessonsByWeekAndDay() {
        Lesson w2Lesson = new Lesson(20, yoga, "Saturday", 2, TimeSlot.MORNING);
        service.addLesson(w2Lesson);
        List<Lesson> week1Sat = service.getLessonsByWeekAndDay(1, "Saturday");
        assertEquals(3, week1Sat.size()); // setUp adds 3 Saturday Week-1 lessons
        List<Lesson> week2Sat = service.getLessonsByWeekAndDay(2, "Saturday");
        assertEquals(1, week2Sat.size());
    }

    @Test
    public void testGetLessonsByMonth() {
        // satMorningYoga and satAfternoonZumba and satMorningZumba are all week 1 = month 1
        List<Lesson> month1 = service.getLessonsByMonth(1);
        assertEquals(3, month1.size());
        List<Lesson> month2 = service.getLessonsByMonth(2);
        assertEquals(0, month2.size());
    }

    @Test
    public void testFindLessonById() {
        Lesson found = service.findLessonById(1);
        assertNotNull(found);
        assertEquals("Yoga", found.getExercise().getName());
    }

    @Test
    public void testFindLessonByIdNotFound() {
        assertNull(service.findLessonById(999));
    }
    // ════════════════════════════════════════════════════════════════
    // EXTRA: CANCEL AFTER ATTEND
    // ════════════════════════════════════════════════════════════════

    @Test
    public void testCannotCancelAttendedBooking() {
        Booking booking = service.bookLesson(alice, satMorningYoga);
        service.attendLesson(booking);
        boolean cancelled = service.cancelBooking(booking);
        assertFalse("Cannot cancel a lesson already attended", cancelled);
        assertEquals(BookingStatus.ATTENDED, booking.getStatus());
    }

}