package nonsensegen.parts;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class DictionaryParts extends AbstractParts{

    private static final Path DICTIONARY = Paths.get("src/main/resources/dictionary.txt");

    @Override
    protected void fillParts() {
        try (BufferedReader reader = Files.newBufferedReader(DICTIONARY)) {
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