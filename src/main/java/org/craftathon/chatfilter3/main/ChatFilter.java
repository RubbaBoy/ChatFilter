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

import java.util.List;
import java.util.Map;

public interface ChatFilter {

    /**
     * Initialized the {@link ChatFilter} with default blocked and whitelisted words.
     */
    void init();

    /**
     * Initializes the {@link ChatFilter} with the given blocked words (word, priority) and default whitelist.
     *
     * @param words The words in (word, priority) as key/value respectively.
     */
    void init(Map<String, Integer> words);

    /**
     * Initializes the {@link ChatFilter} with the given blocked words (word, priority) and whitelisted words.
     *
     * @param words The words in (word, priority) as key/value respectively.
     * @param whitelisted A list of whitelisted words
     */
    void init(Map<String, Integer> words, List<String> whitelisted);

    /**
     * Sets the maximum percentage a word can be numbers before it isn't detected as bad anymore. By default this is 75.
     *
     * @param maxNumberPercentage The percentage from 0-100
     */
    void setMaxNumberPercentage(double maxNumberPercentage);

    /**
     * Sets if the system should block the entire word, or each bad piece.
     *
     * @param blockFullWord If the full word should be blocked
     */
    void setBlockFullWord(boolean blockFullWord);

    /**
     * Cleans an input.
     *
     * @param input The input string to clean
     * @return The cleaned output
     */
    String clean(String input);

    /**
     * Cleans an input, but without applying any of the {@link BlockedWord}s to the input. This is used for
     * custom/non-standard output of the words.
     *
     * @param input The string input
     * @return The unapplied {@link BlockWordQueue}
     */
    BlockWordQueue cleanUnapplied(String input);

    /**
     * Gets a cached {@link QChar} for the given character.
     *
     * @param character The character to get the {@link QChar} of.
     * @return The {@link QChar}
     */
    QChar getQCharFor(Character character);

    /**
     * Gets if the given {@link QChar} is a space.
     *
     * @param qChar The {@link QChar} to check
     * @return If the given {@link QChar} is a space
     */
    boolean isSpace(QChar qChar);

    /**
     * Gets all {@link QChar}s.
     *
     * @return All {@link QChar}s
     */
    List<QChar> getQuantumCharList();
}
