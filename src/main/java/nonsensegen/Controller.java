package nonsensegen;

import com.google.cloud.language.v1.AnalyzeSyntaxResponse;
import nonsensegen.parts.InputParts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class Controller { // Da rinominare a qualcosa come "NonsenseService"

    @Autowired
    private GoogleNlpService googleNlpService;
    @Autowired
    private InputParts inputParts;

    private static final Pattern ENGLISH_WORDS = Pattern.compile("^[a-zA-Z]+$");

    // Metodo aggiunto per analisi e output ordinato
    public String analizzaFrase(String sentence) {
        try {
            AnalyzeSyntaxResponse response = googleNlpService.analyzeSyntax(sentence);

            String detectedLanguage = response.getLanguage();
            if (!"en".equalsIgnoreCase(detectedLanguage)) {
                return "Sorry, at the moment only English language is supported.";
            }

            boolean englishWords = false;
            boolean foreignwords = false;

            for (var token : response.getTokensList()) {
                String word = token.getText().getContent();
                String tag = token.getPartOfSpeech().getTag().name();

                if (ENGLISH_WORDS.matcher(word).matches()) {
                    englishWords = true;
                } else if (word.trim().length() > 0 && !"PUNCT".equals(tag)) {
                    foreignwords = true;
                }
            }
            if (englishWords && foreignwords) {
                return "Be sure to insert only English words.";
            }

            // Suddivisione in categorie
            inputParts.estraiCategorie(response);

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
        } catch (Exception e) {
            return "Error analyzing text: " + e.getMessage();
        }
    }
    /*
    public boolean verificaFraseInput(){
        return false;
    }
    public void dividiInputParti(){}
    public void prendiPartiDizionario(){}
    public void controllaTossicita(){}

     */
}