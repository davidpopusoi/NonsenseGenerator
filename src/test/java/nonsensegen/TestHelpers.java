package nonsensegen;

import com.google.cloud.language.v1.*;

import java.util.List;

public class TestHelpers {

    public static AnalyzeSyntaxResponse createValidAnalyzeSyntaxResponse(String language) {
        Token subject = Token.newBuilder()
                .setText(TextSpan.newBuilder().setContent("dog"))
                .setPartOfSpeech(PartOfSpeech.newBuilder().setTag(PartOfSpeech.Tag.NOUN))
                .setDependencyEdge(DependencyEdge.newBuilder().setLabel(DependencyEdge.Label.NSUBJ))
                .build();

        Token verb = Token.newBuilder()
                .setText(TextSpan.newBuilder().setContent("runs"))
                .setPartOfSpeech(PartOfSpeech.newBuilder().setTag(PartOfSpeech.Tag.VERB))
                .setDependencyEdge(DependencyEdge.newBuilder().setLabel(DependencyEdge.Label.ROOT))
                .build();

        return AnalyzeSyntaxResponse.newBuilder()
                .addAllTokens(List.of(subject, verb))
                .setLanguage(language)
                .build();
    }

    // Has no flags
    public static ModerateTextResponse createModerateTextResponse() {
        return ModerateTextResponse.newBuilder().build();
    }

    public static AnalyzeSyntaxResponse createMixedResponse(String first, String second) {
        Token token1 = Token.newBuilder()
                .setText(TextSpan.newBuilder().setContent(first))
                .setPartOfSpeech(PartOfSpeech.newBuilder().setTag(PartOfSpeech.Tag.NOUN))
                .build();

        Token token2 = Token.newBuilder()
                .setText(TextSpan.newBuilder().setContent(second))
                .setPartOfSpeech(PartOfSpeech.newBuilder().setTag(PartOfSpeech.Tag.NOUN))
                .build();

        return AnalyzeSyntaxResponse.newBuilder()
                .addAllTokens(List.of(token1, token2))
                .setLanguage("en")
                .build();
    }

    public static AnalyzeSyntaxResponse createImperativeResponse() {
        Token verb = Token.newBuilder()
                .setText(TextSpan.newBuilder().setContent("Run"))
                .setPartOfSpeech(PartOfSpeech.newBuilder().setTag(PartOfSpeech.Tag.VERB))
                .setDependencyEdge(DependencyEdge.newBuilder().setLabel(DependencyEdge.Label.ROOT))
                .build();

        return AnalyzeSyntaxResponse.newBuilder()
                .addTokens(verb)
                .setLanguage("en")
                .build();
    }
}
