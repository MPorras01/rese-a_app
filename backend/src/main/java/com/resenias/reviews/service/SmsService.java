package com.resenias.reviews.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    private final String accountSid;
    private final String authToken;
    private final String fromPhone;
    private final Environment environment;
    private boolean twilioInitialized = false;

    public SmsService(@Value("${twilio.account-sid:}") String accountSid,
                      @Value("${twilio.auth-token:}") String authToken,
                      @Value("${twilio.from-phone:}") String fromPhone,
                      Environment environment) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromPhone = fromPhone;
        this.environment = environment;

        if (isProductionMode() && hasValidTwilioConfig()) {
            initializeTwilio();
        }
    }

    /**
     * Send OTP code via SMS
     * 
     * @param phoneNumber Recipient phone number (E.164 format)
     * @param code OTP code to send
     * @return true if sent successfully, false otherwise
     */
    public boolean sendOtpCode(String phoneNumber, String code) {
        if (isDevelopmentMode()) {
            log.info("📱 OTP ENVIADO A {}: {}", phoneNumber, code);
            return true;
        }

        if (!isProductionMode() || !hasValidTwilioConfig()) {
            log.warn("⚠️ SMS no enviado - Twilio no configurado. Código: {}", code);
            return false;
        }

        try {
            if (!twilioInitialized) {
                initializeTwilio();
            }

            Message message = Message.creator(
                    new PhoneNumber(phoneNumber),  // To number
                    new PhoneNumber(fromPhone),    // From number
                    buildOtpMessage(code)
                )
                .create();

            log.info("✅ SMS enviado a {} - SID: {}", phoneNumber, message.getSid());
            return true;
        } catch (Exception e) {
            log.error("❌ Error al enviar SMS a {}: {}", phoneNumber, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send notification SMS (verification, alerts, etc)
     */
    public boolean sendNotification(String phoneNumber, String message) {
        if (isDevelopmentMode()) {
            log.info("📨 NOTIFICACIÓN A {}: {}", phoneNumber, message);
            return true;
        }

        if (!isProductionMode() || !hasValidTwilioConfig()) {
            log.warn("⚠️ Notificación no enviada - Twilio no configurado");
            return false;
        }

        try {
            if (!twilioInitialized) {
                initializeTwilio();
            }

            Message.creator(
                    new PhoneNumber(phoneNumber),
                    new PhoneNumber(fromPhone),
                    message
                )
                .create();

            log.info("✅ Notificación enviada a {}", phoneNumber);
            return true;
        } catch (Exception e) {
            log.error("❌ Error al enviar notificación a {}: {}", phoneNumber, e.getMessage(), e);
            return false;
        }
    }

    private void initializeTwilio() {
        if (hasValidTwilioConfig()) {
            Twilio.init(accountSid, authToken);
            twilioInitialized = true;
            log.info("✅ Twilio inicializado correctamente");
        }
    }

    private boolean hasValidTwilioConfig() {
        return accountSid != null && !accountSid.isBlank()
            && authToken != null && !authToken.isBlank()
            && fromPhone != null && !fromPhone.isBlank();
    }

    private boolean isDevelopmentMode() {
        return environment.acceptsProfiles(Profiles.of("dev"));
    }

    private boolean isProductionMode() {
        return environment.acceptsProfiles(Profiles.of("prod"))
            || !isDevelopmentMode();
    }

    private String buildOtpMessage(String code) {
        return String.format(
            "🔐 Tu código de verificación de Reseña App es: %s\n" +
            "⏱️ Válido por 5 minutos\n" +
            "🔒 Nunca compartas este código",
            code
        );
    }
}
