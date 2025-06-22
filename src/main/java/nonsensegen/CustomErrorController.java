package nonsensegen;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String error(HttpServletRequest request, Model model) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        int status = statusCode != null ? Integer.parseInt(statusCode.toString()) : 500;

        model.addAttribute("status", status);
        model.addAttribute("error", message != null ? message : "Unexpected error");

        if (status == 404) return "error/404";
        if (status == 500) return "error/500";

        return "error/generic";
    }
}
