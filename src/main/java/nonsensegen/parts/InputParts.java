package nonsensegen.parts;

import nonsensegen.Controller;

public class InputParts extends AbstractParts{

    public InputParts(Controller controller){
        super(controller);
    }

    /**
     * Prende la frase input (dopo che l'abbiamo gia' controllata) e la divide in parti e riempie le chiavi della mappa (soggetto, verbo, oggetto)
     */
    @Override
    public void fillMap() {
        // Devo chiamare controller
    }
}
