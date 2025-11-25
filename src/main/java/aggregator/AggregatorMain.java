package aggregator;

import com.fasterxml.jackson.databind.JsonNode;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import common.ControlMessage;
import common.JsonUtils;
import common.ResultMessage;

import java.nio.file.Files;
import java.nio.file.Paths;

public class AggregatorMain {
    private final static String RESULTS_QUEUE = "results_queue";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(RESULTS_QUEUE, true, false, false, null);

        System.out.println("Аггрегатор запустился и ожидает результатов...");

        ResultAggregator aggregator = new ResultAggregator();

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");

            try {
                JsonNode node = JsonUtils.readTree(message);
                String type = node.has("type") ? node.get("type").asText() : "RESULT";

                if ("END".equals(type)) {
                    ControlMessage end = JsonUtils.fromJson(message, ControlMessage.class);
                    aggregator.onEnd(end.jobId, end.totalSections);

                    System.out.println("Получен END для jobId=" + end.jobId
                            + ", totalSections=" + end.totalSections);

                } else {
                    ResultMessage result = JsonUtils.fromJson(message, ResultMessage.class);
                    aggregator.addResult(result);

                    System.out.println("Получен результат для раздела "
                            + result.getSectionId() + " задачи " + result.getJobId());

                    System.out.println("Получено: "
                            + aggregator.getReceivedCount(result.getJobId())
                            + " из " + aggregator.getExpectedCount(result.getJobId()));
                }

                // 4) после любого сообщения проверяем завершение
                String jobId = node.get("jobId").asText();
                if (aggregator.isJobComplete(jobId)) {
                    System.out.println("Задача " + jobId + " завершена!");
                    ResultMessage finalResult = aggregator.getFinalResult(jobId);

                    String json = JsonUtils.toJson(finalResult);
                    String filename = "result-" + jobId + ".json";
                    Files.write(Paths.get(filename), json.getBytes());
                    System.out.println("Записан результат в " + filename);

                    aggregator.removeJob(jobId);
                }

            } catch (Exception e) {
                System.err.println("Ошибка обработки результата: " + e.getMessage());
                e.printStackTrace();
            }
        };

        channel.basicConsume(RESULTS_QUEUE, true, deliverCallback, consumerTag -> {
        });
    }
}
