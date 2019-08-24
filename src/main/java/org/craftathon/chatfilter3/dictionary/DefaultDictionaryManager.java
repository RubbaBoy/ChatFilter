package org.craftathon.chatfilter3.dictionary;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.craftathon.chatfilter3.main.BadWord;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultDictionaryManager implements DictionaryManager {

    public static final String DICTIONARY_URL = "https://raw.githubusercontent.com/first20hours/google-10000-english/master/google-10000-english.txt";

    private List<String> dictionaryLines = new ArrayList<>();

    @Override
    public void readFile() throws URISyntaxException, IOException {
        File file = getFilePath();

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
            dictionaryLines = IOUtils.readLines(new URL(DICTIONARY_URL).openStream(), Charset.defaultCharset());
            FileUtils.write(getFilePath(), getLines(dictionaryLines));
            return;
        }

        dictionaryLines = FileUtils.readLines(file, Charset.defaultCharset());
    }

    @Override
    public List<String> indexWords(List<BadWord> badWordList) {
        List<String> whitelist = new ArrayList<>();

        badWordList.stream().map(BadWord::getComparingString).forEach(badWord -> whitelist.addAll(dictionaryLines.stream().filter(line -> !line.equalsIgnoreCase(badWord) && line.contains(badWord) && !line.equalsIgnoreCase(badWord + "s")).collect(Collectors.toList())));

        return whitelist;
    }

    @Override
    public List<String> getDictionaryLines() {
        return dictionaryLines;
    }

    private File getFilePath() throws URISyntaxException {
        return new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath(), "dictionary" + File.separator + "Dictionary.txt");
    }

    private String getLines(List<String> lines) {
        StringBuilder stringBuilder = new StringBuilder();
        lines.forEach(line -> stringBuilder.append(line).append('\n'));
        return stringBuilder.toString();
    }

}
