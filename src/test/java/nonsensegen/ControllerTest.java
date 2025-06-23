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

    private Controller controller;
    private GoogleNlpService googleNlpService;
    private InputParts inputParts;
    private DictionaryParts dictionaryParts;
    private Template template;
    private History history;

    @BeforeEach
    public void setup() {
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

        // Creates a mock of the Google NLP response
        AnalyzeSyntaxResponse syntaxResponse = TestHelpers.createValidAnalyzeSyntaxResponse("en");
        ModerateTextResponse moderationResponse = TestHelpers.createModerateTextResponse();

        when(googleNlpService.analyzeSyntax(input)).thenReturn(syntaxResponse);
        when(googleNlpService.moderateText(input)).thenReturn(moderationResponse);

        Controller.ControllerResponse cr = controller.analyzeSentence(input);

        assert cr.isValid;
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
        assertTrue(controller.isEnglishSentence(response));
    }

    @Test
    public void testAnalyzeSentence_englishFalse() {
        AnalyzeSyntaxResponse response = TestHelpers.createValidAnalyzeSyntaxResponse("it");
        assertFalse(controller.isEnglishSentence(response));
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
}
