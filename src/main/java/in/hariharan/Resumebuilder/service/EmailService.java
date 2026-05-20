package in.hariharan.Resumebuilder.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

	@Autowired
	private JavaMailSender mailSender;

	@Value("${spring.mail.properties.mail.smtp.from}")
	private String fromEmail;

	@Value("${app.base.url:http://localhost:8080}")
	private String appBaseUrl;

	public void sendVerificationEmail(String toEmail, String token) {
		try {
			String verificationLink = appBaseUrl + "/api/auth/verify-email?token=" + token;
			String subject = "Verify your Email";
			String htmlContent = "<div style='font-family:sans-serif'>"
					+ "<h2>Verify your Email</h2>"
					+ "<p>Please click the button below to verify your email address.</p>"
					+ "<p><a href='" + verificationLink + "' style='display:inline-block;padding:10px 16px;background:#6366f1;color:#fff;text-decoration:none;border-radius:6px;'>Verify Email</a></p>"
					+ "<p>Or copy this link: " + verificationLink + "</p>"
					+ "</div>";

			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(fromEmail);
			helper.setTo(toEmail);
			helper.setSubject(subject);
			helper.setText(htmlContent, true);
			mailSender.send(message);
			log.info("Verification email sent to {}", toEmail);
		} catch (Exception ex) {
			log.error("Failed to send verification email to {}", toEmail, ex);
			throw new RuntimeException("Failed to send verification email: " + ex.getMessage(), ex);
		}
	}
}
