package com.example.springbootproject.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "username", "sender@qq.com");
        ReflectionTestUtils.setField(emailService, "from", "");
    }

    @Test
    void sendsCodeToRecipient() {
        emailService.sendCode("user@qq.com", "123456");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        SimpleMailMessage message = captor.getValue();
        assertEquals("sender@qq.com", message.getFrom());
        assertEquals("user@qq.com", message.getTo()[0]);
        assertTrue(message.getText().contains("123456"));
    }

    @Test
    void rejectsWhenMailIsNotConfigured() {
        ReflectionTestUtils.setField(emailService, "username", "");

        assertThrows(IllegalStateException.class,
                () -> emailService.sendCode("user@qq.com", "123456"));
        verifyNoInteractions(mailSender);
    }
}
