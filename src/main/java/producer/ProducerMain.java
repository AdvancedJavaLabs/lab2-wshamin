package producer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import common.ControlMessage;
import common.JsonUtils;
import common.TaskMessage;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class ProducerMain {
    private final static String TASKS_EXCHANGE = "tasks_exchange";
    private final static String TASKS_QUEUE = "tasks_queue";
    private final static String RESULTS_EXCHANGE = "results_exchange";
    private final static String RESULTS_QUEUE = "results_queue";
    private final static int MAX_SECTION_CHARS = 1000000;

    public static void main(String[] args) throws Exception {
        String corpusPath = args.length > 0 ? args[0] : "corpus.txt";

        long startTimeMillis = System.currentTimeMillis();

        Path corpusFile = Paths.get(corpusPath);
        long corpusSizeBytes = Files.size(corpusFile);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(TASKS_EXCHANGE, "direct");
            channel.queueDeclare(TASKS_QUEUE, true, false, false, null);
            channel.queueBind(TASKS_QUEUE, TASKS_EXCHANGE, "task");

            channel.exchangeDeclare(RESULTS_EXCHANGE, "direct");
            channel.queueDeclare(RESULTS_QUEUE, true, false, false, null);
            channel.queueBind(RESULTS_QUEUE, RESULTS_EXCHANGE, "result");

            String jobId = UUID.randomUUID().toString();
            System.out.println("Job ID: " + jobId);

            int sectionId = 0;

            try (BufferedReader reader = Files.newBufferedReader(corpusFile)) {
                String line;
                StringBuilder sectionBuilder = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) {
                        if (sectionBuilder.length() > 0) {
                            sendTask(channel, jobId, sectionId++, sectionBuilder.toString());
                            sectionBuilder.setLength(0);
                        }
                        continue;
                    }

                    sectionBuilder.append(line).append('\n');

                    if (sectionBuilder.length() >= MAX_SECTION_CHARS) {
                        sendTask(channel, jobId, sectionId++, sectionBuilder.toString());
                        sectionBuilder.setLength(0);
                    }
                }

                if (sectionBuilder.length() > 0) {
                    sendTask(channel, jobId, sectionId++, sectionBuilder.toString());
                }
            }

            sendEnd(channel, jobId, sectionId, corpusPath, corpusSizeBytes, startTimeMillis);
        }
    }

    private static void sendTask(Channel channel, String jobId, int sectionId, String text) throws Exception {
        TaskMessage task = new TaskMessage(jobId, sectionId, -1, text);
        String json = JsonUtils.toJson(task);

        channel.basicPublish(TASKS_EXCHANGE, "task", MessageProperties.PERSISTENT_TEXT_PLAIN, json.getBytes(StandardCharsets.UTF_8));

        if (sectionId % 100 == 0) {
            System.out.println("Отправлена задача " + sectionId);
        }
    }

    private static void sendEnd(Channel channel,
                                String jobId,
                                int totalSections,
                                String corpusPath,
                                long corpusSizeBytes,
                                long startTimeMillis) throws Exception {

        ControlMessage end = new ControlMessage(
                jobId,
                totalSections,
                corpusPath,
                corpusSizeBytes,
                startTimeMillis
        );

        String json = JsonUtils.toJson(end);

        channel.basicPublish(
                RESULTS_EXCHANGE,
                "result",
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                json.getBytes(StandardCharsets.UTF_8)
        );

        System.out.println("Отправляем завершающее сообщение END. Всего секций = " + totalSections);
    }

}
