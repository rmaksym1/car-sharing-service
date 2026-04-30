package com.origin.repository;

import com.origin.model.User;
import com.origin.repository.user.UserRepository;
import com.origin.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;

import static com.origin.util.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = CLEANUP_DB_PATH, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should save and then return a user by id")
    void shouldSaveUserAndReturnItById() {
        User user = TestUtil.createUser();

        User savedUser = userRepository.save(user);
        User actual = userRepository.findById(savedUser.getId()).orElseThrow(
                () -> new AssertionError("User not found!")
        );

        assertNotNull(savedUser.getId());
        assertEquals(savedUser.getEmail(), actual.getEmail());
    }

    @Test
    @DisplayName("Should return a user by email")
    @Sql(scripts = ADD_USER_DB_PATH, executionPhase =
            Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldReturnUserByEmail() {
        String email = "user@gmail.com";

        User actual = userRepository.findByEmail("user@gmail.com").orElseThrow(
                () -> new AssertionError("User not found!")
        );

        assertNotNull(actual.getId());
        assertEquals(email, actual.getEmail());
    }

    @Test
    @DisplayName("Should throw an exception when saving user with non unique email")
    @Sql(scripts = ADD_USER_DB_PATH, executionPhase =
            Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldThrowException_WhenSavingNonUniqueEmailUser() {
        User user = TestUtil.createUser();

        assertThrows(DataIntegrityViolationException.class,
                () -> userRepository.save(user)
        );
    }

    @Test
    @DisplayName("Should soft delete user by id")
    @Sql(scripts = ADD_USER_DB_PATH, executionPhase =
            Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldSoftDeleteUserById() {
        Long id = 1L;
        userRepository.deleteById(id);

        assertTrue(userRepository.findById(id).isEmpty(),
                "User must be soft deleted!"
        );
    }
}
