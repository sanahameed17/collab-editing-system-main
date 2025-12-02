package com.collab.user_service;

import com.collab.user_service.model.User;
import com.collab.user_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserServiceTests {

    @Autowired
    private UserRepository repo;

    @Test
    void testRegisterUser() {
        User u = new User();
        u.setUsername("Test");
        u.setEmail("test@example.com");
        u.setPassword("pass123");
        User saved = repo.save(u);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("Test");
    }
}
