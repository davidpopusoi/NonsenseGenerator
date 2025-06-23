package nonsensegen;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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

@RequiredArgsConstructor
@Controller
public class WebController {

    private static final Logger LOGGER = LoggerFactory.getLogger(nonsensegen.WebController.class);

    private final nonsensegen.Controller controller; // usa il nome completo per evitare conflitti

    @GetMapping("/")
    public String home(Model model, HttpSession session) {

        /*
        History history = (History) session.getAttribute("history");
        if (history != null && !history.getHistory().isEmpty()) {
            model.addAttribute("historyList", history.getHistory());
        }
         */

        model.addAttribute("generated", "Click the button to generate a sentence.");

        return "index";
    }

    @GetMapping("/analyze")
    public String redirectAnalyzeGet() {
        return "redirect:/";
    }

    @PostMapping("/analyze")
    public String analyze(@RequestParam("sentence") String sentence,
                          @RequestParam(value = "confirmed", required = false) String confirmed,
                          Model model) {

        model.addAttribute("sentence", sentence);

        if (confirmed == null) {
            model.addAttribute("showConfirmation", true);
            return "index";
        }

        if ("yes".equalsIgnoreCase(confirmed)) {

            nonsensegen.Controller.ControllerResponse result = controller.analyzeSentence(sentence);
            model.addAttribute("output", true);
            model.addAttribute("showDownload", true);

            if (result != null) {

                if (!result.isEnglishSentence) model.addAttribute("errorFound", "Sorry, at the moment only the English language is supported");
                else if (result.hasForeign) model.addAttribute("errorFound", "Be sure you're not using any special characters");
                else if (!result.isValid) model.addAttribute("errorFound", "The provided sentence doesn't have a correct structure");
                else {
                    model.addAttribute("valid", true);

                    List<Map<String, String>> tokens = result.response.getTokensList().stream().map(token -> {
                        Map<String, String> tokenData = new HashMap<>();
                        tokenData.put("word", token.getText().getContent());
                        tokenData.put("pos", token.getPartOfSpeech().getTag().name());
                        return tokenData;
                    }).toList();

                    List<String> invalidWords = controller.inputParts.getInvalid();

                    // Wods with the (X) tag
                    if (!invalidWords.isEmpty()) {
                        model.addAttribute("invalid", true);
                        model.addAttribute("invalidWords", invalidWords);
                    }

                    Map<String, List<String>> categories = controller.inputParts.getPartMap();

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

    @GetMapping("/api/sentence")
    @ResponseBody
    public String getNewSentence() {
        return controller.generateTemplate();
    }

    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<Resource> download() throws Exception {

        //if(controller.history.getHistory().isEmpty()) return ResponseEntity.ok().body(new );

        Path path = Files.createTempFile("generated", ".txt");
        Files.write(path, controller.history.getHistory());

        Resource resource = new FileSystemResource(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"generated.txt\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(resource);
    }
}