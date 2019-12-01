package com.thunderlane;

import com.rabbitmq.client.*;
import com.thunderlane.adstxt.CommandConsumer;
import com.thunderlane.adstxt.CommandConsumerImpl;
import com.thunderlane.config.Config;
import com.thunderlane.config.ConfigImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class CommandWorker {

    private static final Logger LOG = LoggerFactory.getLogger(Config.class);

    public static void main(String[] args) {
        Config config = ConfigImpl.getInstance();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(config.getProperty("rabbitmq.host"));
        factory.setPort(Integer.parseInt(config.getProperty("rabbitmq.port")));
        factory.setUsername(config.getProperty("rabbitmq.user"));
        factory.setPassword(config.getProperty("rabbitmq.password"));

        try {
            Connection connection = factory.newConnection();
            CommandConsumer commandConsumer = new CommandConsumerImpl(connection);
            commandConsumer.run();
        } catch (IOException | TimeoutException exception) {
            LOG.error("Error consuming queue", exception);
            System.exit(2);
        }
    }
}
