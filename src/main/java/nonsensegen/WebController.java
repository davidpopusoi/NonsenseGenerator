package nonsensegen;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final GoogleNlpService googleNlpService;

    @GetMapping("/")
    public String home() {
        return "index"; // loads index.html
    }

    @PostMapping("/analyze")
    public String analyze(@RequestParam("sentence") String sentence, Model model) {
        try {
            String result = googleNlpService.analyzeSyntax(sentence);
            System.out.println("Analyzed Syntax:\n" + result);
            model.addAttribute("output", result);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("output", "Error analyzing text.");
        }
        return "index";
    }
}