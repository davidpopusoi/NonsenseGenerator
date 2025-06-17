package nonsensegen;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class NonsensegeneratorApplication {

    /*
    private final Container container;

    @Autowired
    public NonsensegeneratorApplication(Container container) {
        this.container = container;
    }*/

    public static void main(String[] args) {
        // Fa partire l'applicazione springboot
        SpringApplication.run(NonsensegeneratorApplication.class, args);
    }
}
