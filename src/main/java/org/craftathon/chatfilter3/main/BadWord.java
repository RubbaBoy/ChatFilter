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

import org.craftathon.chatfilter3.qobjects.QChar;

/**
 * Represents a word that should be blocked. Only one of these objects should be made per bad word.
 */
public interface BadWord extends Cloneable {

    /**
     * Gets the priority of a word.
     *
     * @return The word's priority, 1 being highest
     */
    int getPriority();

    /**
     * Gets the length in normal characters the word is.
     *
     * @return The word's length
     */
    int getLength();

    /**
     * Gets the amount of letters processed.
     *
     * @return The amount of letters processed
     */
    int getLetters();

    /**
     * Gets the amount of numbers processed.
     *
     * @return The amount of numbers processed
     */
    int getNumbers();

    /**
     * Gets the amount of spaces processed.
     *
     * @return The amount of spaces processed
     */
    int getSpaces();

    /**
     * Resets all processed info for the current {@link BadWord}, e.g. {@link BadWord#getLetters()}.
     */
    void resetTemporary();

    /**
     * Gets if there is a {@link QChar} available for processing next.
     *
     * @return If a {@link QChar} is available
     */
    boolean nextAvailable();

    /**
     * Gets if the given {@link QChar} available is part of the current {@link BadWord} or not.
     *
     * @param previous The previously parsed {@link QChar}, or null
     * @param qChar The current {@link QChar}
     * @return If the {@link QChar} is part of the {@link BadWord}
     */
    boolean allowedNext(QChar previous, QChar qChar);

    /**
     * Gets the original string (Stripped of any rules).
     *
     * @return The original string
     */
    String getComparingString();

    /**
     * If clean is false, this will output the {@link Object#toString()} of all {@link QChar}s with some extra
     * formatting. If true, it will output the original string with `!` for any placeholders.
     *
     * @param clean If the result should be readable or not
     * @return The string
     */
    String toString(boolean clean);
}
