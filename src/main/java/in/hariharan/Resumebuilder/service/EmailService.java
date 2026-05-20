package in.hariharan.Resumebuilder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

	@Value("${spring.mail.properties.mail.smtp.from}")
	private String fromEmail;

	private final JavaMailSender mailSender;

	public void sendHtmlEmail(String to, String subject, String htmlContent) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");
			helper.setFrom(fromEmail);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(htmlContent, true);
			mailSender.send(message);
			log.info("Sent email to {}", to);
		} catch (Exception ex) {
			log.error("Failed to send email to {}: {}", to, ex.getMessage(), ex);
		}
	}

}
