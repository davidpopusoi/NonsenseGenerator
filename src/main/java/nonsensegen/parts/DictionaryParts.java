package nonsensegen.parts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

@Component
public class DictionaryParts extends AbstractParts{

    // Path to find the built-in dictionary file
    private Path dictionaryPath = Paths.get("src/main/resources/dictionary.txt");

    @Autowired
    public DictionaryParts(){
        fillParts();
    }

    /**
     * Constructor for manually setting a path and manually filling the part map, used only in testing.
     */
    public DictionaryParts(Path dictionaryPath){
        this.dictionaryPath = dictionaryPath;
        //fillParts();
    }

    public Path getDictionaryPath(){
        return this.dictionaryPath;
    }

    /**
     * Fills the dictionary with parts of speech taken from a built-in dictionary, which is located by {@link #dictionaryPath}.
     * Categories are identified by a line that starts with `#` in the file.
     */
    @Override
    protected void fillParts() {
        try (BufferedReader reader = Files.newBufferedReader(dictionaryPath)) {
            String line;
            String currentCategory = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip empty lines
                if (line.isEmpty()) continue;

                // Identify the category header, such as "#noun"
                if (line.startsWith("#")) {
                    // Remove the `#` from the category name, then insert it as a key in the partMap
                    currentCategory = line.substring(1).toLowerCase();
                    // `putIfAbsent` only inserts the key if it isn't already present in the map
                    partMap.putIfAbsent(currentCategory, new ArrayList<>());

                } else if (currentCategory != null) {
                    // We're going through a category, each line is a word to be added to the current category,
                    // until we encounter another another category header (#adjective)
                    partMap.get(currentCategory).add(line);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}