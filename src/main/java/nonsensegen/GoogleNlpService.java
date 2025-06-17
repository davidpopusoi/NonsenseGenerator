package nonsensegen;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.language.v1.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

// SOLO questa aggiunta: ora restituisce AnalyzeSyntaxResponse invece di String
@Service
public class GoogleNlpService {

    @Value("${google.credentials.path}")
    private String credentialsPath;

    public AnalyzeSyntaxResponse analyzeSyntax(String text) throws IOException {

        GoogleCredentials credentials;

        try (FileInputStream fis = new FileInputStream(credentialsPath)) {
            credentials = GoogleCredentials.fromStream(fis)
                    .createScoped("https://www.googleapis.com/auth/cloud-platform");
        }

        credentials.refreshIfExpired();

        LanguageServiceSettings settings = LanguageServiceSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();

        try (LanguageServiceClient language = LanguageServiceClient.create(settings)) {
            Document doc = Document.newBuilder()
                    .setContent(text)
                    .setType(Document.Type.PLAIN_TEXT)
                    .build();

            return language.analyzeSyntax(doc);
        }
    }
}