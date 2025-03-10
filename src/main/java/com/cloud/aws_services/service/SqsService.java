package com.cloud.aws_services.service;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;

/**
 * Service for interacting with Amazon SQS.
 * Provides methods for sending, receiving, and deleting messages
 */
@AllArgsConstructor
@Service
public class SqsService {

    private static final Logger logger = LoggerFactory.getLogger(SqsService.class);
    private final SqsClient sqsClient;

    /**
     * Sends a message to the specified SQS queue.
     *
     * @param queueUrl The URL of the SQS queue.
     * @param message  The message body.
     * @return The ID of the sent message.
     */
    public String sendMessage(String queueUrl, String message) {
        try {
            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(message)
                    .build();
            String messageId = sqsClient.sendMessage(request).messageId();
            logger.info("Message sent successfully. Message ID: {}", messageId);
            return messageId;
        } catch (Exception e) {
            logger.error("Error sending message to queue {}: {}", queueUrl, e.getMessage(), e);
            throw new RuntimeException("Failed to send message", e);
        }
    }

    /**
     * Receives messages from the specified SQS queue.
     *
     * @param queueUrl        The URL of the SQS queue.
     * @param maxMessages     Maximum number of messages to retrieve.
     * @param waitTimeSeconds Wait time for long polling.
     * @return A list of messages.
     */
    public List<Message> receiveMessages(String queueUrl, int maxMessages, int waitTimeSeconds) {
        try {
            ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(maxMessages)
                    .waitTimeSeconds(waitTimeSeconds)
                    .build();
            ReceiveMessageResponse response = sqsClient.receiveMessage(request);
            List<Message> messages = response.messages();
            logger.info("Received {} messages from queue {}", messages.size(), queueUrl);
            return messages;
        } catch (Exception e) {
            logger.error("Error receiving messages from queue {}: {}", queueUrl, e.getMessage(), e);
            throw new RuntimeException("Failed to receive messages", e);
        }
    }

    /**
     * Deletes a message from the specified SQS queue.
     *
     * @param queueUrl      The URL of the SQS queue.
     * @param receiptHandle The receipt handle of the message.
     */
    public void deleteMessage(String queueUrl, String receiptHandle) {
        try {
            DeleteMessageRequest request = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(receiptHandle)
                    .build();
            sqsClient.deleteMessage(request);
            logger.info("Deleted message with receipt handle {} from queue {}", receiptHandle, queueUrl);
        } catch (Exception e) {
            logger.error("Error deleting message from queue {}: {}", queueUrl, e.getMessage(), e);
            throw new RuntimeException("Failed to delete message", e);
        }
    }
}