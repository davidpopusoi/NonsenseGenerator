package nonsensegen.parts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractParts {

    //protected Map<String, List<String>> map = new HashMap<>();
    protected List<String> adjectives = new ArrayList<>();
    protected List<String> nouns = new ArrayList<>();
    protected List<String> verbs = new ArrayList<>();
    protected List<String> numbers = new ArrayList<>();
    protected List<String> punct = new ArrayList<>();
    protected List<String> invalid = new ArrayList<>();
    protected List<String> others = new ArrayList<>();

    // Tolto Controller perche' la logica va fatta dentro il service (Controller)
    public AbstractParts() {
        initializeMap();
    }

    private void initializeMap(){
        /*
        this.map.put("soggetto", new ArrayList<>());
        this.map.put("verbo", new ArrayList<>());
        this.map.put("oggetto", new ArrayList<>());
        */
    }

    public abstract void fillMap();
}
