package org.craftathon.chatfilter3.main;

/**
 * Represents each word that has been blocked, i.e. each time *'s are shown
 */
public interface BlockedWord {

    /**
     * Gets the index in characters the blocked word is located at.
     *
     * @return The index in characters
     */
    int getIndex();

    /**
     * Gets the length in characters the blocked word is.
     *
     * @return The length in characters
     */
    int getLength();

    /**
     * Gets the percentage the word consists of numbers (0-1).
     *
     * @return The percentage the word consists of numbers
     */
    double getNumberPercentage();

    /**
     * Gets the exact original word being blocked.
     *
     * @return The original word
     */
    String getOriginalWord();

    /**
     * Gets the {@link BadWord} being blocked.
     *
     * @return The {@link BadWord} being blocked
     */
    BadWord getBadWord();
}
