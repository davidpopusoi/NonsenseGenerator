package nonsensegen;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class NonsensegeneratorApplication implements CommandLineRunner {

    private final Container container;

    @Autowired
    public NonsensegeneratorApplication(Container container) {
        this.container = container;
    }

    public static void main(String[] args) {
        SpringApplication.run(NonsensegeneratorApplication.class, args);
        System.out.println("Hello, World!");
    }


    // Per prendere da linea di comando, dopo sara' tutto fatto nell'interfaccia web, non in questa classe quindi, ma in "NonsenseRestController", classe annotata con @RestController
    @Override
    public void run(String... args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.println("inserisci frase:");
        String input = scanner.nextLine();
        System.out.println(input); // Fara' l'output solo una volta dato che per adesso e' scritto in modo che faccia il tutto una volta sola


        /* Ho commentato questi metodi anche nel Container, da guardare!

        container.setInput(input);

        if (container.verificaFraseInput()) {
            container.dividiInputParti();
            container.prendiPartiDizionario();
            container.controllaTossicita();
            System.out.println(container.getTemplate());
        } else {
            System.out.println("Frase non valida");
        }

         */
    }


}
