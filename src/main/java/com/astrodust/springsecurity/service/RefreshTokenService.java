package com.astrodust.springsecurity.service;

import com.astrodust.springsecurity.entity.RefreshToken;
import com.astrodust.springsecurity.exception.RefreshTokenException;
import com.astrodust.springsecurity.repository.RefreshTokenRepository;
import com.astrodust.springsecurity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.jwtRefreshExpirationMs}")
    private Long refreshTokenDuration;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken generate(String username) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(userRepository.findByUsername(username));
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDuration));
        refreshToken.setToken(UUID.randomUUID().toString());
        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RefreshTokenException(token.getToken(), "Refresh token was expired. Please make a new signin request");
        }
        return token;
    }
}
