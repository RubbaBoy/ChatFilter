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

import org.craftathon.chatfilter3.dictionary.DefaultDictionaryManager;
import org.craftathon.chatfilter3.dictionary.DictionaryManager;
import org.craftathon.chatfilter3.qobjects.QChar;
import org.craftathon.chatfilter3.qobjects.QString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Map.entry;

public class DefaultChatFilter implements ChatFilter {

    private static Logger LOGGER = LoggerFactory.getLogger(DefaultChatFilter.class);

    private final QChar SPACE;
    private double maxNumberPercentage = 75; // If the percentage a word is numbers is above this percentage, it will not be blocked
    private boolean blockFullWord = true;
    private DictionaryManager dictionaryManager = new DefaultDictionaryManager();

    private Set<BadWord> badWords;
    private Set<String> whitelistedWords;

    public DefaultChatFilter() {
        this(null);
    }

    public DefaultChatFilter(List<QChar> quantumCharList) {
        if (quantumCharList != null) this.quantumCharList = quantumCharList;
        this.SPACE = new QChar(-1, spacingChars.toArray(new Character[0]));
        this.quantumCharList.add(SPACE);

        try {
            dictionaryManager.readFile();
        } catch (URISyntaxException | IOException e) {
            LOGGER.error("Error while reading dictionary file", e);
        }
    }

    @Override
    public void init() {
        init(blocked);
    }

    @Override
    public void init(Map<String, Integer> words) {
        init(words, whitelisted);
    }

    @Override
    public void init(Map<String, Integer> words, List<String> whitelisted) {
        LOGGER.debug("Initializing bad/whitelisted words...");
        final long start = System.currentTimeMillis();

        badWords = Collections.synchronizedSet(words.entrySet()
                .parallelStream()
                .map(entry -> new DefaultBadWord(this, entry.getKey(), entry.getValue()))
                .collect(Collectors.toUnmodifiableSet()));

        whitelistedWords = Collections.synchronizedSet(getWhitelistSubset(whitelisted, true));
        LOGGER.debug("[Whitelist] Adding non-default: {}", whitelistedWords);
        whitelistedWords.addAll(dictionaryManager.indexWords(badWords));

        var removing = getWhitelistSubset(whitelisted, false);
        whitelistedWords.removeAll(removing);

        LOGGER.debug("[Whitelist] Removing: {}", removing);

        LOGGER.debug("Completed init in {}ms", System.currentTimeMillis() - start);
    }

    private Set<String> getWhitelistSubset(List<String> whitelisted, boolean included) {
        return whitelisted.parallelStream().map(String::toLowerCase)
                .filter(word -> included != word.startsWith("-"))
                .map(word -> !included ? word.substring(1) : word)
                .collect(Collectors.toSet());
    }

    @Override
    public void setMaxNumberPercentage(double maxNumberPercentage) {
        this.maxNumberPercentage = maxNumberPercentage;
    }

    @Override
    public void setBlockFullWord(boolean blockFullWord) {
        this.blockFullWord = blockFullWord;
    }

    @Override
    public String clean(String input) {
        return cleanUnapplied(input).applyBlocks();
    }

    @Override
    public BlockWordQueue cleanUnapplied(String input) {
        final long start = System.currentTimeMillis();
        var inputQString = new QString(this, input).stripRepeating();

        var blockWordQueue = new DefaultBlockWordQueue(this, inputQString);

        badWords.parallelStream().forEach(badWord -> {
            int startingAt = 0;
            int adding = 0;
            for (int i = 0; i < inputQString.getIteratingLength(); i++) {
                var previous = inputQString.qCharAt(i - 1);
                var current = inputQString.qCharAt(i);
                var next = inputQString.qCharAt(i + 1);
                adding += current.getRepetition();

                if (isSpace(current)) {
                    if (badWord.getLetters() + badWord.getNumbers() + badWord.getSpaces() == 0 && isSpace(current)) {
                        badWord.resetTemporary();
                        startingAt = adding;
                        continue;
                    } else if (next != null && previous != null && previous.equalsIgnoreCase(next)) {
                        i++;
                        previous = inputQString.qCharAt(i - 1);
                        current = inputQString.qCharAt(i);
                        startingAt = adding;

                        badWord.resetTemporary();
                    }
                }

                if (badWord.allowedNext(previous, current)) {
                    if (!badWord.nextAvailable()) {
                        int total = badWord.getLetters() + badWord.getNumbers() + badWord.getSpaces();
                        int beginningOfWord = getBeginningOfWord(startingAt, input);
                        int endOfWord = getEndOfWord(startingAt + total, input);
                        String originalWord = input.substring(beginningOfWord, endOfWord).toLowerCase();
                        if (!whitelistedWords.contains(originalWord)) {
                            double percentage = badWord.getNumbers() == 0 ? 0 : badWord.getNumbers() / (double) total * 100D;

                            if (percentage < maxNumberPercentage) {
                                blockWordQueue.addWord(new SimpleBlockedWord(blockFullWord ? beginningOfWord : startingAt, blockFullWord ? endOfWord - beginningOfWord : total, percentage, originalWord, badWord));
                            }
                        }

                        badWord.resetTemporary();
                        startingAt = adding;
                    }
                } else {
                    badWord.resetTemporary();
                    startingAt = adding;
                }
            }
        });

        blockWordQueue.sort();
        blockWordQueue.removeOverlaps();
        blockWordQueue.setTime(System.currentTimeMillis() - start);
        return blockWordQueue;
    }

