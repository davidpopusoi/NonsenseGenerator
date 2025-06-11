package nonsensegen;

import nonsensegen.parts.DictionaryParts;
import nonsensegen.parts.InputParts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Container { // Eventualmente si protrebbe eliminare (dato che tanto il tutto viene richiesto al Controller (service)), ma per adesso la teniamo per raggruppare il tutto

    private String input; // Frase data dall'utente
    private Controller controller;
    private InputParts inputCont; // Componenti estratti dall'input (verbi, aggettivi, nomi, ecc)
    private Template template;
    private DictionaryParts dictCont;  // Componenti estratti dal dizionario (verbi, aggettivi, nomi, ecc)

    @Autowired
    public Container(Controller controller, InputParts inputCont, DictionaryParts dictCont, Template template) {
        this.controller = controller; // Solo il Container deve chiamare il Controller, non viceversa
        this.inputCont = inputCont;
        this.dictCont = dictCont;
        this.template = template;
    }

    /*

    public void setInput(String input) {
        this.input = input;
    }

    public boolean verificaFraseInput() {
        return controller.verificaFraseInput(input);
    }

    public void dividiInputParti() {
        controller.dividiInputParti();
    }

    public String getTemplate() {
        return template.getTemplate();
    }

     */
}
