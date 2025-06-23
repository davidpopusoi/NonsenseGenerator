package nonsensegen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryTest {

    private History history;

    @BeforeEach
    void setUp() {
        history = new History();
    }

    @Test
    void testSaveFinalToHistory_addsUniqueSentences() {
        history.saveFinalToHistory("Sentence 1");
        history.saveFinalToHistory("Sentence 2");

        List<String> saved = history.getHistory();
        assertEquals(2, saved.size());
        assertTrue(saved.contains("Sentence 1"));
        assertTrue(saved.contains("Sentence 2"));
    }

    @Test
    void testSaveFinalToHistory_doesNotAddDuplicates() {
        history.saveFinalToHistory("Hello world");
        history.saveFinalToHistory("Hello world");

        List<String> saved = history.getHistory();
        assertEquals(1, saved.size());
        assertEquals("Hello world", saved.get(0));
    }

    @Test
    void testSaveFinalToHistory_maintainsMaxRecentLimit() {
        for (int i = 1; i <= 25; i++) {
            history.saveFinalToHistory("Sentence " + i);
        }

        List<String> saved = history.getHistory();
        assertEquals(20, saved.size());
        assertFalse(saved.contains("Sentence 1")); // oldest removed
        assertTrue(saved.contains("Sentence 25")); // newest present
    }

    @Test
    void testGetHistory_returnsCorrectOrder() {
        history.saveFinalToHistory("First");
        history.saveFinalToHistory("Second");
        history.saveFinalToHistory("Third");

        List<String> saved = history.getHistory();
        assertEquals(List.of("First", "Second", "Third"), saved);
    }
}
