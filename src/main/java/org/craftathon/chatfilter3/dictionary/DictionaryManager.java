package org.craftathon.chatfilter3.dictionary;

import org.craftathon.chatfilter3.main.BadWord;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public interface DictionaryManager {

    /**
     * Fetches and reads the dictionary file.
     *
     * @throws URISyntaxException If an error occurs
     * @throws IOException If an error occurs
     */
    void readFile() throws URISyntaxException, IOException;

    /**
     *
     *
     * @param badWordList
     * @return
     */
    List<String> indexWords(List<BadWord> badWordList);

    List<String> getDictionaryLines();
}
