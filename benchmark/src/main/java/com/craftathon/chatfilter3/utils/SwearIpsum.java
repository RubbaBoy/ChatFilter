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

package com.craftathon.chatfilter3.utils;

import org.craftathon.chatfilter3.qobjects.QChar;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SwearIpsum {

    private List<String> originalLines;
    private static List<Integer> averageSwear = new ArrayList<>();

    public static void main(String[] args) throws IOException, URISyntaxException {
        var gen = new SwearIpsum();
        gen.init();

        gen.getLines(1000).forEach(System.out::println);

//        double average = IntStream.range(0, 40).mapToDouble(i -> gen.getLines(1000).stream().mapToInt(String::length).average().orElse(0)).average().orElse(0);
//        System.out.println("Average length: " + average);
//        System.out.println("Average swears: " + averageSwear.stream().mapToInt(i -> i).average().orElse(0));
    }

    public SwearIpsum init() {
        try {
            var input = new String(Files.readAllBytes(Paths.get(getClass().getResource("/lipsum.txt").toURI())))
                    .replaceAll("\\s", " ");
            originalLines = Arrays.stream(input.split("\\.")).collect(Collectors.toList());
            Collections.shuffle(originalLines);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }

        return this;
    }

    public List<String> getLines(int count) {
        return IntStream.range(0, count).mapToObj(i -> nextLine()).collect(Collectors.toUnmodifiableList());
    }

    public String nextLine() {
        return processLine(originalLines.get(ThreadLocalRandom.current().nextInt(originalLines.size() - 1)));
    }

    private String processLine(String line) {
        var index = new int[] {0};
        var words = Arrays.stream(line.trim().split("\\s+")).collect(Collectors.toMap(str -> index[0]++, str -> str));
        int quarter = (int) Math.ceil(words.size() / 4D);
        var adding = quarter >= 1 ? ThreadLocalRandom.current().nextInt(quarter) : 1;
        int swearCount = Math.min(words.size() - 1, quarter + adding);
        averageSwear.add(swearCount);

        var keys = new ArrayList<>(words.keySet());
        Collections.shuffle(keys);
        for (int i = 0; i < swearCount; i++) {
            var wordIndex = keys.get(i);
            var original = words.get(wordIndex);
            var swear = getSwear();

            if (original.length() > swear.length()) {
                int repeating = original.length() - swear.length();
                var swearChars = new ArrayList<Character>();
                for (char cha : swear.toCharArray()) swearChars.add(cha);

                for (int i1 = 0; i1 < repeating; i1++) {
                    var randomIndex = ThreadLocalRandom.current().nextInt(swearChars.size() - 1);
                    swearChars.add(randomIndex, swearChars.get(randomIndex));
                }

                words.put(wordIndex, mixUp(wordIndex, swearChars.stream().map(Object::toString).collect(Collectors.joining())));
            } else {
                words.put(wordIndex, mixUp(wordIndex, swear));
            }
        }

        return words.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).collect(Collectors.joining(" "));
    }

    private String mixUp(int index, String input) {
        var chars = new ArrayList<Character>();

        int starting = index == 0 ? 1 : 0;
        if (index == 0) chars.add(Character.toUpperCase(input.charAt(0)));
        for (int i = starting; i < input.length(); i++) {
            var charAt = input.charAt(i);
            getQChar(charAt).ifPresentOrElse(qChar -> {
                var values = qChar.getCharValues();
                chars.add(ThreadLocalRandom.current().nextInt(100) < 66 ? charAt : values.get(ThreadLocalRandom.current().nextInt(values.size() - 1)));
            }, () -> chars.add(charAt));
        }

        return chars.stream().map(Object::toString).collect(Collectors.joining());
    }

    private String getSwear() {
        return swears.get(ThreadLocalRandom.current().nextInt(swears.size() - 1));
    }

    private Optional<QChar> getQChar(Character character) {
        return qChars.stream().filter(qChar -> qChar.equalsIgnoreCase(character)).findFirst();
    }

    private List<QChar> qChars = Arrays.asList(
            new QChar(-1, '＠', '@', 'a', '4', '４', 'ａ', 'ä', 'á', 'â', 'å', 'ª', 'ã', 'ā', 'ằ', 'æ', 'ʌ', 'ὰ', 'ἂ', 'ἃ', 'ᾲ', 'ᾂ', 'ᾃ', 'ᴀ', 'ą', 'ḁ', 'ἀ', 'ἁ', 'ἄ', 'ἅ', 'ἆ', 'ἇ', 'ạ', 'ả', 'ấ', 'ầ', 'ẩ', 'ẫ', 'ậ', 'ắ', 'ẳ', 'ẵ', 'ặ', 'ẚ', 'ᾰ', 'ᾱ', 'ᾳ', 'ᾴ', 'ᾶ', 'ᾷ', 'ά', 'ᾀ', 'ᾁ', 'ᾄ', 'ᾅ', 'ᾆ', 'ᾇ', 'ⓐ', '➍', '➃', 'ᗩ'),
            new QChar(-1, '°', 'o', '0', '０', 'ｏ', 'ö', 'ó', 'ò', 'Ø', 'ô', '¤', 'ờ', 'ồ', 'ṑ', '्', '్', '්', '್', '്', 'ﾟ', '◌', '˚', '˳', 'ȍ', 'ὸ', 'ὂ', 'ὃ', 'œ', '•', 'ᴏ', 'ṍ', 'ṏ', 'ṓ', 'ọ', 'ỏ', 'ố', 'ổ', 'ỗ', 'ộ', 'ớ', 'ở', 'ỡ', 'ợ', 'ὀ', 'ὁ', 'ὄ', 'ὅ', 'ⓞ', 'ᗝ'),
            new QChar(-1, 'i', 'ｉ', '!', 'ï', 'í', 'ì', 'î', '1', '|', 'l', '│', '£', 'ｌ', '１', '┘', 'ī', 'ȉ', 'ὶ', 'ἲ', 'ἳ', 'ῒ', '¡', 'ɪ', 'ʟ', 'ḭ', 'ḯ', 'ỉ', 'ị', 'ἰ', 'ἱ', 'ἴ', 'ἵ', 'ἶ', 'ἷ', 'ῐ', 'ῑ', 'ΐ', 'ῖ', 'ῗ', 'ί', 'ḷ', 'ḹ', 'ḻ', 'ḽ', 'ⓘ', 'ⓛ', '➊', '➀', '⓵', 'ᓰ', 'ᒪ'),
            new QChar(-1, '3', '３', 'e', 'ｅ', 'ë', 'é', 'è', 'ê', 'ề', 'ḕ', 'ɚ', 'ȅ', 'ѐ', 'ὲ', 'ἒ', 'ἓ', '€', 'ᴇ', 'ḗ', 'ḙ', 'ḛ', 'ḝ', 'ẹ', 'ẻ', 'ẽ', 'ế', 'ể', 'ễ', 'ệ', 'ἐ', 'ἑ', 'ἔ', 'ἕ', 'έ', 'ɇ', 'ⓔ', '➌', '➂', '⓷', 'ℯ', 'ᙓ'),
            new QChar(-1, 'u', 'ｕ', 'ü', 'ú', 'ù', 'û', 'ū', 'ǜ', 'ừ', 'ȕ', 'ὺ', 'ὒ', 'ὓ', 'ῢ', 'ᴜ', 'ṳ', 'ṵ', 'ṷ', 'ṹ', 'ṻ', 'ụ', 'ủ', 'ứ', 'ử', 'ữ', 'ự', 'ὐ', 'ὑ', 'ὔ', 'ὕ', 'ὖ', 'ὗ', 'ύ', 'ῠ', 'ῡ', 'ΰ', 'ῦ', 'ῧ', 'ⓤ'),
            new QChar(-1, 'n', 'ｎ', 'ñ', 'ǹ', 'ѝ', 'ɴ', 'ṅ', 'ṇ', 'ṉ', 'ṋ', 'ⓝ', 'η', 'ﬡ', '⋒'),
            new QChar(-1, 'w', 'ｗ', 'ẁ', 'ὼ', 'ὢ', 'ὣ', 'ῲ', 'ᾢ', 'ᴡ', 'ẃ', 'ẅ', 'ẇ', 'ẉ', 'ẘ', 'ὠ', 'ὡ', 'ὤ', 'ὥ', 'ὦ', 'ὧ', 'ώ', 'ᾠ', 'ᾡ', 'ᾤ', 'ᾥ', 'ᾦ', 'ᾧ', 'ῳ', 'ῴ', 'ῶ', 'ῷ', 'ⓦ', 'ᙡ'),
            new QChar(-1, 'h', 'ｈ', '#', 'ὴ', 'ἢ', 'ἣ', 'ῂ', 'ᾒ', 'ᾓ', 'ʜ', 'ḣ', 'ḥ', 'ḧ', 'ḩ', 'ḫ', 'ẖ', 'ἠ', 'ῄ', 'ῆ', 'ῇ', 'ħ', 'ἡ', 'ἤ', 'ἥ', 'ἦ', 'ἧ', 'ᾐ', 'ᾑ', 'ᾔ', 'ᾕ', 'ᾖ', 'ᾗ', 'ή', 'ῃ', 'ⓗ', 'ℌ', '♄', 'ᖺ'),
            new QChar(-1, 'b', 'g', 'ｂ', 'ｇ', '８', '９', '６', '8', '9', '6', 'ß', 'ʙ', 'ɢ', 'ḃ', 'ḅ', 'ḇ', 'ḡ', 'ⓑ', 'ⓖ', '➏', '➒', '➑', '➅', '➈', '➇', '⓺', '⓽', '⓼', '♭', 'ℊ', 'ᕊ', 'ᘐ'),
            new QChar(-1, '$', '5', '５', 's', 'ｓ', '§', 'ṡ', 'ṣ', 'ṥ', 'ṧ', 'ṩ', 'ş', 'ⓢ', '➎', '➄', 'ᔕ', 'ᔓ'),
            new QChar(-1, 'p', 'ｐ', 'q', 'ｑ', 'þ', '¶', 'ᴘ', 'ǫ', 'ṕ', 'ṗ', 'ῥ', 'ῤ', 'ⓟ', 'ⓠ', '℘', 'ᕈ', 'ᕴ'),
            new QChar(-1, '¥', 'y', 'ｙ', 'ÿ', 'ý', 'ỳ', 'ȳ', 'ʏ', 'ẏ', 'ẙ', 'ỵ', 'ỷ', 'ỹ', 'ⓨ', 'ƴ'),
            new QChar(-1, 'd', 'ｄ', 'Ð', 'ᴅ', 'ḋ', 'ḍ', 'ḏ', 'ḑ', 'ḓ', 'ⓓ', 'ᕍ'),
            new QChar(-1, '©', 'c', 'ｃ', '¢', 'ç', 'ɔ', 'ᴄ', 'ć', 'ḉ', 'ⓒ', 'ᑕ'),
            new QChar(-1, '®', 'r', 'ｒ', 'ȑ', 'ʀ', 'ṙ', 'ṛ', 'ṝ', 'ṟ', 'ⓡ', 'ᖇ'),
            new QChar(-1, 't', 'ｔ', 'ᴛ', '┤', 'ṫ', 'ṭ', 'ṯ', 'ṱ', 'ẗ', 'ⓣ', '⊥'),
            new QChar(-1, 'v', 'ｖ', 'ѷ', 'ᴠ', 'ṽ', 'ṿ', 'ⓥ', '♥', 'Ⅴ'),
            new QChar(-1, '×', 'x', '*', 'ｘ', 'ẋ', 'ẍ', 'ⓧ', 'ϰ', 'ჯ'),
            new QChar(-1, 'm', 'ｍ', 'ᴍ', 'ḿ', 'ṁ', 'ṃ', 'ⓜ', 'ᙢ'),
            new QChar(-1, 'z', 'ｚ', 'ᴢ', 'ẑ', 'ẓ', 'ẕ', 'ⓩ'),
            new QChar(-1, 'ƒ', 'f', 'ｆ', 'ғ', 'ḟ', 'ⓕ', 'ℱ'),
            new QChar(-1, 'k', 'ｋ', 'ᴋ', 'ḱ', 'ḳ', 'ḵ', 'ⓚ'),
            new QChar(-1, 'j', 'ｊ', 'ᴊ', 'ⓙ', 'ʝ', 'ᒎ')
    );

    private List<String> swears = Arrays.asList(
            "anal",
            "autism",
            "autistic",
            "bastard",
            "biatch",
            "bitch",
            "blowjob",
            "boner",
            "butthole",
            "buttplug",
            "chink",
            "chode",
            "clitoris",
            "cock",
            "coon",
            "creampie",
            "damn",
            "dick",
            "dike",
            "dildo",
            "dipshit",
            "douche",
            "dyke",
            "ejaculate",
            "erection",
            "erotic",
            "fuck",
            "gay",
            "gringo",
            "grope",
            "hacker",
            "hacks",
            "haxor",
            "hitler",
            "hornie",
            "horny",
            "hump",
            "jerk",
            "jizz",
            "kunt",
            "lube",
            "masturbate",
            "nazi",
            "negro",
            "nigga",
            "nigger",
            "orgasm",
            "penis",
            "porn",
            "pube",
            "pussi",
            "pussy",
            "queer",
            "retard",
            "sex",
            "shit",
            "slut",
            "suicide",
            "testicles",
            "viagra",
            "whore",
            "ass",
            "anus",
            "arse",
            "balls",
            "boob",
            "breast",
            "cunt",
            "fag",
            "hoe",
            "homo",
            "muff",
            "kike",
            "kyke",
            "lesbo",
            "piss",
            "queaf",
            "schlong",
            "spic",
            "twat",
            "tard",
            "wank",
            "willy"
    );

}
