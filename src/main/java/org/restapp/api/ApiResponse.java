package org.restapp.api;

public class ApiResponse {
	public boolean error;
	public int code;
	public String message;
	
	public ApiResponse(String message, int code, boolean isError) {
		this.error = isError;
		this.code = code;
		this.message = message;
	}
}
