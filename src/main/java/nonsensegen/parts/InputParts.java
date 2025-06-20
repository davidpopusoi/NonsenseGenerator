package nonsensegen.parts;

import com.google.cloud.language.v1.Token;
import com.google.cloud.language.v1.AnalyzeSyntaxResponse;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class InputParts extends AbstractParts {

    // Estrae e riempie le categorie, unendo VERB+PRT e PRT+VERB
    public void estraiCategorie(AnalyzeSyntaxResponse response) {
        adjective.clear(); noun.clear(); verb.clear();
        number.clear(); punct.clear(); invalid.clear(); other.clear();

        List<Token> tokens = response.getTokensList();
        int i = 0;
        while (i < tokens.size()) {
            String word = tokens.get(i).getText().getContent();
            String tag = tokens.get(i).getPartOfSpeech().getTag().name();

            // Caso 1: PRT + VERB (es: "to eat")
            if (tag.equals("PRT") && i + 1 < tokens.size()
                    && tokens.get(i + 1).getPartOfSpeech().getTag().name().equals("VERB")) {
                String combined = word + " " + tokens.get(i + 1).getText().getContent();
                verb.add(combined);
                i += 2;
                continue;
            }

            // Caso 2: VERB + PRT (es: "pick up")
            if (tag.equals("VERB") && i + 1 < tokens.size()
                    && tokens.get(i + 1).getPartOfSpeech().getTag().name().equals("PRT")) {
                String combined = word + " " + tokens.get(i + 1).getText().getContent();
                verb.add(combined);
                i += 2;
                continue;
            }

            // Normale categorizzazione
            switch (tag) {
                case "ADJ": adjective.add(word); break;
                case "NOUN": noun.add(word); break;
                case "VERB": verb.add(word); break;
                case "NUM": number.add(word); break;
                case "PUNCT": punct.add(word); break;
                case "X":
                    if (word.matches("^[A-Za-z]+$")) {
                        other.add(word + " (X)");
                    } else {
                        invalid.add(word);
                    }
                    break;
                default: other.add(word + " (" + tag + ")"); break;
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
        if (!adjective.isEmpty()) table.append("ADJECTIVES:      ").append(String.join("   ", adjective)).append("\n");
        if (!noun.isEmpty()) table.append("NOUNS:           ").append(String.join("   ", noun)).append("\n");
        if (!verb.isEmpty()) table.append("VERBS:           ").append(String.join("   ", verb)).append("\n");
        if (!number.isEmpty()) table.append("NUMBERS:         ").append(String.join("   ", number)).append("\n");
        if (!punct.isEmpty()) table.append("PUNCT:           ").append(String.join("   ", punct)).append("\n");
        if (!invalid.isEmpty()) table.append("INVALID WORDS:   ").append(String.join("   ", invalid)).append("\n");
        if (!other.isEmpty()) table.append("OTHERS:          ").append(String.join("   ", other)).append("\n");
        return table.toString();
    }


    // Il resto del file NON toccato!
    @Override
    public void fillParts() {
        // Devo chiamare controller
    }
}