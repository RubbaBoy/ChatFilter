/*
 * ChatFilter
 * Copyright (C) 2019 Craftathon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
