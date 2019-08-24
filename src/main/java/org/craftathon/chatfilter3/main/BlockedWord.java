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
     * Gets the percentage the word consists of numbers (0-100).
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
