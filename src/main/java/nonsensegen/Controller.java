package nonsensegen;

public class Controller {

    private Container container;

    /**
     * Quello che interagisce con le API esterne (cioe' Google)
     */
    public Controller(Container container){
        this.container = container;
    }

    public boolean verificaFraseInput(){

        return false;
    }

    /**
     * Chiamato solo se la verifica della frase input ha successo
     */
    public void dividiInputParti(){

    }

    public void prendiPartiDizionario(){

    }

    public void controllaTossicita(){

    }
}
