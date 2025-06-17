package nonsensegen;

import com.google.cloud.language.v1.*;
import nonsensegen.parts.InputParts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class Controller { // Da rinominare a qualcosa come "NonsenseService"

    @Autowired
    private GoogleNlpService googleNlpService;
    @Autowired
    private InputParts inputParts;

    // TODO: CONTROLLO TOSSICITA' (con metodo API moderateText)
    public void analyzeToxicity(){

    }

    private static final Pattern ENGLISH_WORDS = Pattern.compile("^[a-zA-Z]+$");

    // Metodo aggiunto per analisi e output ordinato
    public String analyzeSentence(String sentence) {
        try {
            AnalyzeSyntaxResponse response = googleNlpService.analyzeSyntax(sentence);


            // TODO: "She doesn't like broccoli." dice che non e' inglese, "asdf jkl; qwop" e' valida, "Cane mangia tavolo." e' valida

            if (!isEnglishSentence(sentence)) {
                return "Sorry, at the moment only English language is supported.";
            }

            if (hasForeign(response)) {
                return "Be sure to insert only English words.";
            }

            if (isValidSentenceFlexible(response)){
                // Suddivisione in categorie
                inputParts.estraiCategorie(response);


                /**
                 * OUTPUT
                 */


                // Output riga per riga (come ora)
                StringBuilder analysis = new StringBuilder();
                response.getTokensList().forEach(token ->
                        analysis.append(token.getText().getContent())
                                .append(" (")
                                .append(token.getPartOfSpeech().getTag())
                                .append(")\n")
                );

                // Sezione parole non valide (tag X)
                if (!inputParts.getInvalid().isEmpty()) {

                }

                // Tabella categorie
                analysis.append(inputParts.getTabellaCategorie());

                return analysis.toString();
            }

            return "Sentence is incorrect";
        } catch (Exception e) {
            return "Error analyzing text: " + e.getMessage();
        }
    }

    /**
     * Controlla se la frase e' in inglese
     */
    public boolean isEnglishSentence(String sentence) {
        // USARE IL METODO DA googleNlpService E NON CREATE ALTRE ISTANZE DI LanguageServiceClient
        AnalyzeSyntaxResponse response = googleNlpService.analyzeSyntax(sentence);
        String detectedLanguage = response.getLanguage(); // "en", "it", etc.

        return detectedLanguage.equals("en");
    }

    /**
     * Controlla se la frase ha elementi estranei (spazi ecc.)
     */
    public boolean hasForeign(AnalyzeSyntaxResponse response){
        boolean englishWords = false;
        boolean foreignWords = false;

        for (var token : response.getTokensList()) {
            String word = token.getText().getContent();
            String tag = token.getPartOfSpeech().getTag().name();

            if (ENGLISH_WORDS.matcher(word).matches()) {
                englishWords = true;
            } else if (word.trim().length() > 0 && !"PUNCT".equals(tag)) {
                foreignWords = true;
            }
        }

        return englishWords && foreignWords;
    }

    /**
     * Controlla se la struttura della frase e' corretta
     */
    public boolean isValidSentenceFlexible(AnalyzeSyntaxResponse response) {
        List<Token> tokens = response.getTokensList();

        boolean hasSubject = false;
        boolean hasVerb = false;
        int meaningfulTokens = 0;

        for (Token token : tokens) {
            String dep = token.getDependencyEdge().getLabel().toString();
            PartOfSpeech.Tag tag = token.getPartOfSpeech().getTag();

            if (dep.equals("NSUBJ") || dep.equals("NSUBJPASS")) {
                hasSubject = true;
            }

            if (tag == PartOfSpeech.Tag.VERB) {
                hasVerb = true;
            }

            if (tag != PartOfSpeech.Tag.X && tag != PartOfSpeech.Tag.PUNCT) {
                meaningfulTokens++;
            }
        }

        // At least 3 meaningful tokens, and a subject + verb
        return meaningfulTokens >= 3 && hasSubject && hasVerb;
    }
}