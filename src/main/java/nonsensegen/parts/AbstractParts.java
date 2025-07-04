package nonsensegen.parts;

import java.util.*;

public abstract class AbstractParts {

    // Map that holds all our words, where the key is part of speech and the value is a String List with the words that
    // have been identified to belong to those parts of speech
    protected Map<String, List<String>> partMap = new HashMap<>();

    protected abstract void fillParts();

    public Map<String, List<String>> getPartMap(){
        return this.partMap;
    }

    /**
     * Returns the category if found with the provided String, otherwise adds the String as a key with a new List to the map
     */
    public List<String> getCategoryOrCreate(String category) {
        return partMap.computeIfAbsent(category.toLowerCase(), k -> new ArrayList<>());
    }

    // Getters for each category. If one doesn't exist, it will be created

    public List<String> getVerb(){
        return this.getCategoryOrCreate("verb");
    }

    public List<String> getNoun(){
        return this.getCategoryOrCreate("noun");
    }

    public List<String> getPluralNoun(){
        return this.getCategoryOrCreate("pluralnoun");
    }

    public List<String> getAdjective(){
        return this.getCategoryOrCreate("adjective");
    }

    public List<String> getNumber(){
        return this.getCategoryOrCreate("number");
    }

    public List<String> getPunct(){
        return this.getCategoryOrCreate("punct");
    }

    public List<String> getOther(){
        return this.getCategoryOrCreate("other");
    }

    public List<String> getInvalid(){
        return this.getCategoryOrCreate("invalid");
    }
}
