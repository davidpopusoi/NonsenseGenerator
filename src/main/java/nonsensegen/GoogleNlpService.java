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

// SOLO questa aggiunta: ora restituisce AnalyzeSyntaxResponse invece di String
@Service
public class GoogleNlpService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GoogleNlpService.class);

    @Value("${google.credentials.path}")
    private String credentialsPath;
    private LanguageServiceClient languageClient;

    @PostConstruct
    public void init() throws IOException {
        LOGGER.info("Initializing");

        GoogleCredentials credentials;
        try (FileInputStream fis = new FileInputStream(credentialsPath)) {
            credentials = GoogleCredentials.fromStream(fis)
                    .createScoped("https://www.googleapis.com/auth/cloud-platform");
        }

        // Refresh token once on startup
        credentials.refreshIfExpired();

        LanguageServiceSettings settings = LanguageServiceSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();

        languageClient = LanguageServiceClient.create(settings);
    }

    public AnalyzeSyntaxResponse analyzeSyntax(String sentence) {
        return languageClient.analyzeSyntax(getDocument(sentence));
    }

    public ModerateTextResponse moderateText(String sentence){
        return languageClient.moderateText(getDocument(sentence));
    }

    private Document getDocument(String sentence){
        return Document.newBuilder()
                .setContent(sentence)
                .setType(Document.Type.PLAIN_TEXT)
                .build();
    }
}