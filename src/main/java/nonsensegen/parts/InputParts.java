package nonsensegen.parts;

import com.google.cloud.language.v1.Token;
import com.google.cloud.language.v1.AnalyzeSyntaxResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class InputParts extends AbstractParts {

    @Autowired
    public InputParts() {
        fillParts();
    }

    /**
     * Extracts and fills the categories, unifying VERB+PRT and PRT+VERB
     */
    public void extractParts(AnalyzeSyntaxResponse response) {
        // Clear the map
        partMap.clear();

        // List of tokens, which are the blocks that form the sentence
        List<Token> tokens = response.getTokensList();
        int i = 0;

        while (i < tokens.size()) {
            // Get the word itself
            String word = tokens.get(i).getText().getContent();
            // Get the associated part of speech "tag"
            String tag = tokens.get(i).getPartOfSpeech().getTag().name();

            // Some edge cases, where we have to make some combining
            // Case 1: PRT ("Particle or other function word") + VERB (e.g.: "to eat")
            if (tag.equals("PRT") && i + 1 < tokens.size()
                    && tokens.get(i + 1).getPartOfSpeech().getTag().name().equals("VERB")) {
                String combined = word + " " + tokens.get(i + 1).getText().getContent();

                this.getVerb().add(combined);

                i += 2;
                continue;
            }

            // Case 2: VERB + PRT ("Particle or other function word") (e.g.: "pick up")
            if (tag.equals("VERB") && i + 1 < tokens.size()
                    && tokens.get(i + 1).getPartOfSpeech().getTag().name().equals("PRT")) {
                String combined = word + " " + tokens.get(i + 1).getText().getContent();

                this.getVerb().add(combined);

                i += 2;
                continue;
            }

            // Add the word to the appropriate category. If the category isn't defined, it will be added to the
            // "other" list, with its tag appended
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

    @Override
    public void fillParts() {
    }
}