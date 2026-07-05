package com.camss.honeymoney.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.camss.honeymoney.model.User;
import com.camss.honeymoney.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {
    @Mock
    private UserRepository userRepository;

    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp(){
        userDetailsService = new UserDetailsServiceImpl(userRepository);
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists(){
        User user = new User();
        user.setEmail("usuario@prueba.com");
        user.setPassword("hashedPassword123");

        when(userRepository.findByEmail("usuario@prueba.com")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("usuario@prueba.com");

        assertEquals("usuario@prueba.com", result.getUsername());
        assertEquals("hashedPassword123", result.getPassword());
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound(){
        when(userRepository.findByEmail("noexiste@prueba.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> {userDetailsService.loadUserByUsername("noexiste@prueba.com");});
    }
}
