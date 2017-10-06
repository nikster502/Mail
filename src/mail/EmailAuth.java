package mail;

import javax.mail.PasswordAuthentication;

public class EmailAuth extends javax.mail.Authenticator {
        private String login;
	private String password;
	public EmailAuth (final String login, final String password)
	{
		this.login    = login;
		this.password = password;
	}
	public PasswordAuthentication getPasswordAuthentication()
	{
		return new PasswordAuthentication(login, password);
	}
}
