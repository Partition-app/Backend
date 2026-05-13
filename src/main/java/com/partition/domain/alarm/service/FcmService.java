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
        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle("Partition")
                        .setBody(type.getMessage())
                        .build())
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.warn("FCM 전송 실패 - token: {}, error: {}", fcmToken, e.getMessage());
        }
    }

    public void sendNearHomeNotification(String fcmToken, String senderName) {
        Message message = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle("파티션")
                        .setBody(senderName + "님이 집 근처에 있는 것 같아요.")
                        .build())
                .putAllData(Map.of("type", "NEAR_HOME_ARRIVAL", "senderName", senderName))
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.warn("FCM 전송 실패 (near-home) - token: {}, error: {}", fcmToken, e.getMessage());
        }
    }
}