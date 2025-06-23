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

    private Path dictionaryPath = Paths.get("src/main/resources/dictionary.txt");

    @Autowired
    public DictionaryParts(){
        fillParts();
    }

    /**
     * Constructor for manually setting a path and manually filling the part map
     */
    public DictionaryParts(Path dictionaryPath){
        this.dictionaryPath = dictionaryPath;
        //fillParts();
    }

    public Path getDictionaryPath(){
        return this.dictionaryPath;
    }

    @Override
    protected void fillParts() {
        try (BufferedReader reader = Files.newBufferedReader(dictionaryPath)) {
            String line;
            String currentCategory = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) continue;

                if (line.startsWith("#")) {
                    // Category headers, such as "#noun"
                    currentCategory = line.substring(1).toLowerCase();
                    partMap.putIfAbsent(currentCategory, new ArrayList<>());
                } else if (currentCategory != null) {
                    partMap.get(currentCategory).add(line);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}