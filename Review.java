package flc.model;

/**
 * Represents a review left by a member for a lesson they attended.
 * Rating scale: 1 (Very dissatisfied) to 5 (Very Satisfied).
 */
public class Review {
    private Member member;
    private Lesson lesson;
    private int rating;       // 1-5
    private String comment;

    public Review(Member member, Lesson lesson, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        this.member = member;
        this.lesson = lesson;
        this.rating = rating;
        this.comment = comment;
    }

    public Member getMember() { return member; }
    public Lesson getLesson() { return lesson; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }

    public String getRatingLabel() {
        switch (rating) {
            case 1: return "Very Dissatisfied";
            case 2: return "Dissatisfied";
            case 3: return "Ok";
            case 4: return "Satisfied";
            case 5: return "Very Satisfied";
            default: return "Unknown";
        }
    }

    @Override
    public String toString() {
        return String.format("Review by %s | Rating: %d (%s) | \"%s\"",
                member.getName(), rating, getRatingLabel(), comment);
    }
}
