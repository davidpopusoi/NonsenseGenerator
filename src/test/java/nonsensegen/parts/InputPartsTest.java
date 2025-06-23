package nonsensegen.parts;

import com.google.cloud.language.v1.AnalyzeSyntaxResponse;
import com.google.cloud.language.v1.PartOfSpeech;
import com.google.cloud.language.v1.TextSpan;
import com.google.cloud.language.v1.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class InputPartsTest {

    private InputParts inputParts;

    @BeforeEach
    void setUp() {
        inputParts = new InputParts();
    }

    @Test
    void extractParts_correctlyExtractsVerbWithParticle() {
        Token verb = createToken("pick", PartOfSpeech.Tag.VERB);
        Token prt = createToken("up", PartOfSpeech.Tag.PRT);

        AnalyzeSyntaxResponse response = AnalyzeSyntaxResponse.newBuilder()
                .addTokens(verb)
                .addTokens(prt)
                .build();

        inputParts.extractParts(response);

        List<String> verbs = inputParts.getVerb();
        assertTrue(verbs.contains("pick up"));
    }

    @Test
    void extractParts_handlesInvalidCharacters() {
        Token xToken = createToken("$$$", PartOfSpeech.Tag.X);
        AnalyzeSyntaxResponse response = AnalyzeSyntaxResponse.newBuilder().addTokens(xToken).build();

        inputParts.extractParts(response);
        assertTrue(inputParts.getInvalid().contains("$$$"));
    }

    private Token createToken(String word, PartOfSpeech.Tag tag) {
        return Token.newBuilder()
                .setText(TextSpan.newBuilder().setContent(word))
                .setPartOfSpeech(PartOfSpeech.newBuilder().setTag(tag))
                .build();
    }
}
