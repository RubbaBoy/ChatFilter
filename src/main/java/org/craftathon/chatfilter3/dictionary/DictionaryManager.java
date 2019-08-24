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
