package nonsensegen;

import nonsensegen.parts.DictionaryParts;
import nonsensegen.parts.InputParts;

public class Container {

    private String input; // Frase data dall'utente
    private Controller controller = new Controller(this);
    private InputParts inputCont = new InputParts(controller); // Componenti estratti dall'input (verbi, aggettivi, nomi, ecc)
    private Template template;
    private DictionaryParts dictCont = new DictionaryParts(controller);  // Componenti estratti dal dizionario (verbi, aggettivi, nomi, ecc)

    public Container(){
        this.template = new Template(this);
    }

    
}
