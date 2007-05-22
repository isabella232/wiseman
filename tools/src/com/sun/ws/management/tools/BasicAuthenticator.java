package com.sun.ws.management.tools;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class BasicAuthenticator extends Authenticator {
	protected PasswordAuthentication getPasswordAuthentication() {
		final String user = System.getProperty("wsman.user", "");
		final String password = System.getProperty("wsman.password", "");
		return new PasswordAuthentication(user, password.toCharArray());
	}
}
