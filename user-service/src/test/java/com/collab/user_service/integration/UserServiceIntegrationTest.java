package com.collab.user_service.integration;

import com.collab.user_service.model.User;
import com.collab.user_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserServiceIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
    }

    @Test
    void testUserRegistration() {
        User saved = userRepository.save(testUser);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("testuser");
    }

    @Test
    void testFindUserByEmail() {
        entityManager.persistAndFlush(testUser);
        
        Optional<User> found = userRepository.findByEmail("test@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testUserUpdate() {
        User saved = entityManager.persistAndFlush(testUser);
        saved.setUsername("updateduser");
        User updated = userRepository.save(saved);
        
        assertThat(updated.getUsername()).isEqualTo("updateduser");
    }

    @Test
    void testUserDeletion() {
        User saved = entityManager.persistAndFlush(testUser);
        Long id = saved.getId();
        
        userRepository.deleteById(id);
        
        Optional<User> found = userRepository.findById(id);
        assertThat(found).isEmpty();
    }
}

