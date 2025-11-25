package worker;

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
        channel.basicQos(1); // Fair dispatch

        System.out.println("Worker started. Waiting for messages...");

        TextProcessor processor = new TextProcessor();

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("Received task");

            try {
                TaskMessage task = JsonUtils.fromJson(message, TaskMessage.class);
                ResultMessage result = processor.process(task);
                result.setTotalSections(task.getTotalSections());

                String resultJson = JsonUtils.toJson(result);
                channel.basicPublish(RESULTS_EXCHANGE, "result",
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        resultJson.getBytes("UTF-8"));

                System.out.println("Processed task " + task.getSectionId());
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            } catch (Exception e) {
                System.err.println("Error processing task: " + e.getMessage());
                e.printStackTrace();
                // Optionally nack or reject
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, true);
            }
        };
        channel.basicConsume(TASKS_QUEUE, false, deliverCallback, consumerTag -> {
        });
    }
}
