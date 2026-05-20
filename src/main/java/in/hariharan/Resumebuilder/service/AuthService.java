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
            String link = appBaseUrl + "/api/auth/verify?token=" + user.getVerificationToken();
            String html = "<p>Hello " + user.getName() + ",</p>"
                    + "<p>Please verify your email by clicking the link below:</p>"
                    + "<p><a href=\"" + link + "\">Verify Email</a></p>";
            emailService.sendHtmlEmail(user.getEmail(), "Verify your email", html);
        } catch (Exception ex) {
            // log or ignore for now; do not fail registration on email send failure
        }
    }

}