    private int getEndOfWord(int current, String input) {
        for (int i = current; i < input.length(); i++) {
            if (input.charAt(i) == ' ') return i;
        }

        return input.length();
    }

    private int getBeginningOfWord(int current, String input) {
        if (current >= input.length()) current = input.length() - 1;
        for (int i = current; i > 0; i--) {
            if (input.charAt(i) == ' ') return i + 1;
        }

        return 0;
    }

    @Override
    public QChar getQCharFor(Character character) {
        return quantumCharList.stream()
                .filter(qChar -> qChar.equalsIgnoreCase(character))
                .findFirst()
                .map(QChar::clone)
                .orElse(new QChar(-1, character));
    }

    @Override
    public boolean isSpace(QChar qChar) {
        return qChar == null || SPACE.equalsExact(qChar);
    }

    public static Map<String, Integer> getBlocked() {
        return blocked;
    }

    public static Map<String, Integer> getBlocked(Map<String, Integer> adding) {
        var blocked = new HashMap<>(getBlocked());
        blocked.putAll(adding);
        return blocked;
    }

    public static List<String> getWhitelisted() {
        return whitelisted;
    }

    public static List<String> getWhitelisted(List<String> adding) {
        var whitelisted = new ArrayList<>(getWhitelisted());
        whitelisted.addAll(adding);
        return whitelisted;
    }

    private static List<String> whitelisted = Arrays.asList(
            "-livesex",
            "-worldsex",
            "-dicke",
            "-fucking",
            "-fucked",
            "-porno",
            "-sexy",
            "-sexual",
            "-sexo",
            "-sexcam",
            "-essex",
            "-sussex",
            "-transexual",
            "-transsexual",
            "-transexuales",
            "-sexuality",
            "-sexually",
            "-erotica"
    );

    private static Map<String, Integer> blocked = Map.ofEntries(
            entry("an!al", 1),
            entry("autism", 1),
            entry("autistic", 1),
            entry("bastard", 1),
            entry("biatch", 1),
            entry("bitch", 1),
            entry("blowjob", 1),
            entry("boner", 1),
            entry("butthole", 1),
            entry("buttplug", 1),
            entry("chink", 1),
            entry("chode", 1),
            entry("clitoris", 1),
            entry("cock", 1),
            entry("c{oo}n", 1),
            entry("creampie", 1),
            entry("damn", 1),
            entry("dick", 1),
            entry("dike", 1),
            entry("dildo", 1),
            entry("dipshit", 1),
            entry("douche", 1),
            entry("dyke", 1),
            entry("ejaculate", 1),
            entry("erection", 1),
            entry("erotic", 1),
            entry("fuck", 1),
            entry("gay", 1),
            entry("gringo", 1),
            entry("grope", 1),
            entry("hacker", 1),
            entry("hacks", 1),
            entry("haxor", 1),
            entry("hitler", 1),
            entry("hornie", 1),
            entry("horny", 1),
            entry("hump", 1),
            entry("jerk", 1),
            entry("jizz", 1),
            entry("kunt", 1),
            entry("lube", 1),
            entry("masturbate", 1),
            entry("nazi", 1),
            entry("negro", 1),
            entry("nigga", 1),
            entry("nigger", 1),
            entry("orgasm", 1),
            entry("penis", 1),
            entry("porn", 1),
            entry("pube", 1),
            entry("pussi", 1),
            entry("pussy", 1),
            entry("queer", 1),
            entry("retard", 1),
            entry("s!ex", 1),
            entry("shit", 1),
            entry("slut", 1),
            entry("suicide", 1),
            entry("testicles", 1),
            entry("viagra", 1),
            entry("whore", 1),

            entry("a{ss}", 0),
            entry("anus", 0),
            entry("arse", 0),
            entry("balls", 0),
            entry("b{oo}b", 0),
            entry("breast", 0),
            entry("cunt", 0),
            entry("fag", 0),
            entry("hoe", 0),
            entry("ho!mo", 0),
            entry("muff", 0),
            entry("kike", 0),
            entry("kyke", 0),
            entry("lesbo", 0),
            entry("pi!{ss}", 0),
            entry("queaf", 0),
            entry("schlong", 0),
            entry("spic", 0),
            entry("twat", 0),
            entry("tard", 0),
            entry("wank", 0),
            entry("willy", 0)
    );

    private List<Character> spacingChars = Arrays.asList(
            ' ',
            '.',
            '-',
            '_',
            '`',
            '~',
            ',',
            '?',
            '!',
            ':',
            '/'
    );


    /*
     * All characters in each QChar will be, when compared, the same, but can be compared with any one of the values in the QChar original: https://rubbaboy.me/code/r1uhhjb
     */

    private List<QChar> quantumCharList = new ArrayList<>(Arrays.asList(
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
    ));


    @Override
    public List<QChar> getQuantumCharList() {
        return this.quantumCharList;
    }
}
