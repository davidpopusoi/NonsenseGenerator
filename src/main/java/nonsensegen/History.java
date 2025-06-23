package nonsensegen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// Component that tracks the generated sentences
@Component
public class History {

    private static final Logger LOGGER = LoggerFactory.getLogger(History.class);
    // Maximum number of history entries
    private static final int MAX_RECENT = 20;

    // List of the generated sentences
    private final List<String> history = new ArrayList<>();

    /**
     * Saves the provided sentence to the internal String List, while making sure that the limit constraints aren't
     * broken and that each added sentence is unique. When the list reaches the limit, the oldest one will be removed.
     */
    public void saveFinalToHistory(String sentence) {
        if(history.size() >= MAX_RECENT) history.remove(0);
        if(!history.contains(sentence)) {
            //LOGGER.info("Saved a sentence to history");
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

