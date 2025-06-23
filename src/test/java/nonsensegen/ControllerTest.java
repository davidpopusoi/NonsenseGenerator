package nonsensegen;

import com.google.cloud.language.v1.AnalyzeSyntaxResponse;
import com.google.cloud.language.v1.ClassificationCategory;
import com.google.cloud.language.v1.ModerateTextResponse;
import nonsensegen.parts.DictionaryParts;
import nonsensegen.parts.InputParts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ControllerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllerTest.class);
    private Controller controller;
    private GoogleNlpService googleNlpService;
    private InputParts inputParts;
    private DictionaryParts dictionaryParts;
    private Template template;
    private History history;

    @BeforeEach
    public void setUp() {
        googleNlpService = mock(GoogleNlpService.class);
        inputParts = mock(InputParts.class);
        dictionaryParts = mock(DictionaryParts.class);
        template = mock(Template.class);
        history = mock(History.class);

        controller = new Controller();
        controller.googleNlpService = googleNlpService;
        controller.inputParts = inputParts;
        controller.dictionaryParts = dictionaryParts;
        controller.template = template;
        controller.history = history;
    }

    @Test
    public void testAnalyzeSentence_validSentence() {
        String input = "The dog runs.";

        // Mock NLP response
        AnalyzeSyntaxResponse syntaxResponse = TestHelpers.createValidAnalyzeSyntaxResponse("en");
        ModerateTextResponse moderationResponse = TestHelpers.createModerateTextResponse();

        when(googleNlpService.analyzeSyntax(input)).thenReturn(syntaxResponse);
        when(googleNlpService.moderateText(input)).thenReturn(moderationResponse);
        when(inputParts.getTabellaCategorie()).thenReturn("TAB");

        Controller.ControllerResponse cr = controller.analyzeSentence(input);

        // TODO: sistemare

        //assert result.contains("dog");
        //assert result.contains("runs");
        //assert result.contains("TAB");

        //LOGGER.info("RESULT: " + result);
    }

    @Test
    public void testGenerateTemplate_callsTemplateAndSavesToHistory() {
        // Avoids calling the real Template, because we only care that it was called correctly
        when(template.getTemplate(dictionaryParts, inputParts)).thenReturn("The cursed tooth shoutings like a rowdy mirror to awaken the door from next codes");

        String result = controller.generateTemplate();

        // Checks that the mocked History object had its method saveFinalToHistory() called with the correct value
        verify(history).saveFinalToHistory("The cursed tooth shoutings like a rowdy mirror to awaken the door from next codes");
        assertEquals("The cursed tooth shoutings like a rowdy mirror to awaken the door from next codes", result);
    }

    @Test
    public void testAnalyzeSentence_englishTrue() {
        AnalyzeSyntaxResponse response = TestHelpers.createValidAnalyzeSyntaxResponse("en");
        when(googleNlpService.analyzeSyntax(any())).thenReturn(response);

        assertTrue(controller.isEnglishSentence("The dog runs."));
    }

    @Test
    public void testAnalyzeSentence_englishFalse() {
        AnalyzeSyntaxResponse response = TestHelpers.createValidAnalyzeSyntaxResponse("it");
        when(googleNlpService.analyzeSyntax(any())).thenReturn(response);

        assertFalse(controller.isEnglishSentence("Il cane corre."));
    }

    /**
     * Tests that the provided "sentence" has foreign elements when special characters are used
     */
    @Test
    public void testHasForeign_true() {
        AnalyzeSyntaxResponse response = TestHelpers.createMixedResponse("dog", "gâteau %^$#@");

        assertTrue(controller.hasForeign(response));
    }

    /**
     * Tests that the provided "sentence" doesn't have foreign elements when special characters aren't used
     */
    @Test
    public void testHasForeign_false() {
        AnalyzeSyntaxResponse response = TestHelpers.createValidAnalyzeSyntaxResponse("en");

        assertFalse(controller.hasForeign(response));
    }

    @Test
    public void testCorrectnessScore() {
        AnalyzeSyntaxResponse response = TestHelpers.createValidAnalyzeSyntaxResponse("en");

        double score = Controller.calculateCorrectnessScore(response);

        assertEquals(1.0, score);
        assertTrue(controller.isValidSentenceFlexible(response));
    }

    @Test
    public void testCorrectnessScore_imperativeVerbOnly() {
        AnalyzeSyntaxResponse response = TestHelpers.createImperativeResponse();

        double score = Controller.calculateCorrectnessScore(response);

        assertEquals(1.0, score);
        assertTrue(controller.isValidSentenceFlexible(response));
    }

    @Test
    public void testAnalyzeModeration_withCategories() {
        ClassificationCategory category = ClassificationCategory.newBuilder()
                .setName("Hate")
                .setConfidence(0.80f)
                .build();

        ModerateTextResponse response = ModerateTextResponse.newBuilder()
                .addModerationCategories(category)
                .build();

        String result = controller.analyzeModeration(response);

        assertTrue(result.contains("Hate"));
        assertTrue(result.contains("80.00%"));
    }

    @Test
    public void testAnalyzeModeration_empty() {
        ModerateTextResponse response = ModerateTextResponse.newBuilder().build();

        String result = controller.analyzeModeration(response);

        assertEquals("", result);
    }
}
