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
import java.nio.file.Path;
import java.nio.file.Paths;

public class AggregatorMain {
    private static final String RESULTS_EXCHANGE = "results_exchange";
    private static final String RESULTS_QUEUE = "results_queue";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(RESULTS_EXCHANGE, "direct");
        channel.queueDeclare(RESULTS_QUEUE, true, false, false, null);
        channel.queueBind(RESULTS_QUEUE, RESULTS_EXCHANGE, "result");

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
                    System.out.println("Получен результат для раздела " + result.getSectionId()
                            + " задачи " + result.getJobId());
                    System.out.println("Получено: " + aggregator.getReceivedCount(result.getJobId())
                            + " из " + aggregator.getExpectedCount(result.getJobId()));
                }

                String jobId = node.get("jobId").asText();
                if (aggregator.isJobComplete(jobId)) {
                    System.out.println("Задача " + jobId + " завершена");
                    ResultMessage finalResult = aggregator.getFinalResult(jobId);

                    String json = JsonUtils.toJson(finalResult);
                    String filename = "result-" + jobId + ".json";

                    Path path = Paths.get(filename);
                    System.out.println("Пишу результат в: " + path.toAbsolutePath());
                    Files.write(path, json.getBytes());
                    System.out.println("Записан результат в " + filename);

                    aggregator.removeJob(jobId);
                }

                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

            } catch (Exception e) {
                e.printStackTrace();
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
            }
        };

        channel.basicConsume(RESULTS_QUEUE, false, deliverCallback, consumerTag -> {
        });
    }
}
