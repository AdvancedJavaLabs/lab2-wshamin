package worker;

import common.ResultMessage;
import common.TaskMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TextProcessor {
    private final Set<String> positiveWords;
    private final Set<String> negativeWords;
    private final int topN;
    private final String replacingName;

    public TextProcessor() {
        this.positiveWords = loadWords("src/main/resources/positive.txt");
        this.negativeWords = loadWords("src/main/resources/negative.txt");
        this.topN = 10;
        this.replacingName = "NAME";
    }

    private Set<String> loadWords(String path) {
        try {
            return new HashSet<>(Files.readAllLines(Paths.get(path)));
        } catch (IOException e) {
            System.err.println("Warning: could not load words from " + path);
            return new HashSet<>();
        }
    }

    public ResultMessage process(TaskMessage task) {
        ResultMessage result = new ResultMessage();
        result.setJobId(task.getJobId());
        result.setSectionId(task.getSectionId());

        String text = task.getText();

        // Подсчет количества слов и частоты
        String[] tokens = text.toLowerCase().split("\\W+");
        Map<String, Integer> wordFreq = new HashMap<>();
        long wordCount = 0;
        for (String token : tokens) {
            if (!token.isEmpty()) {
                wordFreq.put(token, wordFreq.getOrDefault(token, 0) + 1);
                wordCount++;
            }
        }
        result.setWordCount(wordCount);
        result.setWordFreq(wordFreq);

        // top-N слов
        Map<String, Integer> topNWords = wordFreq.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(topN)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
        result.setTopN(topNWords);

        // Анализ тональности
        int pos = 0;
        int neg = 0;
        for (String token : tokens) {
            if (positiveWords.contains(token))
                pos++;
            if (negativeWords.contains(token))
                neg++;
        }
        result.setSentimentPos(pos);
        result.setSentimentNeg(neg);
        result.setSentimentScore(wordCount > 0 ? (double) (pos - neg) / wordCount : 0);

        // Замена имен
        Matcher matcher = Pattern.compile("\\b[A-Z][a-z]+\\b").matcher(text);
        String replacedText = matcher.replaceAll(replacingName);
        result.setReplacedText(replacedText);

        // Сортировка предложений
        String[] sentences = text.split("(?<=[.!?])\\s+");
        List<String> sortedSentences = Arrays.stream(sentences)
                .sorted(Comparator.comparingInt(String::length))
                .collect(Collectors.toList());
        result.setSortedSentences(sortedSentences);

        return result;
    }
}
