package com.example.demo.service;

public interface TokenService {
    
    /**
     * Store a token with associated username
     * 
     * @param token The authentication token
     * @param username The username associated with the token
     */
    void storeToken(String token, String username);
    
    /**
     * Retrieve username associated with a token
     * 
     * @param token The authentication token
     * @return The username if token exists, null otherwise
     */
    String getUsernameFromToken(String token);
    
    /**
     * Check if a token is valid (exists and not expired)
     * 
     * @param token The authentication token
     * @return true if token is valid, false otherwise
     */
    boolean isValidToken(String token);
    
    /**
     * Remove a token from storage
     * 
     * @param token The authentication token to remove
     */
    void removeToken(String token);
    
    /**
     * Generate a simple token for a username
     * 
     * @param username The username to generate token for
     * @return Generated token
     */
    String generateToken(String username);
}
