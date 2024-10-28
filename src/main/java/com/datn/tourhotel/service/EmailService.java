package com.datn.tourhotel.service;

public interface EmailService {
	public void sendBookingConfirmation(String to, String subject, String text); 
}
