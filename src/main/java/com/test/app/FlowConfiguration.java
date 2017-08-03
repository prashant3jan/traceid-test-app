package com.test.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class FlowConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(FlowConfiguration.class);
    static final String INBOUND_CHANNEL = "inbound-channel";
    private static final String TASK_EXECUTOR = "task-executor";
    private static final String MESSAGE_FLOW = "message-flow";
    private static final String ERROR_FLOW = "error-flow";

    @Bean(name = INBOUND_CHANNEL)
    public MessageChannel getInboundChannel() {
        return MessageChannels.direct().get();
    }

    @Bean(name = TASK_EXECUTOR)
    public TaskExecutor getTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("TestApp-");
        taskExecutor.setCorePoolSize(5);
        return taskExecutor;
    }

    @Bean(name = MESSAGE_FLOW)
    @Autowired
    public IntegrationFlow getMessageFlow(@Qualifier(INBOUND_CHANNEL)MessageChannel messageChannel,
                                          @Qualifier(TASK_EXECUTOR)TaskExecutor taskExecutor) {
        return IntegrationFlows
                .from(messageChannel)
                .channel(channels -> channels.executor(taskExecutor))
                .handle(message -> {
                    LOG.info("Throwing error");
                    throw new RuntimeException("Test exception");
                })
                .get();
    }

    @Bean(name = ERROR_FLOW)
    public IntegrationFlow getErrorIntegrationFlow() {
        return IntegrationFlows
                .from(MessageHeaders.ERROR_CHANNEL)
                .handle(message ->
                    // Payload should be a MessageDeliveryException
                    LOG.error("\n\n*********************************************************************************\n" +
                            "* Shouldn't the payload type of messages \n" +
                            "* going to the error channel be 'MessageDeliveryException'?\n" +
                            "* Here's what it really is: '{}' \n" +
                            "*********************************************************************************\n\n", message.getPayload().getClass())
                ).get();
    }
}
