package nonsensegen.parts;

import com.google.cloud.language.v1.Token;
import com.google.cloud.language.v1.AnalyzeSyntaxResponse;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class InputParts extends AbstractParts {

    private List<String> adjectives = new ArrayList<>();
    private List<String> nouns = new ArrayList<>();
    private List<String> verbs = new ArrayList<>();
    private List<String> numbers = new ArrayList<>();
    private List<String> punct = new ArrayList<>();
    private List<String> invalid = new ArrayList<>();
    private List<String> others = new ArrayList<>();

    // Estrae e riempie le categorie, unendo VERB+PRT e PRT+VERB
    public void estraiCategorie(AnalyzeSyntaxResponse response) {
        adjectives.clear(); nouns.clear(); verbs.clear();
        numbers.clear(); punct.clear(); invalid.clear(); others.clear();

        List<Token> tokens = response.getTokensList();
        int i = 0;
        while (i < tokens.size()) {
            String word = tokens.get(i).getText().getContent();
            String tag = tokens.get(i).getPartOfSpeech().getTag().name();

            // Caso 1: PRT + VERB (es: "to eat")
            if (tag.equals("PRT") && i + 1 < tokens.size()
                    && tokens.get(i + 1).getPartOfSpeech().getTag().name().equals("VERB")) {
                String combined = word + " " + tokens.get(i + 1).getText().getContent();
                verbs.add(combined);
                i += 2;
                continue;
            }

            // Caso 2: VERB + PRT (es: "pick up")
            if (tag.equals("VERB") && i + 1 < tokens.size()
                    && tokens.get(i + 1).getPartOfSpeech().getTag().name().equals("PRT")) {
                String combined = word + " " + tokens.get(i + 1).getText().getContent();
                verbs.add(combined);
                i += 2;
                continue;
            }

            // Normale categorizzazione
            switch (tag) {
                case "ADJ": adjectives.add(word); break;
                case "NOUN": nouns.add(word); break;
                case "VERB": verbs.add(word); break;
                case "NUM": numbers.add(word); break;
                case "PUNCT": punct.add(word); break;
                case "X":
                    if (word.matches("^[A-Za-z]+$")) {
                        others.add(word + " (X)");
                    } else {
                        invalid.add(word);
                    }
                    break;
                default: others.add(word + " (" + tag + ")"); break;
            }
            i++;
        }
    }

    //invalid words
    public List<String> getInvalid() {
        return invalid;
    }

    // Tabella testuale delle categorie
    public String getTabellaCategorie() {
        StringBuilder table = new StringBuilder();
        table.append("\nSYNTAX TABLE\n");
        if (!adjectives.isEmpty()) table.append("ADJECTIVES:      ").append(String.join("   ", adjectives)).append("\n");
        if (!nouns.isEmpty()) table.append("NOUNS:           ").append(String.join("   ", nouns)).append("\n");
        if (!verbs.isEmpty()) table.append("VERBS:           ").append(String.join("   ", verbs)).append("\n");
        if (!numbers.isEmpty()) table.append("NUMBERS:         ").append(String.join("   ", numbers)).append("\n");
        if (!punct.isEmpty()) table.append("PUNCT:           ").append(String.join("   ", punct)).append("\n");
        if (!invalid.isEmpty()) table.append("INVALID WORDS:   ").append(String.join("   ", invalid)).append("\n");
        if (!others.isEmpty()) table.append("OTHERS:          ").append(String.join("   ", others)).append("\n");
        return table.toString();
    }


    // Il resto del file NON toccato!
    @Override
    public void fillMap() {
        // Devo chiamare controller
    }
}