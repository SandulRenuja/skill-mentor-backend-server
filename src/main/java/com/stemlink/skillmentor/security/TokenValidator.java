package com.stemlink.skillmentor.security;
import java.util.List;

public interface TokenValidator {

    boolean validateToken(String token);
    String extractUserId(String token);
    List<String> extractRoles(String token);
}
