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
import java.time.LocalTime;

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
            /*
            model.addAttribute("sentence", sentence);
            controller.analyzeSentence(sentence);
            if (!controller.inputParts.getInvalid().isEmpty()) {
                model.addAttribute("showError", true);
                model.addAttribute("invalidWords", controller.inputParts.getInvalid());
            } else {
                model.addAttribute("showConfirmation", true);
            }
            */
            return "index";
        }

        if ("yes".equalsIgnoreCase(confirmed)) {
            String result = controller.analyzeSentence(sentence);
            model.addAttribute("output", result);
            model.addAttribute("showDownload", true);
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
        Path path = Files.createTempFile("generated", ".txt");
        Files.write(path, controller.history.getHistory());
        Resource resource = new FileSystemResource(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"generated.txt\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(resource);
    }
}