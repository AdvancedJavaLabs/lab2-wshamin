package producer;

import java.util.ArrayList;
import java.util.List;

public class TextSplitter {
    public List<String> splitByParagraphs(String text) {
        String[] paragraphs = text.split("\\n\\s*\\n");
        List<String> sections = new ArrayList<>();
        for (String p : paragraphs) {
            if (!p.trim().isEmpty()) {
                sections.add(p);
            }
        }
        return sections;
    }
}
