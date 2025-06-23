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

    private static final Logger LOGGER = LoggerFactory.getLogger(Controller.class);

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

    private static final Pattern ENGLISH_WORDS = Pattern.compile("^[a-zA-Z]+$");

    public class ControllerResponse{

        public AnalyzeSyntaxResponse response;
        public ModerateTextResponse moderateTextResponse;
        public boolean isEnglishSentence = true;
        public boolean hasForeign = false;
        public boolean isValid = false;

        public ControllerResponse(AnalyzeSyntaxResponse response){
            this.response = response;
        }
    }

    public @Nullable ControllerResponse analyzeSentence(String sentence) {
        LOGGER.info("Attempting to analyze: \"" + sentence + "\"");

        try {
            AnalyzeSyntaxResponse response = googleNlpService.analyzeSyntax(sentence);
            ControllerResponse cr = new ControllerResponse(response);

            if (!isEnglishSentence(sentence)) {
                cr.isEnglishSentence = false;
                //return "Sorry, at the moment only English language is supported.";
            }

            if (hasForeign(response)) {
                cr.hasForeign = true;
                //return "Be sure to insert only English words.";
            }

            if (isValidSentenceFlexible(response)){
                cr.isValid = true;

                ModerateTextResponse moderateTextResponse = googleNlpService.moderateText(sentence);
                cr.moderateTextResponse = moderateTextResponse;

                // Load the parts found in the input sentence
                inputParts.extractParts(response);
            }

            //return "Sentence is incorrect";
            return cr;
        } catch (Exception e) {
            System.out.println(e);
            //return "Error analyzing text: " + e.getMessage();
            return null;
        }
    }

    public String generateTemplate(){
        String finalTemplate = template.getTemplate(dictionaryParts, inputParts);
        history.saveFinalToHistory(finalTemplate);
        return finalTemplate;
    }

    /**
     * Checks if the provided sentence is in English
     */
    public boolean isEnglishSentence(String sentence) {
        // Use the instance googleNlpService and DO NOT make other instances of LanguageServiceClient
        AnalyzeSyntaxResponse response = googleNlpService.analyzeSyntax(sentence);
        String detectedLanguage = response.getLanguage(); // "en", "it", etc.

        return detectedLanguage.equals("en");
    }

    /**
     * Checks if the sentence has foreign elements (rogue spaces, etc.)
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
     * Checks if the sentence's structure is "correct"
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
}