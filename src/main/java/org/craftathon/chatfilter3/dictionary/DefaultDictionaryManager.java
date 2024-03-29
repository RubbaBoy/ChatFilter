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
import java.util.Set;
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
    public Set<String> indexWords(Set<BadWord> badWordList) {
        return badWordList.stream()
                .map(BadWord::getComparingString)
                .flatMap(badWord ->
                        dictionaryLines.stream()
                                .filter(line -> !line.equalsIgnoreCase(badWord) && line.contains(badWord) && !line.equalsIgnoreCase(badWord + "s")))
                .collect(Collectors.toUnmodifiableSet());
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
