package nonsensegen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Nonsense Generator Spring Boot application, from which the
 * Spring application context and embedded server are launched
 */
@SpringBootApplication
public class NonsensegeneratorApplication {

    public static void main(String[] args) {
        // Start the Spring Boot application
        // When we start the application, it will also scan for @Components, inject the dependencies between components,
        // find and initialize all @Services and start the embedded server.

        // The services we initialize are GoogleNlpService and Controller.
        SpringApplication.run(NonsensegeneratorApplication.class, args);
    }
}
