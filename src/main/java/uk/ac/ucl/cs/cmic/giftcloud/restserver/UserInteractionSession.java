package uk.ac.ucl.cs.cmic.giftcloud.restserver;

/**
 * Represents a single operation performed by the user. The idea is that if the user responds to a diaog (in particular,
 * choosing cancel), then any other operations in the same interaction session which are running on different threads
 * can automatically give the same response (cancel), rather than presenting the user with multiple dialogs
 */
public class UserInteractionSession {

    // Volatile ensures every thread can immediately access changes to the cancelled state
    private volatile boolean cancelled = false;

    public void cancel() {
        cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }


}
