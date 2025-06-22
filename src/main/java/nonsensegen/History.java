package nonsensegen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class History {

    private static final Logger LOGGER = LoggerFactory.getLogger(History.class);

    private static final int MAX_RECENT = 20;
    private final List<String> history = new ArrayList<>();
    private final List<String> recentWords = new ArrayList<>();

    public void saveFinalToHistory(String sentence) {
        if(history.size() >= MAX_RECENT) history.remove(0);
        if(!history.contains(sentence)) {
            LOGGER.info("Saved a sentence to history");
            history.add(sentence);
        }
    }

    /**
     * Returns a String List with the last 20 generated sentences
     */
    public List<String> getHistory() {
        return history;
    }
}

