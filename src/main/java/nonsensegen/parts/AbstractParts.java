package nonsensegen.parts;

import nonsensegen.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractParts {

    protected Map<String, List<String>> map = new HashMap<>();
    protected Controller controller;

    public AbstractParts(Controller controller) {
        this.controller = controller;
        initializeMap();
    }

    private void initializeMap(){
        this.map.put("soggetto", new ArrayList<>());
        this.map.put("verbo", new ArrayList<>());
        this.map.put("oggetto", new ArrayList<>());
    }

    public abstract void fillMap();
}
