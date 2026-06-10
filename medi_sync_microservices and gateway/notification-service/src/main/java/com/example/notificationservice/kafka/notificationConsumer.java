package com.example.notificationservice.kafka;

import appointment.events.AppointmentEvent;
import com.example.notificationservice.email.email_sender;
import com.google.protobuf.InvalidProtocolBufferException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class notificationConsumer {
    private static final Logger log = LoggerFactory.getLogger(notificationConsumer.class);
    private final email_sender emailSender;
    public notificationConsumer(email_sender emailSender) {
        this.emailSender = emailSender;
    }
    @KafkaListener(topics="appointment",groupId = "notification-service")
    public void consume(byte[] message){
        try {
            AppointmentEvent event = AppointmentEvent.parseFrom(message);
            String patientEmail = event.getPatientEmail();

            if (patientEmail == null || patientEmail.isEmpty()) {
                log.warn("No patient email in event for appointment: {}", event.getAppointmentId());
                return;
            }

            String text;
            if ("REJECTED".equals(event.getEventType())) {
                text = "Your appointment has been reviewed by our staff.\n\n" +
                        "Unfortunately your booking was not confirmed.\n" +
                        "Recommended Department: " + event.getDepartment() + "\n" +
                        "Recommended Doctor: " + event.getDoctorName() + "\n\n" +
                        "Please rebook using the above details.";
            } else {
                text = "Your appointment is confirmed!\n\n" +
                        "Doctor:" + event.getDoctorName() + "\n" +
                        "Department: " + event.getDepartment() + "\n" +
                        "Time: " + event.getAppointmentTime();
            }

            emailSender.sendEmail(patientEmail, text);
        }
        catch (InvalidProtocolBufferException e)
        {
            log.error("Error deserializing event {}", e.getMessage());
        }
    }
}
