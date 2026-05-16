package com.partition.domain.alarm.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.partition.entity.enums.AlarmType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class FcmService {

    public void sendPush(String fcmToken, AlarmType type) {
        sendPush(fcmToken, type.getMessage());
    }

    public void sendPush(String fcmToken, String body) {
        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle("Partition")
                        .setBody(body)
                        .build())
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.warn("FCM 전송 실패 - token: {}, error: {}", fcmToken, e.getMessage());
        }
    }

    public void sendNearHomeNotification(String fcmToken, String senderName) {
        String safeName = (senderName != null && !senderName.isBlank()) ? senderName : "사용자";
        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle("파티션")
                        .setBody(safeName + "님이 집 근처에 있는 것 같아요.")
                        .build())
                .putData("type", "NEAR_HOME_ARRIVAL")
                .putData("senderName", safeName)
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.warn("FCM 전송 실패 (near-home) - token: {}, error: {}", fcmToken, e.getMessage());
        }
    }
}