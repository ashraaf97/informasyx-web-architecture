package com.example.demo.domain.repository;

import com.example.demo.domain.User;
import com.example.demo.domain.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    
    Optional<VerificationToken> findByToken(String token);
    
    List<VerificationToken> findByUserAndTokenType(User user, VerificationToken.TokenType tokenType);
    
    void deleteByExpiryDateBeforeAndUsedTrue(LocalDateTime date);
    
    void deleteByUserAndTokenType(User user, VerificationToken.TokenType tokenType);
}