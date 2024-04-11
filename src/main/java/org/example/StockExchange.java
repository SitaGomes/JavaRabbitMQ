package org.example;

import com.rabbitmq.client.*;

import java.util.LinkedList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class StockExchange {
    private final static String QUEUE_NAME = "BROKER";
    private final static String EXCHANGE_NAME = "BOLSADEVALORES";
    private final static String ROUTING_KEY = "order.*";

    private Broker broker = new Broker();

    private Map<String, List<Order>> orderBook = new HashMap<>();

    public void start() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername("admin");
        factory.setPassword("123456");
        factory.setVirtualHost("/");
        factory.setPort(5672);
        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println(" [StockExchange] Received '" + message + "'");
                try {
                    processMessage(message, channel);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
            });

            // ‘Loop’ para manter acordado
            while (true) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void processMessage(String message, Channel channel) throws Exception {
        String[] parts = message.split(" ");
        String operation = parts[0];
        int quantity = Integer.parseInt(parts[1]);
        double price = Double.parseDouble(parts[2]);
        String stockSymbol = parts[3];

        matchOrders(stockSymbol, operation, quantity, price, channel);
    }

    private void matchOrders(String stockSymbol, String operation, int quantity, double price, Channel channel)
            throws Exception {
        List<Order> orders = orderBook.getOrDefault(stockSymbol, new LinkedList<>());

        if (operation.equals(Operation.BUY.toString())) {
            for (Order order : orders) {
                boolean isValidBuyOrder = price >= order.price && quantity == order.quantity;
                if (order.operation.toString().equals(Operation.SELL.toString()) && isValidBuyOrder) {
                    System.out.println(" [StockExchange Transaction]: Compra realizada -> " + quantity + " ações "
                            + stockSymbol + " a R$" + price);
                    String updateMessage = "Transaction: Compra " + quantity + " ações " + stockSymbol + " a R$"
                            + price;
                    broker.sendStockInfoRequest(stockSymbol, updateMessage);
                    orders.remove(order);
                    return;
                }
            }

            orders.add(new Order(Operation.BUY, quantity, price));
            orderBook.put(stockSymbol, orders);

        } else if (operation.equals(Operation.SELL.toString())) {
            for (Order order : orders) {
                boolean isValidSellOrder = price >= order.price && quantity == order.quantity;
                if (order.operation.toString().equals(Operation.BUY.toString()) && isValidSellOrder) {
                    System.out.println(" [StockExchange Transaction]: Venda realizada -> " + quantity + " ações "
                            + stockSymbol + " a R$" + price);
                    String updateMessage = "Transaction: Venda " + quantity + " ações " + stockSymbol + " a R$" + price;
                    broker.sendStockInfoRequest(stockSymbol, updateMessage);
                    orders.remove(order);
                    return;
                }
            }

            orders.add(new Order(Operation.SELL, quantity, price));
            orderBook.put(stockSymbol, orders);
        }
    }

    public static void main(String[] argv) throws Exception {
        StockExchange stockExchange = new StockExchange();
        stockExchange.start();
    }
}
