package nonsensegen.app;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.logging.Logger;

@Controller
public class NonsensegeneratorController {

    private static final Logger logger = Logger.getLogger(NonsensegeneratorController.class.getName());

    @GetMapping("/")
    public String index() {
        return "index"; // Renders templates/index.html
    }

    @PostMapping("/submit")
    public String handleInput(@RequestParam("sentence") String sentence) {
        logger.info("User submitted sentence: " + sentence);
        return "redirect:/"; // After submission, go back to home
    }
}
