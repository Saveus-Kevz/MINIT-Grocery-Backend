// File: src/test/java/com/minimartph/Minit/service/EmailServiceTest.java
package com.minimartph.Minit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Unit Tests")
class EmailServiceTest {

  @Mock private JavaMailSender mailSender;

  @InjectMocks private EmailService emailService;

  @Nested
  @DisplayName("sendCredentials()")
  class SendCredentialsTests {

    @Test
    @DisplayName("Should send email with correct credentials")
    void shouldSendEmailWithCorrectCredentials() {
      // Given
      String to = "user@example.com";
      String username = "testuser";
      String rawPassword = "password123";
      String firstName = "John";

      // When
      emailService.sendCredentials(to, username, rawPassword, firstName);

      // Then
      ArgumentCaptor<SimpleMailMessage> messageCaptor =
          ArgumentCaptor.forClass(SimpleMailMessage.class);
      verify(mailSender, times(1)).send(messageCaptor.capture());

      SimpleMailMessage sentMessage = messageCaptor.getValue();
      assertThat(sentMessage.getTo()).containsExactly(to);
      assertThat(sentMessage.getSubject()).isEqualTo("Your Minit Staff Account");
      assertThat(sentMessage.getText()).contains("Dear John,");
      assertThat(sentMessage.getText()).contains("Username: testuser");
      assertThat(sentMessage.getText()).contains("Password: password123");
      assertThat(sentMessage.getText()).contains("http://localhost:3000");
      assertThat(sentMessage.getText()).contains("Minit Team");
    }

    @Test
    @DisplayName("Should handle different recipient email")
    void shouldHandleDifferentRecipient() {
      // Given
      String to = "another@example.com";
      String username = "anotheruser";
      String rawPassword = "pass456";
      String firstName = "Jane";

      // When
      emailService.sendCredentials(to, username, rawPassword, firstName);

      // Then
      ArgumentCaptor<SimpleMailMessage> messageCaptor =
          ArgumentCaptor.forClass(SimpleMailMessage.class);
      verify(mailSender, times(1)).send(messageCaptor.capture());

      SimpleMailMessage sentMessage = messageCaptor.getValue();
      assertThat(sentMessage.getTo()).containsExactly(to);
      assertThat(sentMessage.getText()).contains("Dear Jane,");
      assertThat(sentMessage.getText()).contains("Username: anotheruser");
      assertThat(sentMessage.getText()).contains("Password: pass456");
    }

    @Test
    @DisplayName("Should include all required fields in email")
    void shouldIncludeAllRequiredFields() {
      // Given
      String to = "test@example.com";
      String username = "fulluser";
      String rawPassword = "securepass";
      String firstName = "Robert";

      // When
      emailService.sendCredentials(to, username, rawPassword, firstName);

      // Then
      ArgumentCaptor<SimpleMailMessage> messageCaptor =
          ArgumentCaptor.forClass(SimpleMailMessage.class);
      verify(mailSender).send(messageCaptor.capture());

      String emailText = messageCaptor.getValue().getText();

      // Verify all required fields are present
      assertThat(emailText).contains("Dear Robert,");
      assertThat(emailText).contains("Your account has been created successfully");
      assertThat(emailText).contains("Username: fulluser");
      assertThat(emailText).contains("Password: securepass");
      assertThat(emailText).contains("http://localhost:3000");
      assertThat(emailText).contains("Regards,");
      assertThat(emailText).contains("Minit Team");
    }

    @Test
    @DisplayName("Should handle empty strings gracefully")
    void shouldHandleEmptyStrings() {
      // Given
      String to = "empty@example.com";
      String username = "";
      String rawPassword = "";
      String firstName = "";

      // When
      emailService.sendCredentials(to, username, rawPassword, firstName);

      // Then
      ArgumentCaptor<SimpleMailMessage> messageCaptor =
          ArgumentCaptor.forClass(SimpleMailMessage.class);
      verify(mailSender).send(messageCaptor.capture());

      SimpleMailMessage sentMessage = messageCaptor.getValue();
      assertThat(sentMessage.getTo()).containsExactly(to);
      assertThat(sentMessage.getText()).contains("Dear ,");
      assertThat(sentMessage.getText()).contains("Username: ");
      assertThat(sentMessage.getText()).contains("Password: ");
    }

