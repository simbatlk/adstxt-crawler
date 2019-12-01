package com.thunderlane.adstxt;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import com.thunderlane.config.Config;
import com.thunderlane.config.ConfigImpl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class CommandConsumerImpl implements CommandConsumer {

    private final Object monitor = new Object();
    private Connection connection;
    private Channel channel;
    private DeliverCallback deliverCallback;
    private Config config = ConfigImpl.getInstance();

    public CommandConsumerImpl(Connection connection) throws IOException {
        this.connection = connection;
        this.setUpChannel();
        this.setDeliveryCallback();
    }

    @Override
    public void run() throws IOException {
        this.channel.basicConsume(config.getProperty("rabbitmq.commandQueue"), false, this.deliverCallback, (consumerTag -> {}));
        while (true) {
            synchronized (this.monitor) {
                try {
                    this.monitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setUpChannel() throws IOException {
        this.channel = this.connection.createChannel();
        this.channel.queueDeclare(config.getProperty("rabbitmq.commandQueue"), true, false, false, null);
        this.channel.basicQos(1);
    }

    private void setDeliveryCallback() {
        this.deliverCallback = (consumerTag, deliveredMessage) -> {
            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(deliveredMessage.getProperties().getCorrelationId())
                    .build();

            String response = "";

            try {
                String message = new String(deliveredMessage.getBody(), StandardCharsets.UTF_8);
                JSONArray parsedArray = (JSONArray) (new JSONParser()).parse(message);

                for (JSONObject website : (Iterable<JSONObject>) parsedArray) {
                    System.out.printf("Parsing website id %s with domain %s%n", website.get("id"), website.get("domain"));
                    System.out.println("Parsed!");
                }

                response = "abc";
            } catch (RuntimeException | ParseException e) {
                System.out.println(" [.] " + e.toString());
            } finally {
                this.channel.basicPublish("", deliveredMessage.getProperties().getReplyTo(), replyProps, response.getBytes(StandardCharsets.UTF_8));
                this.channel.basicAck(deliveredMessage.getEnvelope().getDeliveryTag(), false);

                synchronized (this.monitor) {
                    this.monitor.notify();
                }
            }
        };
    }
}
