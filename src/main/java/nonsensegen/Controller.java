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

import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class Controller {

    // Logger used mainly for debugging
    private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

    // Objects used by the Controller which are initialized automatically thanks to the @Autowired annotation,
    // which eliminates manual instantiation
    @Autowired
    GoogleNlpService googleNlpService;
    @Autowired
    public InputParts inputParts;
    @Autowired
    DictionaryParts dictionaryParts;
    @Autowired
    Template template;
    @Autowired
    public History history;

    public @Nullable ControllerResponse analyzeSentence(String sentence) {
        LOGGER.info("Attempting to analyze: \"" + sentence + "\"");

        try {
            // Run the sentence through the Google API
            AnalyzeSyntaxResponse syntaxResponse = googleNlpService.analyzeSyntax(sentence);
            // Create a response object, which we'll return when this method is called
            ControllerResponse cr = new ControllerResponse(syntaxResponse);

            if (!isEnglishSentence(syntaxResponse)) {
                cr.isEnglishSentence = false;
                //return "Sorry, at the moment only English language is supported.";
            }

            // Check if the sentences has any foreign/special characters
            if (hasForeign(syntaxResponse)) {
                cr.hasForeign = true;
                //return "Be sure to insert only English words.";
            }

            // Check if the sentence's structure is "correct". Note that not everything is caught and there WILL be
            // some false positives/negatives
            if (isValidSentenceFlexible(syntaxResponse)){
                cr.isValid = true;

                // Once that the sentence's structure is found to be "correct", we can also run it through the
                // moderation analysis, which will give us some categories if they apply to the sentence
                ModerateTextResponse moderateTextResponse = googleNlpService.moderateText(sentence);
                cr.moderateTextResponse = moderateTextResponse;

                // Load the parts of speech found in the input sentence
                inputParts.extractParts(syntaxResponse);
            }

            //return "Sentence is incorrect";
            return cr;
        } catch (Exception e) {
            System.out.println(e);
            //return "Error analyzing text: " + e.getMessage();
            return null;
        }
    }

    /**
     * Generates the filled template, using both InputParts and DictionaryParts, and saves it to history
     */
    public String generateTemplate(){
        String finalTemplate = template.getTemplate(dictionaryParts, inputParts);
        history.saveFinalToHistory(finalTemplate);
        return finalTemplate;
    }

    /**
     * Checks if the provided sentence is in English
     */
    public boolean isEnglishSentence(AnalyzeSyntaxResponse response) {
        // Use the instance googleNlpService and DO NOT make other instances of LanguageServiceClient!
        // We get the detected language from the analysis we already did before
        String detectedLanguage = response.getLanguage(); // "en", "it", etc.
        //LOGGER.info("DETECTED LANGUAGE: " + detectedLanguage);
        return detectedLanguage.equals("en");
    }

    // Regex we use to detect words with only alphabetic characters:
    // ^ = start of string, [a-zA-Z] = any letter, + = one or more times, $ = end of string
    private static final Pattern WORDS = Pattern.compile("^[a-zA-Z]+$");

    /**
     * Checks if the sentence has foreign elements (rogue spaces, etc.)
     */
    public boolean hasForeign(AnalyzeSyntaxResponse response) {
        boolean englishWords = false;
        boolean foreignWords = false;

        // Go through every token (block) of the sentence and check if it contains anomalies
        for (var token : response.getTokensList()) {
            String word = token.getText().getContent();
            String tag = token.getPartOfSpeech().getTag().name();

            // Check if the word matches the WORDS regex, aka if its only alphabetical letters
            if (WORDS.matcher(word).matches()) {
                englishWords = true;
            }

            // Check for foreign elements which aren't punctuation, empty, or numbers
            else if (word.trim().length() > 0 && !"PUNCT".equals(tag)) {
                // Another regex to check if the word contains only digits. We need this to avoid flagging numbers
                if (!word.matches("\\d+")) {
                    foreignWords = true;
                }
            }
        }

        // Debug
        //LOGGER.info("ENGLISH: " + englishWords + " SPECIAL: " + foreignWords);
        return englishWords && foreignWords;
    }

    /**
     * Checks if the sentence's structure is "correct". The score system isn't really used
     */
    public static double calculateCorrectnessScore(AnalyzeSyntaxResponse response) {
        // Flags for the basic grammatical components
        boolean hasSubject = false;
        boolean hasMainVerb = false;
        boolean hasAnyVerb = false;

        Token rootToken = null;  // Stores the root token of the dependency tree

        // Go through every token (block) of the sentence
        for (Token token : response.getTokensList()) {
            PartOfSpeech pos = token.getPartOfSpeech();
            Tag tag = pos.getTag();  // Category
            Label depLabel = token.getDependencyEdge().getLabel();  // Dependency relationship

            // Find the root of the sentence
            if (depLabel == Label.ROOT) {
                rootToken = token;
            }

            // Look for subject: noun or pronoun with nominal subject relation
            if ((tag == Tag.NOUN || tag == Tag.PRON) &&
                    (depLabel == Label.NSUBJ || depLabel == Label.NSUBJPASS)) {
                hasSubject = true;
            }

            // Check if any verb exists in the sentence
            if (tag == Tag.VERB) {
                hasAnyVerb = true;
            }

            // Check if the root token is a verb, if its the main predicate
            if (depLabel == Label.ROOT && tag == Tag.VERB) {
                hasMainVerb = true;
            }
        }

        // Approximation for imperative: root is verb, first token is root and no subject
        boolean isImperative = false;
        if (rootToken != null) {

            Tag rootTag = rootToken.getPartOfSpeech().getTag();

            //LOGGER.info("Imperative check, hasSubject: " + hasSubject + ", tag: " + rootTag);

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

    /**
     * Summerizes the response from ModerateTextResponse
     */
    public String analyzeModeration(ModerateTextResponse response) {
        StringBuilder sb = new StringBuilder();

        List<ClassificationCategory> categories = response.getModerationCategoriesList();

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

        // For when there aren't notable categories (for now it applies only to categories that don't reach 50%)
        if(sb.toString().equals("MODERATION CATEGORIES FOUND:\n")) return "";
        return sb.toString();
    }

    /**
     * A Data Transfer Object, which just encapsulates the analysis results for simplified data transfer
     */
    public static class ControllerResponse{

        public AnalyzeSyntaxResponse response;
        public ModerateTextResponse moderateTextResponse;
        public boolean isEnglishSentence = true;
        public boolean hasForeign = false;
        public boolean isValid = false;

        public ControllerResponse(AnalyzeSyntaxResponse response){
            this.response = response;
        }
    }
}