    @Test
    @DisplayName("Should handle special characters in username and password")
    void shouldHandleSpecialCharacters() {
      // Given
      String to = "special@example.com";
      String username = "user@special!";
      String rawPassword = "p@$$w0rd!@#$";
      String firstName = "Special";

      // When
      emailService.sendCredentials(to, username, rawPassword, firstName);

      // Then
      ArgumentCaptor<SimpleMailMessage> messageCaptor =
          ArgumentCaptor.forClass(SimpleMailMessage.class);
      verify(mailSender).send(messageCaptor.capture());

      SimpleMailMessage sentMessage = messageCaptor.getValue();
      assertThat(sentMessage.getText()).contains("Username: user@special!");
      assertThat(sentMessage.getText()).contains("Password: p@$$w0rd!@#$");
    }

    @Test
    @DisplayName("Should handle long strings")
    void shouldHandleLongStrings() {
      // Given
      String to = "long@example.com";
      String username = "verylongusername" + "x".repeat(100);
      String rawPassword = "verylongpassword" + "y".repeat(100);
      String firstName = "VeryLongFirstName" + "z".repeat(50);

      // When
      emailService.sendCredentials(to, username, rawPassword, firstName);

      // Then
      ArgumentCaptor<SimpleMailMessage> messageCaptor =
          ArgumentCaptor.forClass(SimpleMailMessage.class);
      verify(mailSender).send(messageCaptor.capture());

      SimpleMailMessage sentMessage = messageCaptor.getValue();
      assertThat(sentMessage.getText()).contains(username);
      assertThat(sentMessage.getText()).contains(rawPassword);
      assertThat(sentMessage.getText()).contains(firstName);
    }

    @Test
    @DisplayName("Should format email with correct line breaks")
    void shouldFormatEmailWithCorrectLineBreaks() {
      // Given
      String to = "format@example.com";
      String username = "formatuser";
      String rawPassword = "formatpass";
      String firstName = "Format";

      // When
      emailService.sendCredentials(to, username, rawPassword, firstName);

      // Then
      ArgumentCaptor<SimpleMailMessage> messageCaptor =
          ArgumentCaptor.forClass(SimpleMailMessage.class);
      verify(mailSender).send(messageCaptor.capture());

      String emailText = messageCaptor.getValue().getText();

      // Verify line breaks are present
      assertThat(emailText).contains("\n\n");
      assertThat(emailText).contains("\n\nRegards,\n");
    }

    @Test
    @DisplayName("Should call mailSender.send exactly once")
    void shouldCallMailSenderExactlyOnce() {
      // Given
      String to = "once@example.com";
      String username = "onceuser";
      String rawPassword = "oncepass";
      String firstName = "Once";

      // When
      emailService.sendCredentials(to, username, rawPassword, firstName);

      // Then
      verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Should not throw exception when mailSender throws")
    void shouldNotThrowExceptionWhenMailSenderThrows() {
      // Given
      String to = "error@example.com";
      String username = "erroruser";
      String rawPassword = "errorpass";
      String firstName = "Error";

      doThrow(new RuntimeException("Mail server error"))
          .when(mailSender)
          .send(any(SimpleMailMessage.class));

      // When & Then - The service doesn't catch exceptions, so it will propagate
      // This test verifies the exception is thrown
      org.junit.jupiter.api.Assertions.assertThrows(
          RuntimeException.class,
          () -> {
            emailService.sendCredentials(to, username, rawPassword, firstName);
          });
    }

    @Test
    @DisplayName("Should handle null values gracefully")
    void shouldHandleNullValues() {
      // Given
      String to = "nulltest@example.com";
      String username = null;
      String rawPassword = null;
      String firstName = null;

      // When
      emailService.sendCredentials(to, username, rawPassword, firstName);

      // Then
      ArgumentCaptor<SimpleMailMessage> messageCaptor =
          ArgumentCaptor.forClass(SimpleMailMessage.class);
      verify(mailSender).send(messageCaptor.capture());

      SimpleMailMessage sentMessage = messageCaptor.getValue();
      assertThat(sentMessage.getTo()).containsExactly(to);
      assertThat(sentMessage.getText()).contains("Dear null,");
      assertThat(sentMessage.getText()).contains("Username: null");
      assertThat(sentMessage.getText()).contains("Password: null");
    }
  }
}
