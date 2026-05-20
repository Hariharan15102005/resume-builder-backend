package in.hariharan.Resumebuilder.service;

import in.hariharan.Resumebuilder.dto.AuthResponse;
import in.hariharan.Resumebuilder.dto.RegisterRequest;
import in.hariharan.Resumebuilder.document.User;
import in.hariharan.Resumebuilder.repository.UserRepository;
import in.hariharan.Resumebuilder.exception.ResourceExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public AuthResponse register(RegisterRequest request) {
        User user = toDocument(request);
        User savedUser = registerUser(user);
        return toResponse(savedUser);
    }

    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResourceExistsException("User already exists with this email");
        }

        user.setId(UUID.randomUUID().toString());
        user.setEmailVerified(false);
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationExpires(LocalDateTime.now().plusHours(24));
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        sendVerificationEmail(savedUser);
        return savedUser;
    }

    private AuthResponse toResponse(User newUser) {
        return AuthResponse.builder()
                .id(newUser.getId())
                .name(newUser.getName())
                .email(newUser.getEmail())
                .profileImageUrl(newUser.getProfileImageUrl())
                .emailVerified(newUser.isEmailVerified())
                .subscriptionPlan(newUser.getSubscriptionPlan())
                .token(newUser.getVerificationToken())
                .createdAt(newUser.getCreatedAt())
                .updatedAt(newUser.getUpdatedAt())
                .build();
    }

    private User toDocument(RegisterRequest request) {
        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .profileImageUrl(request.getProfileImageUrl())
                .subscriptionPlan("Basic")
                .build();
    }

    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

        if (user.getVerificationExpires() != null && user.getVerificationExpires().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationExpires(null);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private void sendVerificationEmail(User user) {
        try {
            String verificationLink = "http://localhost:8081/api/auth/verify?token=" + user.getVerificationToken();
            String htmlContent = "<div style='font-family:sans-serif'>"
                    + "<h2>Verify your Email</h2>"
                    + "<p>Please click the button below to verify your email address.</p>"
                    + "<p><a href='" + verificationLink + "' style='display:inline-block;padding:10px 16px;background:#6366f1;color:#fff;text-decoration:none;border-radius:6px;'>Verify Email</a></p>"
                    + "<p>Or copy this link: " + verificationLink + "</p>"
                    + "</div>";

            emailService.sendHtmlEmail(user.getEmail(), "Verify your Email", htmlContent);
        } catch (Exception ex) {
            log.error("Failed to send verification email to {}", user.getEmail(), ex);
            throw new RuntimeException("Failed to send verification email: " + ex.getMessage(), ex);
        }
    }

}
