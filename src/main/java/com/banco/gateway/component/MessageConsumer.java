package com.banco.gateway.component;

import jakarta.jms.TextMessage;
import lombok.extern.log4j.Log4j2;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class MessageConsumer {

    @JmsListener(destination = "auth-queue", containerFactory = "jmsListenerContainerFactory")
    public void messageListener(Object eventMessage) {
        try {
            String jsonMessage;

            if (eventMessage instanceof ActiveMQTextMessage activemqtextmessage) {
                jsonMessage = activemqtextmessage.getText();
            } else if (eventMessage instanceof TextMessage textMessage) {
                jsonMessage = textMessage.getText();
            } else {
                log.warn("Tipo de mensaje desconocido: {}", eventMessage.getClass().getName());
                return;
            }
            log.info("JSON Message received: {}", jsonMessage);

        } catch (Exception e) {
            log.error("Error processing message: {}", e.getMessage(), e);
        }
    }
}
