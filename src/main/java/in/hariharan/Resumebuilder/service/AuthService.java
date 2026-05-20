package in.hariharan.Resumebuilder.service;

import in.hariharan.Resumebuilder.dto.AuthResponse;
import in.hariharan.Resumebuilder.dto.RegisterRequest;
import in.hariharan.Resumebuilder.document.User;
import in.hariharan.Resumebuilder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import in.hariharan.Resumebuilder.exception.ResourceExistsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import in.hariharan.Resumebuilder.service.EmailService;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.base.url:http://localhost:8080}")
    private String appBaseUrl;

    public AuthResponse register(RegisterRequest request) {
        // simple existence check
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceExistsException("User already exists with this email");
        }

        User newUser = toDocument(request);
        newUser = userRepository.save(newUser);

        sendVerificationEmail(newUser);

        return toResponse(newUser);
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
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .profileImageUrl(request.getProfileImageUrl())
                .subscriptionPlan("Basic")
                .emailVerified(false)
                .verificationToken(UUID.randomUUID().toString())
                .verificationExpires(LocalDateTime.now().plusHours(24))
                .build();
    }

    private void sendVerificationEmail(User user) {
        try {
            String link = appBaseUrl + "/api/auth/verify-email?token=" + user.getVerificationToken();
            String html = "<div style='font-family:sans-serif'>"
                    + "<h2>Verify your email</h2>"
                    + "<p>Hi " + user.getName() + ", please confirm your email to activate your account.</p>"
                    + "<p><a href='" + link + "' style='display: inline-block; padding: 10px 16px; background:#6366f1; color:#fff; text-decoration:none; border-radius:6px;'>Verify Email</a></p>"
                    + "<p>Or copy this link: " + link + "</p>"
                    + "<p>This link expires in 24 hours.</p>"
                    + "</div>";
            emailService.sendHtmlEmail(user.getEmail(), "Verify your email", html);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to send verification email: " + ex.getMessage(), ex);
        }
    }

}
