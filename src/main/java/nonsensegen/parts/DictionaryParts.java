package nonsensegen.parts;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class DictionaryParts extends AbstractParts{

    private static final Path DICTIONARY = Paths.get("src/main/resources/dictionary.txt");

    @Override
    public void fillParts(){
        List<String> lines = null;
        try {
            lines = Files.readAllLines(DICTIONARY);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<String> currentList = null;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            switch (line.toLowerCase()) {
                case "adjective:":
                    currentList = adjective;
                    break;
                case "noun:":
                    currentList = noun;
                    break;
                case "pluralnoun:":
                    currentList = plural_noun;
                    break;
                case "verb:":
                    currentList = verb;
                    break;
                case "number:":
                    currentList = number;
                    break;
                case "punct:":
                    currentList = punct;
                    break;
                case "invalid:":
                    currentList = invalid;
                    break;
                case "others:":
                    currentList = other;
                    break;
                default:
                    if (currentList != null) {
                        currentList.add(line);
                    }
                    break;
            }
        }
    }
}
