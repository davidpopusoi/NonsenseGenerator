package nonsensegen;

import nonsensegen.parts.AbstractParts;
import nonsensegen.parts.DictionaryParts;
import nonsensegen.parts.InputParts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
        try {
            List<String> templates = Files.readAllLines(TEMPLATES);

            /*if (templates.isEmpty()) {
                str = "No templates found.";
                return;
            }*/

            // suddivide la lista di template in tre "sottoliste", ciascuna contenente una diversa parte del template
            Random randTempl = new Random();
            List<String> parte1 = templates.subList(2, 99);   // dalla riga 3 alla 99 inclusa
            List<String> parte2 = templates.subList(102, 198); // dalla riga 103 alla 198 inclusa
            List<String> parte3 = templates.subList(202, 298); // dalla riga 203 alla 298 inclusa

            // sceglie casualmente una porzione di template da ciascuna delle tre sottoliste
            String randPart1 = parte1.get(randTempl.nextInt(parte1.size())).trim();
            String randPart2 = parte2.get(randTempl.nextInt(parte2.size())).trim();
            String randPart3 = parte3.get(randTempl.nextInt(parte3.size())).trim();

            str = randPart1 + " " + randPart2 + " " + randPart3;
            return str;
        }
        catch (IOException e) {
            e.printStackTrace();
            return "Error reading template file.";
        }
    }

    // TODO: MANCA USARE LE InputParts
    public String fillTemplate(String template, DictionaryParts dictionaryParts, InputParts inputParts) {
        Matcher matcher = Pattern.compile("\\[(.+?)]").matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {



            String key = matcher.group(1); // e.g. "plural_noun", "noun", "verb"

            LOGGER.info("IM INSIDE: " + key);

            List<String> words = getWordListForKey(key, dictionaryParts);

            // TODO: SISTEMARE QUA, non mette le parole dentro

            String replacement = (words != null && !words.isEmpty())
                    ? "*" + words.get(random.nextInt(words.size())) + "*"
                    : "[" + key + "]"; // fallback if no list found or empty

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return result.toString();
    }

    // TODO: QUA PRENDE DIRETTAMENTE LE LISTE CHE ESISTONO NELLA CLASSE, FORSE MEGLIO FARE UNA MAPPA DATO CHE NON FUNZIONA
    @SuppressWarnings("unchecked")
    private List<String> getWordListForKey(String key, DictionaryParts dictionaryParts) {
        try {
            Field field = AbstractParts.class.getDeclaredField(key);
            field.setAccessible(true);

            LOGGER.info(field.getName());

            return (List<String>) field.get(dictionaryParts);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Field not found or inaccessible, return null
            LOGGER.info("FIELD NOT FOUND: " + key);
            return null;
        }
    }
}
