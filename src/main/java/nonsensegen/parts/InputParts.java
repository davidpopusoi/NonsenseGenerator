package nonsensegen.parts;

import nonsensegen.Controller;
import org.springframework.stereotype.Component;

@Component
public class InputParts extends AbstractParts{

    /* TODO: Da rivedere
    public InputParts(Controller service){
        super(service);
    }

     */

    /**
     * Prende la frase input (dopo che l'abbiamo gia' controllata) e la divide in parti e riempie le chiavi della mappa (soggetto, verbo, oggetto)
     */
    @Override
    public void fillMap() {
        // Devo chiamare controller
    }
}
