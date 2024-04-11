package org.example;

import com.rabbitmq.client.*;

public class Broker {
    private final static String QUEUE_NAME = "BROKER";
    private final static String EXCHANGE_NAME = "BOLSADEVALORES";
    private final static String UPDATE_ROUTING_KEY = "update.#";

    public void listenToUpdates(String stockSymbol) throws Exception {
        Thread listenerThread = new Thread(() -> {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost("localhost");
                factory.setUsername("admin");
                factory.setPassword("123456");
                factory.setVirtualHost("/");
                factory.setPort(5672);
                try (Connection connection = factory.newConnection();
                        Channel channel = connection.createChannel()) {

                    String routingKey = UPDATE_ROUTING_KEY + stockSymbol;
                    channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
                    String queueName = channel.queueDeclare().getQueue();
                    channel.queueBind(queueName, EXCHANGE_NAME, routingKey);

                    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                        String message = new String(delivery.getBody(), "UTF-8");
                        System.out.println(" [Broker] Update Received: '" + message + "'");
                    };
                    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
                    });

                    // Keep listening for updates
                    while (!Thread.currentThread().isInterrupted()) {
                        Thread.sleep(1000);
                    }
                }
            } catch (Exception e) {
                System.out.println("Error in listener thread: " + e.getMessage());
            }
        });

        listenerThread.start();
    }

    public void sendBuyOrder(String message) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("admin");
        factory.setPassword("123456");
        factory.setVirtualHost("/");
        factory.setPort(5672);
        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String buyOrder = Operation.BUY + " " + message;
            channel.basicPublish("", QUEUE_NAME, null, buyOrder.getBytes());
            System.out.println(" [Broker] Sent '" + buyOrder + "'");
        }
    }

    public void sendSellOrder(String message) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("admin");
        factory.setPassword("123456");
        factory.setVirtualHost("/");
        factory.setPort(5672);
        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String sellOrder = Operation.SELL + " " + message;
            channel.basicPublish("", QUEUE_NAME, null, sellOrder.getBytes());
            System.out.println(" [Broker] Sent '" + sellOrder + "'");
        }
    }

    public void sendStockInfoRequest(String stockSymbol, String message) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("admin");
        factory.setPassword("123456");
        factory.setVirtualHost("/");
        factory.setPort(5672);
        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.basicPublish(EXCHANGE_NAME, "update." + stockSymbol, null, message.getBytes());
        }
    }

    public static void main(String[] args) {

        String stockSymbol = args[0] != "" ? "PETR4" : args[0];
        Broker broker = new Broker();
        try {
            broker.listenToUpdates(stockSymbol);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
