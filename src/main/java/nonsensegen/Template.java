package nonsensegen;

import nonsensegen.parts.DictionaryParts;
import nonsensegen.parts.InputParts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Template {
    private static final Logger LOGGER = LoggerFactory.getLogger(Template.class);
    private static final Path TEMPLATES = Paths.get("src/main/resources/sentenceTemplates.txt");
    private String str;
    private final Random random = new Random();

    /**
     * Ritorna la template gia' riempita e la salva in cronologia.txt
     */
    public String getTemplate(DictionaryParts dictionaryParts, InputParts inputParts){
        return fillTemplate(genTemplate(), dictionaryParts, inputParts);
    }

    /**
     * Da chiamare solamente DOPO che l'input e' gia' stato controllato e diviso in parti
     */
    private String genTemplate() {
        try (BufferedReader reader = Files.newBufferedReader(TEMPLATES)) {
            Map<Integer, List<String>> parts = Map.of(
                    1, new ArrayList<>(),
                    2, new ArrayList<>(),
                    3, new ArrayList<>()
            );

            int currentPart = 0;

            for (String line; (line = reader.readLine()) != null; ) {
                line = line.trim();

                if (line.isEmpty()) continue;
                if (line.startsWith("#")) {
                    currentPart++;
                    continue;
                }

                parts.getOrDefault(currentPart, List.of()).add(line);
            }

            Random rand = new Random();
            String randPart1 = getRandomLine(parts.get(1), rand);
            String randPart2 = getRandomLine(parts.get(2), rand);
            String randPart3 = getRandomLine(parts.get(3), rand);

            str = String.join(" ", randPart1, randPart2, randPart3);
            return str;

        } catch (Exception e) {
            //throw new RuntimeException(e);
            System.out.println(e);
            return "Error reading template file.";
        }
    }

    private String getRandomLine(List<String> list, Random rand) {
        if (list == null || list.isEmpty()) return "[MISSING PART]";
        return list.get(rand.nextInt(list.size())).trim();
    }

    public String fillTemplate(String template, DictionaryParts dictionaryParts, InputParts inputParts) {
        Pattern pattern = Pattern.compile("\\[(\\w+)]");
        Matcher matcher = pattern.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String category = matcher.group(1).toLowerCase();
            String replacement = getReplacement(category, 0.5, dictionaryParts, inputParts);

            // DEBUG
            //LOGGER.info("Placeholder: " + category + " | Replacement: " + replacement);

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return result.toString();
    }


    private String getReplacement(String category, double dictionaryRatio, DictionaryParts dictionaryParts, InputParts inputParts){
        boolean useDict = random.nextDouble() < dictionaryRatio;

        List<String> dictList = dictionaryParts.getCategoryOrCreate(category);
        List<String> inputList = inputParts.getCategoryOrCreate(category);

        List<String> primary = useDict ? dictList : inputList;
        List<String> fallback = useDict ? inputList : dictList;

        if (!primary.isEmpty()) {
            //LOGGER.info("SELECTED FROM primary, # of parts: " + primary.size());
            return primary.get(random.nextInt(primary.size()));
        } else if (!fallback.isEmpty()) {
            //LOGGER.info("SELECTED FROM fallback, # of parts: " + fallback.size());
            return fallback.get(random.nextInt(fallback.size()));
        } else {
            return "[N/A]";
        }
    }
}
