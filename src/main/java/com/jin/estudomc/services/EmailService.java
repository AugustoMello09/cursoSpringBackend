package com.jin.estudomc.services;

import org.springframework.mail.SimpleMailMessage;

import com.jin.estudomc.domain.Pedido;

public interface EmailService {

	void sendOrderConfirmationEmail(Pedido obj);

	void sendEmail(SimpleMailMessage msg);
}
