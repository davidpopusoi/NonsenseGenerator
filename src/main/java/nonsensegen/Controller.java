package nonsensegen;

import com.google.cloud.language.v1.*;
import com.google.cloud.language.v1.DependencyEdge.Label;
import com.google.cloud.language.v1.PartOfSpeech.Tag;
import nonsensegen.parts.DictionaryParts;
import nonsensegen.parts.InputParts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class Controller { // Da rinominare a qualcosa come "NonsenseService"

    private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

    @Autowired
    private GoogleNlpService googleNlpService;
    @Autowired
    private InputParts inputParts;
    @Autowired
    private DictionaryParts dictionaryParts;
    @Autowired
    private Template template;

    private static final Pattern ENGLISH_WORDS = Pattern.compile("^[a-zA-Z]+$");

    // Metodo aggiunto per analisi e output ordinato
    public String analyzeSentence(String sentence) {


        // TODO: TEST TEMPLATE, MANCA AGGIUNTA DEI TERMINI InputParts

        LOGGER.info(template.getTemplate(dictionaryParts, inputParts));


        LOGGER.info("Attempting to analyze: \"" + sentence + "\"");

        try {
            AnalyzeSyntaxResponse response = googleNlpService.analyzeSyntax(sentence);

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

                // Toxicity
                // TODO: da mette alla frase finale, non a quella di input
                analysis.append("\n")
                        .append(analyzeModeration(googleNlpService.moderateText(sentence)));

                return analysis.toString();
            }

            // TODO: "Killing people is bad" e' incorrect
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

    public static double calculateCorrectnessScore(AnalyzeSyntaxResponse response) {
        boolean hasSubject = false;
        boolean hasMainVerb = false;
        boolean hasAnyVerb = false;

        Token rootToken = null;

        for (Token token : response.getTokensList()) {
            PartOfSpeech pos = token.getPartOfSpeech();
            Tag tag = pos.getTag();
            Label depLabel = token.getDependencyEdge().getLabel();

            if (depLabel == Label.ROOT) {
                rootToken = token;
            }

            // Subject: noun or pronoun with nominal subject relation
            if ((tag == Tag.NOUN || tag == Tag.PRON) &&
                    (depLabel == Label.NSUBJ || depLabel == Label.NSUBJPASS)) {
                hasSubject = true;
            }

            // Check for any verb in sentence
            if (tag == Tag.VERB) {
                hasAnyVerb = true;
            }

            // ROOT verb check
            if (depLabel == Label.ROOT && tag == Tag.VERB) {
                hasMainVerb = true;
            }
        }

        // Heuristic for imperative: root is verb, first token is root, no subject
        boolean isImperative = false;
        if (rootToken != null) {


            Tag rootTag = rootToken.getPartOfSpeech().getTag();

            LOGGER.info("Imperative check, hasSubject: " + hasSubject + ", tag: " + rootTag);

            if (rootTag == Tag.VERB) {

                if (response.getTokensList().get(0).equals(rootToken)) {
                    if (!hasSubject) {
                        isImperative = true;
                    }
                }
            }
        }

        // Sentence must have at least one verb
        if (!hasAnyVerb) {
            return 0.0;
        }

        if (isImperative) {
            return 1.0;
        }

        if (hasSubject && hasMainVerb) {
            return 1.0;
        }

        if (hasSubject || hasMainVerb) {
            return 0.5;
        }

        return 0.0;
    }

    public boolean isValidSentenceFlexible(AnalyzeSyntaxResponse response) {
        return calculateCorrectnessScore(response) == 1.0;
    }

    /*
    public boolean isValidSentenceFlexible(AnalyzeSyntaxResponse response) {
        List<Token> tokens = response.getTokensList();

        boolean hasSubjectLike = false;
        boolean hasVerbLike = false;
        int meaningfulTokens = 0;

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            PartOfSpeech.Tag tag = token.getPartOfSpeech().getTag();
            String lemma = token.getLemma().toLowerCase();

            // Count meaningful words (exclude punctuation, unknowns)
            if (tag != PartOfSpeech.Tag.PUNCT && tag != PartOfSpeech.Tag.X) {
                meaningfulTokens++;
            }

            // Consider NOUN, PRONOUN, PROPER NOUN as subject-like
            if (tag == PartOfSpeech.Tag.NOUN ||
                    tag == PartOfSpeech.Tag.PRON) {
                hasSubjectLike = true;
            }

            // Consider VERB or lemma like "is", "are", etc. as verb-like
            if (tag == PartOfSpeech.Tag.VERB || lemma.equals("be") || lemma.equals("is") || lemma.equals("are")) {
                hasVerbLike = true;
            }

            // Imperative case: sentence starts with a verb and has 1+ other words
            if (i == 0 && tag == PartOfSpeech.Tag.VERB && meaningfulTokens >= 2) {
                return true; // Imperative sentence like "Eat the cake"
            }
        }

        // Declarative case: has at least subject and verb
        return meaningfulTokens >= 2 && hasSubjectLike && hasVerbLike;
    }
    */





    /**
     * Summerizes the response from ModerateTextResponse
     */
    public String analyzeModeration(ModerateTextResponse response) {
        StringBuilder sb = new StringBuilder();

        List<ClassificationCategory> categories = response.getModerationCategoriesList();

        // Non penso ci sia mai un caso in cui le categorie siano vuote
        /*
        if (categories.isEmpty()) {
            return "No harmful content detected.";
        }

         */

        sb.append("MODERATION CATEGORIES FOUND:\n");
        for (ClassificationCategory category : categories) {
            float percent = category.getConfidence() * 100;
            if(percent < 55.0F) continue;

            sb.append("- ")
                    .append(category.getName())
                    .append(": ")
                    .append(String.format("%.2f%%", percent))
                    .append("\n");
        }

        // Caso in cui non ci sono categorie di nota (per adesso solo categorie che hanno piu' del 50% passano)
        if(sb.toString().equals("MODERATION CATEGORIES FOUND:\n")) return "";
        return sb.toString();
    }
}