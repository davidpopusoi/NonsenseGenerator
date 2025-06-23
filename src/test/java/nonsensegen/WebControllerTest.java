package nonsensegen;

import com.google.cloud.language.v1.*;
import nonsensegen.parts.InputParts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebController.class)
public class WebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Controller controller;

    private MockHttpSession session = new MockHttpSession();

    /**
     * Tests the default view
     */
    @Test
    void home_ReturnsIndexWithDefaultMessage() throws Exception {
        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("generated", "Click the button to generate a sentence."));
    }

    /**
     * Tests the GET /analyze, which redirects to home
     */
    @Test
    void redirectAnalyzeGet_RedirectsToHome() throws Exception {
        mockMvc.perform(get("/analyze"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    /**
     * Tests when the user accepts to have his sentence analysed
     */
    @Test
    void analyze_RequiresConfirmation_WhenNoConfirmationParam() throws Exception {
        // When POST without confirmation
        mockMvc.perform(post("/analyze")
                        .param("sentence", "Test sentence"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("sentence", "Test sentence"))
                .andExpect(model().attribute("showConfirmation", true));
    }

    /**
     * Tests when the user declines to have his sentence analysed
     */
    @Test
    void analyze_RedirectsHome_WhenUserDeclines() throws Exception {
        mockMvc.perform(post("/analyze")
                        .param("sentence", "Any sentence")
                        .param("confirmed", "no"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    /**
     * Tests a successful analysis with a valid response
     */
    @Test
    void analyze_ProcessesSentence_WhenUserConfirms() throws Exception {
        Token testToken = Token.newBuilder()
                .setText(TextSpan.newBuilder().setContent("dog").build())
                .setPartOfSpeech(PartOfSpeech.newBuilder().setTag(PartOfSpeech.Tag.NOUN).build())
                .build();

        // Create a minimal AnalyzeSyntaxResponse
        AnalyzeSyntaxResponse syntaxResponse = AnalyzeSyntaxResponse.newBuilder()
                .addTokens(testToken)
                .build();

        // Create valid response
        Controller.ControllerResponse mockResponse = new Controller.ControllerResponse(syntaxResponse);
        mockResponse.isEnglishSentence = true;
        mockResponse.hasForeign = false;
        mockResponse.isValid = true;
        mockResponse.moderateTextResponse = null;

        when(controller.analyzeSentence(anyString())).thenReturn(mockResponse);

        // Creates and configure a real InputParts object
        InputParts inputParts = new InputParts();
        inputParts.getInvalid().add("invalidWord");

        // Sets the inputParts directly on the controller mock
        ReflectionTestUtils.setField(controller, "inputParts", inputParts);

        // Execute POST with confirmation
        mockMvc.perform(post("/analyze")
                        .param("sentence", "Valid sentence")
                        .param("confirmed", "yes"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("output", "showDownload", "valid"))
                .andExpect(model().attribute("invalid", true))
                .andExpect(model().attribute("invalidWords", List.of("invalidWord")))
                .andExpect(model().attribute("categories", inputParts.getPartMap()))
                .andExpect(model().attributeDoesNotExist("errorFound"));
    }

    /**
     * Tests a sentence which is not in English
     */
    @Test
    void analyze_ShowsError_WhenNonEnglish() throws Exception {
        Controller.ControllerResponse mockResponse = new Controller.ControllerResponse(
                AnalyzeSyntaxResponse.getDefaultInstance());
        mockResponse.isEnglishSentence = false;

        when(controller.analyzeSentence(anyString())).thenReturn(mockResponse);

        mockMvc.perform(post("/analyze")
                        .param("sentence", "Texto en español")
                        .param("confirmed", "yes"))
                .andExpect(model().attribute("errorFound",
                        "Sorry, at the moment only the English language is supported"));
    }

    /**
     * Tests a sentence that contains foreign elements/characters
     */
    @Test
    void analyze_ShowsError_WhenForeignCharacters() throws Exception {
        Controller.ControllerResponse mockResponse = new Controller.ControllerResponse(
                AnalyzeSyntaxResponse.getDefaultInstance());
        mockResponse.isEnglishSentence = true;
        mockResponse.hasForeign = true;

        when(controller.analyzeSentence(anyString())).thenReturn(mockResponse);

        mockMvc.perform(post("/analyze")
                        .param("sentence", "Word with spéciål chârs")
                        .param("confirmed", "yes"))
                .andExpect(model().attribute("errorFound",
                        "Be sure you're not using any special characters"));
    }

    /**
     * Tests a sentence that has an invalid structure
     */
    @Test
    void analyze_ShowsError_WhenInvalidStructure() throws Exception {
        Controller.ControllerResponse mockResponse = new Controller.ControllerResponse(
                AnalyzeSyntaxResponse.getDefaultInstance());
        mockResponse.isEnglishSentence = true;
        mockResponse.isValid = false;

        // Configures the mock controller to return a prepared response for any analyzeSentence() call
        when(controller.analyzeSentence(anyString())).thenReturn(mockResponse);

        mockMvc.perform(post("/analyze")
                        .param("sentence", "Incomplete sentence")
                        .param("confirmed", "yes"))
                .andExpect(model().attribute("errorFound",
                        "The provided sentence doesn't have a correct structure"));
    }

    /**
     * Tests the template generation call that is done through GET /api/sentence
     */
    @Test
    void getNewSentence_ReturnsGeneratedSentence() throws Exception {
        // Configures the mock controller to return a prepared response for any generateTemplate() call
        when(controller.generateTemplate()).thenReturn("Mock generated sentence");

        mockMvc.perform(get("/api/sentence"))
                .andExpect(status().isOk())
                .andExpect(content().string("Mock generated sentence"));
    }

    /**
     * Tests downloading the recent history of generated sentences
     */
    @Test
    void download_ReturnsFile_WhenHistoryExists() throws Exception {
        // Setup history
        History history = new History();
        // Add some elements to the History object
        history.getHistory().add("Sentence 1");
        history.getHistory().add("Sentence 2");
        // Feed the History object to the controller
        ReflectionTestUtils.setField(controller, "history", history);

        // Attempt downloading the history as a text file
        MvcResult result = mockMvc.perform(get("/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"generated.txt\""))
                .andReturn();

        // Parse the result and get the data from it
        String content = result.getResponse().getContentAsString();
        assertEquals("Sentence 1\nSentence 2", content);
    }

    /**
     * Tests downloading the recent history of generated sentences when the user generated no sentences
     */
    @Test
    void download_ReturnsEmptyFile_WhenNoHistory() throws Exception {
        // Setup an empty history
        History history = new History();
        // Feed the History object to the controller
        ReflectionTestUtils.setField(controller, "history", history);

        // Attempt downloading the history as a text file
        MvcResult result = mockMvc.perform(get("/download"))
                .andExpect(status().isOk())
                .andReturn();

        // Parse the result and get the data from it
        String content = result.getResponse().getContentAsString();
        assertEquals("", content);
    }

    /**
     * Tests the moderation analysis
     */
    @Test
    void analyze_ShowsModerationFlags_WhenContentIsFlagged() throws Exception {
        // Create an empty InputParts
        InputParts testInputParts = new InputParts();

        // Inject it into the controller
        ReflectionTestUtils.setField(controller, "inputParts", testInputParts);

        // Setup response
        AnalyzeSyntaxResponse syntaxResponse = AnalyzeSyntaxResponse.newBuilder().build();
        Controller.ControllerResponse mockResponse = new Controller.ControllerResponse(syntaxResponse);
        mockResponse.isEnglishSentence = true;
        mockResponse.isValid = true;

        // Create a moderation category
        ClassificationCategory category = ClassificationCategory.newBuilder()
                .setName("Insult")
                .setConfidence(0.80f)
                .build();

        // Build the moderation response
        ModerateTextResponse moderationResponse = ModerateTextResponse.newBuilder()
                .addModerationCategories(category)
                .build();

        // Feed the created response to the mock ControllerResponse
        mockResponse.moderateTextResponse = moderationResponse;

        // Stub the instruction, configuring the mock controller to return a prepared response for any analyzeSentence() call
        when(controller.analyzeSentence(anyString())).thenReturn(mockResponse);

        // Execute and verify
        mockMvc.perform(post("/analyze")
                        .param("sentence", "Flagged content")
                        .param("confirmed", "yes"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("moderationFlags"))
                .andExpect(model().attribute("moderationFlags", hasSize(1)));
    }

    /**
     * Tests when the submitted sentence is null
     */
    @Test
    void analyze_HandlesNullResponse() throws Exception {
        // Configures the mock controller to return a prepared response for any analyzeSentence() call
        when(controller.analyzeSentence(anyString())).thenReturn(null);

        mockMvc.perform(post("/analyze")
                        .param("sentence", "test")
                        .param("confirmed", "yes"))
                .andExpect(model().attributeExists("output"))
                .andExpect(model().attributeDoesNotExist("valid"));
    }
}