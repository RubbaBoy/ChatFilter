package org.craftathon.chatfilter3.main;

import org.craftathon.chatfilter3.qobjects.QString;

import java.util.List;

public interface BlockWordQueue {

    /**
     * Adds a blocked word to the queue to deal with later.
     *
     * @param blockedWord The {@link SimpleBlockedWord} to add
     */
    void addWord(BlockedWord blockedWord);

    /**
     * Sorts the blocked words.
     */
    void sort();

    /**
     * Gets the {@link SimpleBlockedWord}s.
     *
     * @return All added {@link SimpleBlockedWord}s
     */
    List<BlockedWord> getBlocked();

    /**
     * Gets the base {@link QString} being cleaned.
     *
     * @return The {@link QString} being cleaned
     */
    QString getQString();

    /**
     * Removes any overlaps of words.
     */
    void removeOverlaps();

    /**
     * Applies all blocked words to the base {@link QString}, and returns the cleaned output.
     *
     * @return The cleaned output
     */
    String applyBlocks();

    /**
     * Sets the time it took to process.
     *
     * @param time The time in milliseconds
     */
    void setTime(long time);

    /**
     * Gets the time it took to process.
     *
     * @return The time in milliseconds
     */
    long getTime();
}
