package com.banco.gateway.component;

import jakarta.jms.JMSException;
import jakarta.jms.TextMessage;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.banco.gateway.dto.response.TokenEvent;
import com.banco.gateway.service.ITokenCacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Log4j2
@Component
@AllArgsConstructor
public class MessageConsumer {

    private final ITokenCacheService tokenCacheService;
    private final ObjectMapper objectMapper;

    @JmsListener(destination = "auth-topic", containerFactory = "jmsListenerContainerFactory")
    public void messageListener(Object eventMessage) {
        try {
            String jsonMessage = extractMessageContent(eventMessage);
            log.info("JSON Message received: {}", jsonMessage);

            TokenEvent event = objectMapper.readValue(jsonMessage, TokenEvent.class);

            if ("LOGOUT".equalsIgnoreCase(event.getEventType())) {
                tokenCacheService.markTokenAsRevokedAndExpired(event.getToken());
                log.info("Token {} marcado como revocado y expirado en cache", event.getToken());
            }

        } catch (JsonProcessingException e) {
            log.error("Error deserializando mensaje JSON: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error procesando mensaje: {}", e.getMessage(), e);
        }
    }

    private String extractMessageContent(Object message) throws JMSException {
        if (message instanceof ActiveMQTextMessage amqTextMessage) {
            return amqTextMessage.getText();
        } else if (message instanceof TextMessage textMessage) {
            return textMessage.getText();
        }
        throw new JMSException("Tipo de mensaje no soportado: " + message.getClass().getName());
    }
}
