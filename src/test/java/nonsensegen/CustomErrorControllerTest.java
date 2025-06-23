package nonsensegen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomErrorController.class)
public class CustomErrorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Helper to set error attributes in the request
    private static RequestPostProcessor errorAttributes(int status, String message) {
        return (MockHttpServletRequest request) -> {
            request.setAttribute("jakarta.servlet.error.status_code", status);
            request.setAttribute("jakarta.servlet.error.message", message);
            return request;
        };
    }

    @BeforeEach
    void setup() {
        // Configure standalone setup with view resolver
        mockMvc = MockMvcBuilders.standaloneSetup(new CustomErrorController())
                .setViewResolvers(new InternalResourceViewResolver())
                .build();
    }

    @Test
    void error_404_Returns404View() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/error")
                        .with(errorAttributes(404, "Not Found")))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("status", 404))
                .andExpect(model().attribute("error", "Not Found"));
    }

    @Test
    void error_500_Returns500View() throws Exception {
        mockMvc.perform(get("/error")
                        .with(errorAttributes(500, "Server Error")))
                .andExpect(status().isOk())
                .andExpect(view().name("error/500"))  // Verify view name
                .andExpect(model().attribute("status", 500))
                .andExpect(model().attribute("error", "Server Error"));
    }

    @Test
    void error_400_ReturnsGenericView() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/error")
                        .with(errorAttributes(400, "Bad Request")))
                .andExpect(status().isOk())
                .andExpect(view().name("error/generic"))
                .andExpect(model().attribute("status", 400))
                .andExpect(model().attribute("error", "Bad Request"));
    }

    @Test
    void error_NoStatusCode_DefaultsTo500() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/error")
                        .with((request) -> {
                            request.setAttribute("jakarta.servlet.error.message", "Test error");
                            return request;
                        }))
                .andExpect(view().name("error/500"))
                .andExpect(model().attribute("status", 500))
                .andExpect(model().attribute("error", "Test error"));
    }

    @Test
    void error_NoMessage_DefaultsToGeneric() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/error")
                        .with(errorAttributes(403, null)))
                .andExpect(view().name("error/generic"))
                .andExpect(model().attribute("status", 403))
                .andExpect(model().attribute("error", "Unexpected error"));
    }
}
