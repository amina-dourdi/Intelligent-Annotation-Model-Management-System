package com.ensah.Core.services;

public interface IEmailService {
    void sendReminderEmail(String to, String subject, String text);
}
