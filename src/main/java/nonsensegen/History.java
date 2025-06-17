package nonsensegen;
import java.util.List;

    public class History {

        private final List<String> history = new List<>();
        private final List<String> recentWords = new List<>();
        private static final int MAX_RECENT_WORDS = 20;

        // salva una frase generata in cronologia
        public void saveToHistory(String sentence) {
            history.add(sentence);
        }

        // restituisce la cronologia delle frasi generate
        public List<String> getHistory() {
            return history;
        }

        // salva una parola usata (da dizionario o input)
        public void addUsedWord(String word) {
            if (recentWords.size() >= MAX_RECENT_WORDS) {
                recentWords.removeFirst(); // rimuove la più vecchia quando la lista è piena
            }
            recentWords.add(word);
        }

        // restituisce la lista delle ultime 20 parole usate
        public List<String> getRecentWords() {
            return new List<>(recentWords);
        }
    }
}
