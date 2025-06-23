package nonsensegen;

import nonsensegen.parts.DictionaryParts;
import nonsensegen.parts.InputParts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
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
     * Returns the filled template using DictionaryParts and InputParts, a.k.a. the complete nonsense sentence
     */
    public String getTemplate(DictionaryParts dictionaryParts, InputParts inputParts) {
        return fillTemplate(genTemplate(), dictionaryParts, inputParts);
    }

    /**
     * Reads the template file, which is structured like this:
     *
     *   #part1
     *
     *   [adj] [noun] [verb]
     *   The [adj] [noun]
     *   ...
     *   #part2
     *
     *   [verb] [adv]
     *   ...
     *   #part3
     *
     *   [prep] [adj] [noun]
     *   ...
     *
     *
     * Returns the NOT filled template, created by joining different parts found in the TEMPLATES file.
     * This String will have placeholders, such as [verb], [noun], [pluralnoun]
     */
    private String genTemplate() {
        try (BufferedReader reader = Files.newBufferedReader(TEMPLATES)) {
            // Organize templates by section
            Map<Integer, List<String>> parts = Map.of(
                    1, new ArrayList<>(),
                    2, new ArrayList<>(),
                    3, new ArrayList<>()
            );

            int currentPart = 0;

            for (String line; (line = reader.readLine()) != null; ) {
                line = line.trim();

                if (line.isEmpty()) continue;

                // Different parts have headers that start with #
                if (line.startsWith("#")) {
                    currentPart++;
                    continue;
                }

                // Add the current line to the current section
                parts.getOrDefault(currentPart, List.of()).add(line);
            }

            // Randomly select one template from each section
            Random rand = new Random();
            String randPart1 = getRandomLine(parts.get(1), rand);
            String randPart2 = getRandomLine(parts.get(2), rand);
            String randPart3 = getRandomLine(parts.get(3), rand);

            // Combine the parts into the full template
            str = String.join(" ", randPart1, randPart2, randPart3);
            return str;

        } catch (Exception e) {
            //throw new RuntimeException(e);
            System.out.println(e);
            return "Error reading template file.";
        }
    }

    /**
     * Selects a random line from a list of template parts
     */
    private String getRandomLine(List<String> list, Random rand) {
        if (list == null || list.isEmpty()) return "[MISSING PART]";
        return list.get(rand.nextInt(list.size())).trim();
    }

    /**
     * Replaces the placeholders in a template with actual words
     */
    public String fillTemplate(String template, DictionaryParts dictionaryParts, InputParts inputParts) {
        // Regex to find placeholders
        Pattern pattern = Pattern.compile("\\[(\\w+)]");
        Matcher matcher = pattern.matcher(template);
        StringBuffer result = new StringBuffer();

        // Process each placeholder
        while (matcher.find()) {
            String category = matcher.group(1).toLowerCase();
            String replacement = getReplacement(category, 0.5, dictionaryParts, inputParts);

            // Special handling for verb placeholders
            if ("verb".equals(category)) {
                int end = matcher.end();  // position just after the placeholder
                if ((replacement.endsWith("s") || replacement.endsWith("ed")) &&
                        end < template.length() &&
                        template.charAt(end) == 's') {
                    // Drop the final 's' from the replacement
                    replacement = replacement.substring(0, replacement.length() - 1);
                }
            }

            // DEBUG
            //LOGGER.info("Placeholder: " + category + " | Replacement: " + replacement);

            // Finally replace the placeholder with actual word
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        // Append remaining template content
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Selects an appropriate word for a placeholder category, using a weighted selection between dictionary and user words
     * (by default we use a 0.5 weight, which is 50% dictionary, 50% input)
     */
    private String getReplacement(String category, double dictionaryRatio, DictionaryParts dictionaryParts, InputParts inputParts) {
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
