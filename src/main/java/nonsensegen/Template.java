package nonsensegen;

import nonsensegen.parts.DictionaryParts;
import nonsensegen.parts.InputParts;

import java.util.List;

public class Template {

    /**
        Luca mangia il pane
        [Soggetto] [verbo] il [oggetto]

        ~~~ ~~~ il ~~~
     */
    private String str;
    private Container cont;

    public Template(Container cont){
        this.cont = cont;
    }

    /**
     * Da chiamare solamente DOPO che l'input e' gia' stato controllato e diviso in parti
     */
    private void genTemplate(){
        // Chiama controller, che chiamera' un API esterno?
    }

    /**
     * Ritorna la template gia' riempita e la salva in cronologia.txt
     */
    public String getTemplate(InputParts inputCont, DictionaryParts dictCont){

        return str;
    }
}
