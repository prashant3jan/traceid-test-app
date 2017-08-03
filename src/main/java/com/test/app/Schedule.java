package com.test.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class Schedule {

    private static final Logger LOG = LoggerFactory.getLogger(Schedule.class);
    private MessageChannel inboundChannel;

    @Autowired
    public void setInboundChannel(@Qualifier(FlowConfiguration.INBOUND_CHANNEL) MessageChannel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    @Scheduled(fixedDelayString = "${test.app.schedule}")
    public void schedule() {
        LOG.info("Sending message...");

        this.inboundChannel.send(MessageBuilder
                .withPayload("Testing")
                .build()
        );
    }
}
