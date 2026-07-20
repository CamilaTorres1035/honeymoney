package com.camss.honeymoney.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.camss.honeymoney.model.RefreshToken;
import com.camss.honeymoney.model.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    
    // Útil para limpiar tokens viejos del usuario o cerrar sesión
    int deleteByUser(User user);
}