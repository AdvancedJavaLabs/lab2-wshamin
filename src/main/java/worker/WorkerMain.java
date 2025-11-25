package worker;

import java.nio.charset.StandardCharsets;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.MessageProperties;
import common.JsonUtils;
import common.ResultMessage;
import common.TaskMessage;

public class WorkerMain {
    private final static String TASKS_QUEUE = "tasks_queue";
    private final static String RESULTS_EXCHANGE = "results_exchange";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(TASKS_QUEUE, true, false, false, null);
        channel.basicQos(1);

        channel.exchangeDeclare(RESULTS_EXCHANGE, "direct");

        System.out.println("Воркер запустился и ожидает сообщения...");

        TextProcessor processor = new TextProcessor();

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("Получен таск");

            try {
                TaskMessage task = JsonUtils.fromJson(message, TaskMessage.class);
                ResultMessage result = processor.process(task);
                result.setTotalSections(task.getTotalSections());

                String resultJson = JsonUtils.toJson(result);
                channel.basicPublish(RESULTS_EXCHANGE, "result",
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        resultJson.getBytes(StandardCharsets.UTF_8));

                System.out.println("Обрабатываем таск " + task.getSectionId());
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            } catch (Exception e) {
                System.err.println("Ошибка при обработке таска: " + e.getMessage());
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
            }
        };

        channel.basicConsume(TASKS_QUEUE, false, deliverCallback, consumerTag -> {
        });
    }
}
