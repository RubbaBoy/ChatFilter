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

package org.craftathon.chatfilter3.dictionary;

import org.craftathon.chatfilter3.main.BadWord;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

public interface DictionaryManager {

    /**
     * Fetches and reads the dictionary file.
     *
     * @throws URISyntaxException If an error occurs
     * @throws IOException If an error occurs
     */
    void readFile() throws URISyntaxException, IOException;

    /**
     * Indexes all the dictionary words according the the given {@link BadWord}s.
     *
     * @param badWordList The {@link BadWord}s to index off of
     * @return An unmodifiable set of the whitelisted words
     */
    Set<String> indexWords(Set<BadWord> badWordList);

    /**
     * Gets all raw lines of the dictionary
     *
     * @return The lines of the dictionary
     */
    List<String> getDictionaryLines();
}
