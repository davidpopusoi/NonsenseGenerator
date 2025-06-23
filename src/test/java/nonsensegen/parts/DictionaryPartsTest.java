package nonsensegen.parts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DictionaryPartsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryPartsTest.class);
    private DictionaryParts dictionaryParts;

    @BeforeEach
    void setUp() {
        Path testFile = Paths.get("src/test/resources/dictionary_test.txt");
        dictionaryParts = new DictionaryParts(testFile);

        assertNotNull(dictionaryParts.getDictionaryPath());
        assertTrue(Files.exists(dictionaryParts.getDictionaryPath()));

        // Force reload
        dictionaryParts.partMap.clear();
        dictionaryParts.fillParts();
    }

    @Test
    void testDictionary_LoadsWordsIntoCorrectCategories() {
        List<String> nouns = dictionaryParts.getNoun();
        assertNotNull(nouns);
        assertFalse(nouns.isEmpty(), "Nouns list should not be empty");

        List<String> verbs = dictionaryParts.getVerb();
        assertFalse(verbs.isEmpty(), "Verbs list should not be empty");
    }
}
