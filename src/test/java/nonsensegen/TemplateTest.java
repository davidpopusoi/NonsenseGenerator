package nonsensegen;

import nonsensegen.parts.DictionaryParts;
import nonsensegen.parts.InputParts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TemplateTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateTest.class);
    private Template template;
    private DictionaryParts mockDict;
    private InputParts mockInput;

    /**
     * Fills the mock Parts objects with words
     */
    @BeforeEach
    void setUp() {
        template = new Template();

        mockDict = new DictionaryParts() {
            @Override
            protected void fillParts() {
                partMap.put("noun", List.of("cat", "tree"));
                partMap.put("verb", List.of("jumps", "runs"));
                partMap.put("adjective", List.of("fast", "green"));
            }
        };

        mockInput = new InputParts() {
            @Override
            public void fillParts() {
                partMap.put("noun", List.of("dog"));
                partMap.put("verb", List.of("walks"));
                partMap.put("adjective", List.of("slow"));
            }
        };
    }

    /**
     * Tests that the generated template gets all its placeholders replaced with words from the correct categories
     */
    @Test
    void testFillTemplate_fillsAllPlaceholders() {
        String raw = "The [noun] [verb] the [noun] [adjective].";
        String result = template.fillTemplate(raw, mockDict, mockInput);

        // Shouldn't contain any placeholders
        assertFalse(result.contains("[noun]"));
        assertFalse(result.contains("[verb]"));
        assertFalse(result.contains("[adjective]"));

        // Should contain actual words from either mockDict or mockInput
        assertTrue(result.matches("The \\w+ \\w+ the \\w+ \\w+\\."));

        LOGGER.info("RESULT: " + result);
    }

    /**
     * Tests that the generated template gets its placeholders replaced with [N/A] when there are no words available
     */
    @Test
    void testFillTemplate_whenNoWordsAvailable() {
        DictionaryParts emptyDict = new DictionaryParts() {
            @Override
            public List<String> getCategoryOrCreate(String category) {
                return List.of();
            }
        };
        InputParts emptyInput = new InputParts() {
            @Override
            public List<String> getCategoryOrCreate(String category) {
                return List.of();
            }
        };

        String raw = "This is a [nonexistent] test.";
        String result = template.fillTemplate(raw, emptyDict, emptyInput);
        assertTrue(result.contains("[N/A]"));

        LOGGER.info("RESULT: " + result);
    }

    /**
     * Tests that the generated template gets its placeholders replaced with [N/A] when an unknown category is specified
     */
    @Test
    void testFillTemplate_handlesUnknownCategoriesGracefully() {
        String raw = "This [madeup] sentence has [noun] and [verb].";
        String result = template.fillTemplate(raw, mockDict, mockInput);
        assertTrue(result.contains("[N/A]") || result.matches("This \\w+ sentence has \\w+ and \\w+\\."));

        LOGGER.info("RESULT: " + result);
    }

    /**
     * Tests that the generated template gets filled even when DictionaryParts has no words
     */
    @Test
    void testFillTemplate_fallbackToInputPartsWhenDictionaryIsEmpty() {
        DictionaryParts emptyDict = new DictionaryParts() {
            @Override
            protected void fillParts() {
                // Empty
            }
        };

        // InputParts has usable data
        InputParts inputOnly = new InputParts() {
            @Override
            public void fillParts() {
                partMap.put("noun", List.of("alien"));
                partMap.put("verb", List.of("screams"));
            }
        };

        String raw = "The [noun] [verb].";
        String result = template.fillTemplate(raw, emptyDict, inputOnly);

        assertTrue(result.contains("alien") || result.contains("screams"));
        assertFalse(result.contains("[noun]"));
        assertFalse(result.contains("[verb]"));

        LOGGER.info("RESULT: " + result);
    }

    /**
     * Tests that the generated template gets filled even when InputParts has no words
     */
    @Test
    void testFillTemplate_fallbackToDictionaryPartsWhenInputIsEmpty() {
        DictionaryParts dictOnly = new DictionaryParts() {
            @Override
            protected void fillParts() {
                partMap.put("noun", List.of("robot"));
                partMap.put("verb", List.of("flies"));
            }
        };

        InputParts emptyInput = new InputParts() {
            @Override
            public void fillParts() {
                // Empty
            }
        };

        String raw = "The [noun] [verb].";
        String result = template.fillTemplate(raw, dictOnly, emptyInput);

        assertTrue(result.contains("robot") || result.contains("flies"));
        assertFalse(result.contains("[noun]"));
        assertFalse(result.contains("[verb]"));

        LOGGER.info("RESULT: " + result);
    }
}
