package nonsensegen.parts;

import nonsensegen.Controller;

public class DictionaryParts extends AbstractParts{


    /**
     * dictionary.txt
     *
     * --soggeto
     * Luca
     * Andrea
     * Me
     * She
     * He
     * --verbo
     * Goes
     * Eats
     * Drives
     * --oggetto
     * dog
     * cat
     * table
     * computer
     */
    public DictionaryParts(Controller controller){
        super(controller);
    }

    /**
     * Riempie la mappa prendendo dal file dictionary.txt
     */
    @Override
    public void fillMap() {
        // Devo chiamare controller
    }
}
