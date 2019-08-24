package org.craftathon.chatfilter3.main;

import org.craftathon.chatfilter3.dictionary.DefaultDictionaryManager;
import org.craftathon.chatfilter3.dictionary.DictionaryManager;
import org.craftathon.chatfilter3.qobjects.QChar;
import org.craftathon.chatfilter3.qobjects.QString;
import org.junit.jupiter.api.BeforeAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChatFilterTest {

    private static Logger LOGGER = LoggerFactory.getLogger(ChatFilterTest.class);

    private static ChatFilter chatFilter;

    @BeforeAll
    public static void setUp() {
        chatFilter = new DefaultChatFilter();
        var blocked = new HashMap<>(DefaultChatFilter.getBlocked());
        var whitelist = new ArrayList<>(DefaultChatFilter.getWhitelisted());
        // Blocked and whitelist are to ensure that the necessary inputs are available during most tests, so the logic
        // of the code is working properly. Some edge cases may occur while all words are available, which is why they
        // are also added. It may be worth having two runs of tests: One as it is now, and one with just the words
        // necessary.
        blocked.putAll(Map.of(
                "fuck", 1,
                "a{ss}", 0,
                "s!ex", 0
        ));
        chatFilter.setBlockFullWord(false);
        chatFilter.setMaxNumberPercentage(75);
        whitelist.addAll(Arrays.asList("assault", "assist"));
        chatFilter.init(blocked, whitelist);
    }

    @Test
    public void checkQCharsOriginality() {
        var used = new ArrayList<Character>();
        var withoutSpaces = new ArrayList<>(chatFilter.getQuantumCharList());
        withoutSpaces.remove(withoutSpaces.size() - 1);

        for (QChar qChar : withoutSpaces) {
            for (char cha : qChar.getCharValues()) {
                cha = Character.toLowerCase(cha);
                boolean cont = used.contains(cha);

                if (cont) LOGGER.error("Duplicate character: \'{}\'", cha);

                assertFalse(cont);

                used.add(cha);
            }
        }
    }

    @Test
    public void wordReconstruction() {
        for (int i = 0; i < 10; i++) {
            String string = generateString(20);
            QString inputQString = new QString(chatFilter, string).stripRepeating();
            assertEquals(string, inputQString.reconstruct());
        }
    }

    @Test
    public void basicWordRecognition() {
        assertClean(chatFilter,
                "fuck", "****",
                "fuck test", "**** test",
                "test fuck", "test ****",
                "test fuck test", "test **** test",
                "test fuuuuuck test", "test ******** test",
                "test fffuuucccck test", "test *********** test");
    }

    @Test
    public void integratedWordRecognition() {
        chatFilter.setBlockFullWord(true);
        assertClean(chatFilter,
                "fuck", "****",
                "fucktest", "********",
                "testfuck", "********",
                "testfucktest", "************",
                "testfuuuuucktest", "****************",
                "test testfucktest test", "test ************ test",
                "test fucktest test", "test ******** test",
                "test testfuck test", "test ******** test");
        chatFilter.setBlockFullWord(false);
    }

    @Test
    public void duplicateCharacterPreservation() {
        assertClean(chatFilter,
                "ass", "***",
                "as", "as",
                "asss", "****",
                "test as test", "test as test",
                "test asssssss test", "test ******** test",
                "test a sssss test", "test ******* test");
    }

    @Test
    public void noCharacterSeparation() {
        assertClean(chatFilter,
                "sex", "***",
                "test s ex test", "test s ex test",
                "test seeex test", "test ***** test",
                "test s eeex test", "test s eeex test");
    }

    @Test
    public void numberThreshold() {
        assertClean(chatFilter,
                "ass", "***",
                "test 45 test", "test 45 test",
                "test 455 test", "test 455 test",
                "test 4sss test", "test **** test");
    }

    @Test
    public void wordWhitelist() {
        assertClean(chatFilter,
                "ass", "***",
                "assault", "assault",
                "test assist ass assault", "test assist *** assault",
                "assisttest ass asstest assault", "***isttest *** ***test assault");
    }

    @Test
    public void dictionaryDownload() {
        LOGGER.debug("Downloading/Reading dictionary...");
        DictionaryManager dictionaryManager = new DefaultDictionaryManager();
        try {
            dictionaryManager.readFile();
        } catch (URISyntaxException | IOException e) {
            LOGGER.error("Problem downloading/reading dictionary. Is the dictionary URL accessible? " + DefaultDictionaryManager.DICTIONARY_URL, e);
            e.printStackTrace();
        }

        LOGGER.debug("{} dictionary lines found.", dictionaryManager.getDictionaryLines().size());

        assertTrue(dictionaryManager.getDictionaryLines().size() > 0);
    }

    @Test
    public void specialCharacters() {
        assertClean(chatFilter,
                "./test", "./test",
                ".test", ".test",
                ":test", ":test",
                "%%%%test", "%%%%test",
                ". /test", ". /test");
    }

    @Test
    public void puncuationSpace() {
        assertClean(chatFilter,
                "fuck, it", "****, it",
                "fuck! 111", "****! 111",
                "fuck! 123", "****! 123"
        );
    }

    @Test
    public void randomStrings() {
        assertClean(chatFilter,
                "45 ass 455 a55 4ss 4ss ass 4ss grass", "45 *** 455 *** *** *** *** *** grass",
                "This is a fuu\uff55\uff55\uff55uckin  big asss message of some of the fuckin amazing things that the filter can accomplish ya piece of shit", "This is a *********in  big **** message of some of the ****in amazing things that the filter can accomplish ya piece of****t",
                "i don't like this", "i don't like this",
                "This bitch 4ss shit better work I swear to fucking god", "This ***** *** **** better work I swear to ****ing god",
                "assisttest ass asstest assault", "***isttest *** ***test assault"
        );
    }

    private static void assertClean(ChatFilter chatFilter, String... dirtyClean) {
        for (int i = 0; i < dirtyClean.length / 2; i += 2) {
            assertEquals(dirtyClean[i + 1], chatFilter.clean(dirtyClean[i]));
        }
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
