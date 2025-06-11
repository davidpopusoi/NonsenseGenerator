package nonsensegen;

import nonsensegen.parts.DictionaryParts;
import nonsensegen.parts.InputParts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Template {

    /**
        Luca mangia il pane
        [Soggetto] [verbo] il [oggetto]

        ~~~ ~~~ il ~~~
     */
    private String str;

    @Autowired
    public Template(){

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
