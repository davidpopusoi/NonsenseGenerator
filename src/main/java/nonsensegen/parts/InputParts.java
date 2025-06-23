package nonsensegen.parts;

import com.google.cloud.language.v1.Token;
import com.google.cloud.language.v1.AnalyzeSyntaxResponse;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class InputParts extends AbstractParts {

    /**
     * Extracts and fills the categories, unifying VERB+PRT and PRT+VERB
     */
    public void extractParts(AnalyzeSyntaxResponse response) {
        partMap.clear();

        List<Token> tokens = response.getTokensList();
        int i = 0;
        while (i < tokens.size()) {
            String word = tokens.get(i).getText().getContent();
            String tag = tokens.get(i).getPartOfSpeech().getTag().name();

            // Case 1: PRT + VERB (e.g.: "to eat")
            if (tag.equals("PRT") && i + 1 < tokens.size()
                    && tokens.get(i + 1).getPartOfSpeech().getTag().name().equals("VERB")) {
                String combined = word + " " + tokens.get(i + 1).getText().getContent();

                this.getVerb().add(combined);

                i += 2;
                continue;
            }

            // Case 2: VERB + PRT (e.g.: "pick up")
            if (tag.equals("VERB") && i + 1 < tokens.size()
                    && tokens.get(i + 1).getPartOfSpeech().getTag().name().equals("PRT")) {
                String combined = word + " " + tokens.get(i + 1).getText().getContent();

                this.getVerb().add(combined);

                i += 2;
                continue;
            }

            switch (tag) {
                case "ADJ": this.getAdjective().add(word); break;
                case "NOUN": this.getNoun().add(word); break;
                case "VERB": this.getVerb().add(word); break;
                case "NUM": this.getNumber().add(word); break;
                case "PUNCT": this.getPunct().add(word); break;
                case "X":
                    if (word.matches("^[A-Za-z]+$")) {
                        this.getOther().add(word + " (X)");
                    } else {
                        this.getInvalid().add(word);
                    }
                    break;
                default: this.getOther().add(word + " (" + tag + ")"); break;
            }
            i++;
        }
    }

    public String getTabellaCategorie() {
        StringBuilder table = new StringBuilder();
        table.append("\nSYNTAX TABLE\n");
        if (!getAdjective().isEmpty()) table.append("ADJECTIVES:      ").append(String.join("   ", getAdjective())).append("\n");
        if (!getNoun().isEmpty()) table.append("NOUNS:           ").append(String.join("   ", getNoun())).append("\n");
        if (!getVerb().isEmpty()) table.append("VERBS:           ").append(String.join("   ", getVerb())).append("\n");
        if (!getNumber().isEmpty()) table.append("NUMBERS:         ").append(String.join("   ", getNumber())).append("\n");
        if (!getPunct().isEmpty()) table.append("PUNCT:           ").append(String.join("   ", getPunct())).append("\n");
        if (!getInvalid().isEmpty()) table.append("INVALID WORDS:   ").append(String.join("   ", getInvalid())).append("\n");
        if (!getOther().isEmpty()) table.append("OTHERS:          ").append(String.join("   ", getOther())).append("\n");
        return table.toString();
    }

    @Override
    public void fillParts() {
    }
}