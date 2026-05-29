package com.trung.notificationservice.service;

import com.trung.notificationservice.event.OrderCreateEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;

    @KafkaListener(topics = "order-create-topic", groupId = "notification-service")
    public void handleNotification(OrderCreateEvent event){
//        String to = event.getUserEmail();
//        Long orderId = event.getOrderId();
//        String productName = event.getProductName();
//        sendOrderConfirmationEmail(to, orderId, productName);

        String to = event.getUserEmail();
        String subject = "Hi " + event.getUserEmail() + ", here is your reminder!";
        String content = "Wake up early tomorrow and clean the house.";

        sendEmail(to, subject, content);
    }

    public void sendOrderConfirmationEmail(String to, Long orderId, String productName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("trung8d2005@gmail.com");
            helper.setTo(to);
            helper.setSubject("🎉 Xác nhận đặt hàng thành công - Mã đơn: " + orderId);

            String htmlContent = """
                <div style="font-family: Helvetica, Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;">
                    <h2 style="color: #2e6c80; text-align: center;">Cảm ơn bạn đã đặt hàng!</h2>
                    <p style="font-size: 16px; color: #333;">Chào bạn,</p>
                    <p style="font-size: 16px; color: #333;">Hệ thống đã ghi nhận đơn hàng của bạn. Dưới đây là thông tin chi tiết:</p>
                    
                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 8px; margin: 20px 0;">
                        <ul style="list-style-type: none; padding: 0; margin: 0;">
                            <li style="margin-bottom: 10px;"><strong>Mã đơn hàng:</strong> <span style="color: #007bff;">#%s</span></li>
                            <li><strong>Sản phẩm:</strong> %s</li>
                        </ul>
                    </div>
                    
                    <p style="font-size: 14px; color: #666; text-align: center;">Chúng tôi sẽ sớm giao hàng đến bạn. Chúc bạn một ngày vui vẻ!</p>
                </div>
                """.formatted(orderId, productName);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Đã gửi email xác nhận thành công tới: {}", to);

        } catch (MessagingException e) {
            log.error("Lỗi khi gửi email tới {}: {}", to, e.getMessage());
        }
    }

    public void sendEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }
}
