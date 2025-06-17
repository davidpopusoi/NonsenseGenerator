package nonsensegen;

import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller; // questo è l'annotation di Spring

@RequiredArgsConstructor
@Controller
public class WebController {

    private final nonsensegen.Controller controller; // usa il nome completo per evitare conflitti

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @PostMapping("/analyze")
    public String analyze(@RequestParam("sentence") String sentence, Model model) {
        String result = controller.analizzaFrase(sentence);
        model.addAttribute("output", result);
        return "index";
    }
}