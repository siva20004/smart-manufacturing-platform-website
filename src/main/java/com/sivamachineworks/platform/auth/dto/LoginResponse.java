package com.sivamachineworks.platform.auth.dto;

import java.util.List;

public class LoginResponse {
    
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private String username;
    private String email;
    private List<String> roles;

    public LoginResponse() {
        this.tokenType = "Bearer";
    }

    public LoginResponse(String accessToken, long expiresIn, String username, String email, List<String> roles) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}
