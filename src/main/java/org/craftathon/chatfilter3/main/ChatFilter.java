package org.craftathon.chatfilter3.main;

import org.craftathon.chatfilter3.dictionary.DictionaryManager;
import org.craftathon.chatfilter3.qobjects.QChar;
import org.craftathon.chatfilter3.qobjects.QString;
import org.craftathon.chatfilter3.utils.InlineLinkedHashMap;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

public class ChatFilter {

    private final QChar SPACE;
    private double maxNumberPercentage = 75; // If the percentage a word is numbers is above this percentage, it will not be blocked
    private List<Integer> times = new ArrayList<>();
    private boolean blockFullWord = true;
    private DictionaryManager dictionaryManager = new DictionaryManager();

    public ChatFilter() {
        this(null);
    }

    public ChatFilter(List<QChar> quantumCharList) {
        if (quantumCharList != null) this.quantumCharList = quantumCharList;
        this.SPACE = new QChar(-1, spacingChars.toArray(new Character[0]));
        this.quantumCharList.add(SPACE);

        try {
            dictionaryManager.readFile();
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        boolean benchmark = false;

        ChatFilter chatFilter = new ChatFilter(null);
        chatFilter.init();

        System.out.println("chatFilter.whitelisted = " + chatFilter.whitelisted);

//        String input = "45 ass 455 a55 4ss 4ss ass 4ss grass";
//        String input = "This is a fuuｕｕｕuckin  big asss message of some of the fuckin amazing things that the filter can accomplish ya piece of shit";
        String input = "i dont like this";
//        String input = "This bitch 4ss shit better work I swear to fucking god";
//        String input = "What the fuck lmao";

        if (!benchmark) {
            System.out.println("Cleaned:\n\t" + input + "\n\t" + chatFilter.clean(input, false));
            return;
        }


        // Warmup
        for (int i = 0; i < 10; i++) {
            chatFilter.clean(input, false);
        }

        for (int i = 0; i < 100; i++) {
            chatFilter.clean(input, true);
        }

        int sum = chatFilter.times.stream().mapToInt(Integer::intValue).sum();
        double average = sum / chatFilter.times.size();

        System.out.println("Cleaned:\n\t" + input + "\n\t" + chatFilter.clean(input, false));

        System.out.println("Average time for x1000 loops: " + average + "ms");
    }

    public void init() {
        init(this.blocked);
    }

    public void init(Map<String, Integer> words) {
        init(words, this.whitelisted);
    }

    public void init(Map<String, Integer> words, List<String> whitelisted) {
        System.out.println("Initializing bad/whitelisted words...");
        final long start = System.currentTimeMillis();

        badWords.clear();
        words.forEach((word, priority) -> badWords.add(new BadWord(this, word, priority)));

        System.out.println("[Whitelist] Adding: " + getWhitelistSubset(whitelisted, true));

        this.whitelisted = getWhitelistSubset(whitelisted, true);
        this.whitelisted.addAll(dictionaryManager.indexWords(new ArrayList<>(badWords)));
        this.whitelisted.removeAll(getWhitelistSubset(whitelisted, false));

        System.out.println("[Whitelist] Removing: " + getWhitelistSubset(whitelisted, false));

        System.out.println("Completed in " + (System.currentTimeMillis() - start) + "ms");
    }

    private List<String> getWhitelistSubset(List<String> whitelisted, boolean included) {
        return whitelisted.stream().map(String::toLowerCase).filter(word -> included != word.startsWith("-")).map(word -> !included ? word.substring(1) : word).collect(Collectors.toList());
    }

    public void setMaxNumberPercentage(double maxNumberPercentage) {
        this.maxNumberPercentage = maxNumberPercentage;
    }

    public void setBlockFullWord(boolean blockFullWord) {
        this.blockFullWord = blockFullWord;
    }

    public String clean(String input) {
        return clean(input, true);
    }

    public String clean(String input, boolean sendMessage) {
        final long start = System.currentTimeMillis();

        BlockWordQueue blockWordQueue = cleanUnapplied(input);

        String cleaned = blockWordQueue.applyBlocks();

        if (sendMessage) {
            times.add((int) (System.currentTimeMillis() - start));
        }

        System.out.println("Completed cleaning in " + (System.currentTimeMillis() - start) + "ms");

        return cleaned;
    }

    public BlockWordQueue cleanUnapplied(String input) {
        final long start = System.currentTimeMillis();
        QString inputQString = new QString(this, input).stripRepeating();

        BlockWordQueue blockWordQueue = new BlockWordQueue(this, inputQString);

        this.badWords.parallelStream().forEach(badWord -> {
            int startingAt = 0;
            int adding = 0;
            for (int i = 0; i < inputQString.getIteratingLength(); i++) {
                QChar previous = inputQString.qCharAt(i - 1);
                QChar current = inputQString.qCharAt(i);
                QChar next = inputQString.qCharAt(i + 1);
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
                        if (!this.whitelisted.contains(originalWord.toLowerCase())) {
                            double percentage = badWord.getNumbers() == 0 ? 0 : badWord.getNumbers() / (double) total * 100D;

                            if (percentage < maxNumberPercentage) {
                                blockWordQueue.addWord(new BlockedWord(blockFullWord ? beginningOfWord : startingAt, blockFullWord ? endOfWord - beginningOfWord : total, percentage, originalWord, badWord));
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

    public QChar getQCharFor(Character character) {
        for (QChar qChar : this.quantumCharList) {
            if (qChar.equalsIgnoreCase(character)) return qChar.clone();
        }

        return new QChar(-1, character);
    }

    public boolean isSpace(QChar qChar) {
        if (qChar == null) return true;
        return SPACE.equalsExact(qChar);
    }

    private Set<BadWord> badWords = new HashSet<>();

    private List<String> whitelisted = new ArrayList<>(Arrays.asList(
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
    ));

    private InlineLinkedHashMap<String, Integer> blocked = new InlineLinkedHashMap<>(
            "an!al", 1,
            "autism", 1,
            "autistic", 1,
            "bastard", 1,
            "biatch", 1,
            "bitch", 1,
            "blowjob", 1,
            "boner", 1,
            "butthole", 1,
            "buttplug", 1,
            "chink", 1,
            "chode", 1,
            "clitoris", 1,
            "cock", 1,
            "c{oo}n", 1,
            "creampie", 1,
            "damn", 1,
            "dick", 1,
            "dike", 1,
            "dildo", 1,
            "dipshit", 1,
            "douche", 1,
            "dyke", 1,
            "ejaculate", 1,
            "erection", 1,
            "erotic", 1,
            "fuck", 1,
            "gay", 1,
            "gringo", 1,
            "grope", 1,
            "hacker", 1,
            "hacks", 1,
            "haxor", 1,
            "hitler", 1,
            "hornie", 1,
            "horny", 1,
            "hump", 1,
            "jerk", 1,
            "jizz", 1,
            "kunt", 1,
            "lube", 1,
            "masturbate", 1,
            "nazi", 1,
            "negro", 1,
            "nigga", 1,
            "nigger", 1,
            "orgasm", 1,
            "penis", 1,
            "porn", 1,
            "pube", 1,
            "pussi", 1,
            "pussy", 1,
            "queer", 1,
            "retard", 1,
            "s!ex", 1,
            "shit", 1,
            "slut", 1,
            "suicide", 1,
            "testicles", 1,
            "viagra", 1,
            "whore", 1,

            "a{ss}", 0,
            "anus", 0,
            "arse", 0,
            "balls", 0,
            "b{oo}b", 0,
            "breast", 0,
            "cunt", 0,
            "fag", 0,
            "hoe", 0,
            "ho!mo", 0,
            "muff", 0,
            "kike", 0,
            "kyke", 0,
            "lesbo", 0,
            "pi!{ss}", 0,
            "queaf", 0,
            "schlong", 0,
            "spic", 0,
            "twat", 0,
            "tard", 0,
            "wank", 0,
            "willy", 0
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


    public List<QChar> getQuantumCharList() {
        return this.quantumCharList;
    }
}
