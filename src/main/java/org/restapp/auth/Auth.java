package org.restapp.auth;

/**
 * A simple authentication object for obtaining authentication tokens
 * @author aniket
 */
public class Auth {
	public String username;
	public String password;
	public String token;
	
	public Auth() {}
	
	public Auth(String username, String password, String token) {
		this.username = username;
		this.password = password;
		this.token = token;
	}
}
