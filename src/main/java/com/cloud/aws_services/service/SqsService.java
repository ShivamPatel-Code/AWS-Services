package com.cloud.aws_services.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;


@AllArgsConstructor
@Service
public class SqsService {
    private final SqsClient sqsClient;

    // Send a message to the specified queue
    public String sendMessage(String queueUrl, String message) {
        SendMessageRequest request = SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(message)
                .build();
        return sqsClient.sendMessage(request).messageId();
    }

    // Receive messages from the specified queue. maxMessages = 10
    public List<Message> receiveMessages(String queueUrl, int maxMessages, int waitTimeSeconds) {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(maxMessages)
                .waitTimeSeconds(waitTimeSeconds)
                .build();
        ReceiveMessageResponse response = sqsClient.receiveMessage(request);
        return response.messages();
    }

    // Delete a message from the specified queue using its receipt handle.
    public void deleteMessage(String queueUrl, String receiptHandle) {
        DeleteMessageRequest request = DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(receiptHandle)
                .build();
        sqsClient.deleteMessage(request);
    }
}
