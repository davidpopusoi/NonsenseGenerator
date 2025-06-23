package nonsensegen;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.language.v1.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

// Service to interact with the Google Cloud Natural Language API. Handles auth, client initialization and API calls
@Service
public class GoogleNlpService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleNlpService.class);

    // Path to the Google Cloud credentials .json file, which is injected from application.properties
    @Value("${google.credentials.path}")
    private String credentialsPath;

    // Google Cloud Language Service client, which we interact with to make our API calls
    private LanguageServiceClient languageClient;

    /**
     * Initializes Google NLP client on the application startup, creating a LanguageServiceClient instance after
     * authenticating with our credentials
     */
    @PostConstruct
    public void init() throws IOException {
        LOGGER.info("Initializing");

        GoogleCredentials credentials;

        // Load credentials from the .json file
        try (FileInputStream fis = new FileInputStream(credentialsPath)) {
            credentials = GoogleCredentials.fromStream(fis)
                    .createScoped("https://www.googleapis.com/auth/cloud-platform");
        }

        // Refresh the credentials once on startup
        credentials.refreshIfExpired();

        // Configure the settings for the instance, including our authentication method that utilises the credentials file
        LanguageServiceSettings settings = LanguageServiceSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();

        // Create the instance with our settings
        languageClient = LanguageServiceClient.create(settings);
    }

    /**
     * Analyzes sentence syntax using Google Natural Language API, returning an information rich object that contains
     * things like tokens, parts of speech tags, dependencies, etc.
     */
    public AnalyzeSyntaxResponse analyzeSyntax(String sentence) {
        return languageClient.analyzeSyntax(getDocument(sentence));
    }

    /**
     * Returns an analysis of the provided sentence, pointing out sensitive content
     */
    public ModerateTextResponse moderateText(String sentence){
        return languageClient.moderateText(getDocument(sentence));
    }

    /**
     * Creates the Document object from plain text, used in the other methods when interacting with the API
     */
    private Document getDocument(String sentence){
        return Document.newBuilder()
                .setContent(sentence)
                .setType(Document.Type.PLAIN_TEXT)
                .build();
    }
}