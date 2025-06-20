package nonsensegen.parts;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractParts {

    //protected Map<String, List<String>> map = new HashMap<>();
    protected List<String> adjective = new ArrayList<>();
    protected List<String> noun = new ArrayList<>();
    protected List<String> plural_noun = new ArrayList<>();
    protected List<String> verb = new ArrayList<>();
    protected List<String> number = new ArrayList<>();
    protected List<String> punct = new ArrayList<>();
    protected List<String> invalid = new ArrayList<>();
    protected List<String> other = new ArrayList<>();

    // Tolto Controller perche' la logica va fatta dentro il service (Controller)
    public AbstractParts() {

    }

    public abstract void fillParts();
}
