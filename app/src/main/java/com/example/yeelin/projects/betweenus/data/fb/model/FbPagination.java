package com.example.yeelin.projects.betweenus.data.fb.model;

/**
 * Created by ninjakiki on 11/30/15.
 */
/**
 * FbPagination class
 */
public class FbPagination {
    private final FbCursor cursors;
    private final String previous;
    private final String next;

    public FbPagination(FbCursor cursors, String previous, String next) {
        this.cursors = cursors;
        this.previous = previous;
        this.next = next;
    }

    public FbPagination(String previous, String next, String before, String after) {
        this.cursors = new FbCursor(after, before);
        this.previous = previous;
        this.next = next;
    }

    public FbCursor getCursors() {
        return cursors;
    }

    public String getPrevious() {
        return previous;
    }

    public String getNext() {
        return next;
    }

    @Override
    public String toString() {
        return String.format("Cursors:%s, Next:%s, Previous:%s", cursors, next, previous);
    }

    /**
     * FbCursor class
     */
    public static class FbCursor {
        private final String after;
        private final String before;

        public FbCursor(String after, String before) {
            this.after = after;
            this.before = before;
        }

        public String getAfter() {
            return after;
        }

        public String getBefore() {
            return before;
        }

        @Override
        public String toString() {
            return String.format("After:%s, Before:%s", after, before);
        }
    }
}
