package nonsensegen.parts;

import nonsensegen.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
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
    /* TODO: Da rivedere
    @Autowired
    public DictionaryParts(Controller service){
        super(service);
    }*/

    /**
     * Riempie la mappa prendendo dal file dictionary.txt
     */
    @Override
    public void fillMap() {
        // Devo chiamare controller
    }
}
