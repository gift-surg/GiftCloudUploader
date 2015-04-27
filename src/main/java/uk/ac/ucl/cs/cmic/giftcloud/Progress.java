package uk.ac.ucl.cs.cmic.giftcloud;

public interface Progress {
    void startProgressBar(int maximum);

    void startProgressBar();

    void updateProgressBar(int value);

    void updateProgressBar(int value, int maximum);

    void endProgressBar();

    void updateStatusText(final String progressText);

    boolean isCancelled();
}
