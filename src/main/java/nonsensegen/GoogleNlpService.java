package nonsensegen;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.language.v1.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;

@Service
public class GoogleNlpService {

    @Value("${google.credentials.path}")
    private String credentialsPath;

    public String analyzeSyntax(String text) throws IOException {

        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new FileInputStream(credentialsPath));

        LanguageServiceSettings settings = LanguageServiceSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();

        try (LanguageServiceClient language = LanguageServiceClient.create(settings)) {
            Document doc = Document.newBuilder()
                    .setContent(text)
                    .setType(Document.Type.PLAIN_TEXT)
                    .build();

            AnalyzeSyntaxResponse response = language.analyzeSyntax(doc);

            StringBuilder result = new StringBuilder();
            for (Token token : response.getTokensList()) {
                result.append(token.getText().getContent())
                        .append(" (")
                        .append(token.getPartOfSpeech().getTag())
                        .append(")\n");
            }

            return result.toString();
        }
    }
}