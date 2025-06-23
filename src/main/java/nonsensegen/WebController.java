package nonsensegen;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring MVC Controller that handles all web requests for the application: page navigation,
 * sentence analysis workflow and file downloads
 */
@RequiredArgsConstructor  // Lombok: creates constructor with required arguments
@Controller
public class WebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(nonsensegen.WebController.class);

    // Our controller
    private final nonsensegen.Controller controller;

    /**
     * Home page
     */
    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        model.addAttribute("generated", "Click the button to generate a sentence.");

        return "index";
    }

    /**
     * Redirects GET /analyze requests to home page, which prevents direct access to the analysis endpoint
     */
    @GetMapping("/analyze")
    public String redirectAnalyzeGet() {
        return "redirect:/";
    }

    /**
     * Analyzes user-submitted sentences
     *
     * Workflow:
     * 1. User submits sentence
     * 2. System shows confirmation page
     * 3. On confirmation, processes sentence
     * 4. Returns analysis results or errors
     */
    @PostMapping("/analyze")
    public String analyze(@RequestParam("sentence") String sentence,
                          @RequestParam(value = "confirmed", required = false) String confirmed,
                          Model model) {

        // Preserve the sentence, which will be displayed at most times
        model.addAttribute("sentence", sentence);

        // Show confirmation page if not confirmed
        if (confirmed == null) {
            model.addAttribute("showConfirmation", true);
            return "index";
        }

        // Continue if the user agrees to analyse the sentence
        if ("yes".equalsIgnoreCase(confirmed)) {

            // Get the response from calling analyzeSentence(), which interacts with the Google API
            nonsensegen.Controller.ControllerResponse result = controller.analyzeSentence(sentence);

            // Now that we have a response, we can mark "output" as true, meaning that we can show to
            // the user some results in the UI
            model.addAttribute("output", true);
            // We can also show the option to download the history
            model.addAttribute("showDownload", true);

            // Make sure the response isn't null
            if (result != null) {

                // Sentence checks to warn the user and prompt him to enter another sentence
                if (!result.isEnglishSentence) model.addAttribute("errorFound", "Sorry, at the moment only the English language is supported");
                else if (result.hasForeign) model.addAttribute("errorFound", "Be sure you're not using any special characters");
                else if (!result.isValid) model.addAttribute("errorFound", "The provided sentence doesn't have a correct structure");
                else {
                    // If all previous checks are passed (english, no special characters, structure is valid), continue
                    model.addAttribute("valid", true);

                    // Parse the tokens (blocks) of the sentence to display them to the user
                    List<Map<String, String>> tokens = result.response.getTokensList().stream().map(token -> {
                        Map<String, String> tokenData = new HashMap<>();
                        tokenData.put("word", token.getText().getContent());
                        tokenData.put("pos", token.getPartOfSpeech().getTag().name());
                        return tokenData;
                    }).toList();

                    // Get also the invalid parts
                    List<String> invalidWords = controller.inputParts.getInvalid();

                    // Handle the words with the (X) tag
                    if (!invalidWords.isEmpty()) {
                        model.addAttribute("invalid", true);
                        model.addAttribute("invalidWords", invalidWords);
                    }

                    // Get the categorized words from input
                    Map<String, List<String>> categories = controller.inputParts.getPartMap();

                    // Parse the moderation flags found by the API, but only those with high confidence
                    List<Map<String, Object>> moderationFlags = new ArrayList<>();

                    if (result.moderateTextResponse != null) {
                        moderationFlags = result.moderateTextResponse.getModerationCategoriesList().stream()
                                .filter(cat -> cat.getConfidence() >= 0.55)
                                .map(cat -> {
                                    Map<String, Object> flag = new HashMap<>();
                                    flag.put("name", cat.getName());
                                    flag.put("confidence", cat.getConfidence());
                                    return flag;
                                }).toList();
                    }

                    // Add the parsed information as attributes to the model, so that we can interact and customise them
                    // with the use of thymeleaf
                    model.addAttribute("tokens", tokens);
                    model.addAttribute("categories", categories);
                    model.addAttribute("moderationFlags", moderationFlags);
                }

            }
            return "index";
        }

        // User declined
        if ("no".equalsIgnoreCase(confirmed)) return "redirect:/";

        return "index";
    }

    /**
     * API endpoint for generating new nonsense sentences
     */
    @GetMapping("/api/sentence")
    @ResponseBody
    public String getNewSentence() {
        return controller.generateTemplate();
    }

    /**
     * Downloads the history of generated sentences as a text file
     */
    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<Resource> download() throws Exception {

        if(controller.history.getHistory().isEmpty())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

        Path path = Files.createTempFile("generated", ".txt");
        Files.write(path, controller.history.getHistory());

        Resource resource = new FileSystemResource(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"generated.txt\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(resource);
    }
}