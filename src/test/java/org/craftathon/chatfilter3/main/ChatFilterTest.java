package org.craftathon.chatfilter3.main;

import org.craftathon.chatfilter3.dictionary.DictionaryManager;
import org.craftathon.chatfilter3.qobjects.QChar;
import org.craftathon.chatfilter3.qobjects.QString;
import org.craftathon.chatfilter3.utils.InlineLinkedHashMap;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChatFilterTest {

    @Test
    public void checkQCharsOriginality() {
        ChatFilter chatFilter = new ChatFilter();
        chatFilter.init();
        List<Character> used = new ArrayList<>();

        List<QChar> withoutSpaces = new ArrayList<>(chatFilter.getQuantumCharList());
        withoutSpaces.remove(withoutSpaces.size() - 1);

        for (QChar qChar : withoutSpaces) {
            for (char cha : qChar.getCharValues()) {
                cha = Character.toLowerCase(cha);
                boolean cont = used.contains(cha);

                if (cont) System.out.println("Duplicate character: \'" + cha + "\'");

                assertFalse(cont);

                used.add(cha);
            }
        }
    }

    @Test
    public void wordReconstruction() {
        ChatFilter chatFilter = new ChatFilter();
        chatFilter.init();

        for (int i = 0; i < 100; i++) {
            String string = generateString(100);
            QString inputQString = new QString(chatFilter, string).stripRepeating();
            assertEquals(string, inputQString.reconstruct());
        }
    }

    @Test
    public void basicWordRecognition() {
        ChatFilter chatFilter = new ChatFilter();
        chatFilter.init(Collections.singletonMap("fuck", 1));

        new InlineLinkedHashMap<String, String>(
                "fuck", "****",
                "fuck test", "**** test",
                "test fuck", "test ****",
                "test fuck test", "test **** test",
                "test fuuuuuck test", "test ******** test",
                "test fffuuucccck test", "test *********** test").forEach((input, expected) -> assertEquals(expected, chatFilter.clean(input)));
    }

    @Test
    public void integratedWordRecognition() {
        ChatFilter chatFilter = new ChatFilter();
        chatFilter.init(Collections.singletonMap("fuck", 1));

        new InlineLinkedHashMap<String, String>(
                "fuck", "****",
                "fucktest", "********",
                "testfuck", "********",
                "testfucktest", "************",
                "testfuuuuucktest", "****************",
                "test testfucktest test", "test ************ test",
                "test fucktest test", "test ******** test",
                "test testfuck test", "test ******** test").forEach((input, expected) -> assertEquals(expected, chatFilter.clean(input)));
    }

    @Test
    public void duplicateCharacterPreservation() {
        ChatFilter chatFilter = new ChatFilter();
        chatFilter.init(Collections.singletonMap("a{ss}", 0));

        new InlineLinkedHashMap<String, String>(
                "ass", "***",
                "as", "as",
                "asss", "****",
                "test as test", "test as test",
                "test asssssss test", "test ******** test",
                "test a sssss test", "test ******* test").forEach((input, expected) -> assertEquals(expected, chatFilter.clean(input)));
    }

    @Test
    public void noCharacterSeparation() {
        ChatFilter chatFilter = new ChatFilter();
        chatFilter.init(Collections.singletonMap("s!ex", 0));

        new InlineLinkedHashMap<String, String>(
                "sex", "***",
                "test s ex test", "test s ex test",
                "test seeex test", "test ***** test",
                "test s eeex test", "test s eeex test").forEach((input, expected) -> assertEquals(expected, chatFilter.clean(input)));
    }

    @Test
    public void numberThreshold() {
        ChatFilter chatFilter = new ChatFilter();
        chatFilter.init(Collections.singletonMap("a{ss}", 0));
        chatFilter.setMaxNumberPercentage(75);

        new InlineLinkedHashMap<String, String>(
                "ass", "***",
                "test 45 test", "test 45 test",
                "test 455 test", "test 455 test",
                "test 4sss test", "test **** test").forEach((input, expected) -> assertEquals(expected, chatFilter.clean(input)));
    }

    @Test
    public void wordWhitelist() {
        ChatFilter chatFilter = new ChatFilter();
        chatFilter.init(Collections.singletonMap("a{ss}", 0), Arrays.asList("assault", "assist"));
        chatFilter.setBlockFullWord(false);

        new InlineLinkedHashMap<String, String>(
                "ass", "***",
                "assault", "assault",
                "test assist ass assault", "test assist *** assault",
                "assisttest ass asstest assault", "***isttest *** ***test assault").forEach((input, expected) -> assertEquals(expected, chatFilter.clean(input)));
    }

    @Test
    public void dictionaryDownload() {
        System.out.println("Downloading/Reading dictionary...");
        DictionaryManager dictionaryManager = new DictionaryManager();
        try {
            dictionaryManager.readFile();
        } catch (URISyntaxException | IOException e) {
            System.out.println("Problem downloading/reading dictionary. Is the dictionary URL accessible? " + DictionaryManager.DICTIONARY_URL);
            e.printStackTrace();
        }

        System.out.println(dictionaryManager.getDictionaryLines().size() + " dictionary lines found.");

        assertTrue(dictionaryManager.getDictionaryLines().size() > 0);
    }

    @Test
    public void specialCharacters() {
        ChatFilter chatFilter = new ChatFilter();
        chatFilter.init(Collections.singletonMap("a{ss}", 0));
        chatFilter.setBlockFullWord(false);

        new InlineLinkedHashMap<String, String>(
                "./test", "./test",
                ".test", ".test",
                ":test", ":test",
                "%%%%test", "%%%%test",
                ". /test", ". /test").forEach((input, expected) -> assertEquals(expected, chatFilter.clean(input)));
    }

    @Test
    public void puncuationSpace() {
        ChatFilter chatFilter = new ChatFilter();
        chatFilter.init(Collections.singletonMap("fuck", 1));
        chatFilter.setBlockFullWord(false);

        new InlineLinkedHashMap<String, String>(
                "fuck, it", "****, it",
                "fuck! 111", "****! 111",
                "fuck! 123", "****! 123").forEach((input, expected) -> assertEquals(expected, chatFilter.clean(input)));
    }

    private String generateString(int length) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char cha = (char) (ThreadLocalRandom.current().nextInt('z' - 'a' + 1) + 'a');
            if (ThreadLocalRandom.current().nextBoolean()) cha = Character.toUpperCase(cha);
            stringBuilder.append(cha);
        }

        return stringBuilder.toString();
    }
}