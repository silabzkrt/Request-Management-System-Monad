package org.vaadin.example.domain.user.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetMail(String toEmail, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@monad.com.tr");
            message.setTo(toEmail);
            message.setSubject("Şifre Sıfırlama Talebi - Monad Request Management");
            message.setText("Merhaba,\n\n" +
                    "Şifrenizi sıfırlamak için aşağıdaki bağlantıya tıklayın. Bu bağlantı 1 saat boyunca geçerli olacaktır:\n\n" +
                    resetLink + "\n\n" +
                    "Eğer böyle bir talepte bulunmadıysanız bu mesajı görmezden gelebilirsiniz.\n\n" +
                    "İyi çalışmalar.");
            mailSender.send(message);
            System.out.println("E-posta başarıyla gönderildi: " + toEmail);
        } catch (Exception e) {
            System.err.println("E-posta gönderimi başarısız oldu (SMTP ayarları eksik olabilir). Ancak sıfırlama linkiniz aşağıdadır:");
            System.out.println("=================================================");
            System.out.println("ŞİFRE SIFIRLAMA LİNKİ: " + resetLink);
            System.out.println("=================================================");
        }
    }
}
