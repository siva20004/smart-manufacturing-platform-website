package com.sivamachineworks.platform;

import com.sivamachineworks.platform.identity.domain.Role;
import com.sivamachineworks.platform.identity.domain.User;
import com.sivamachineworks.platform.identity.repository.RoleRepository;
import com.sivamachineworks.platform.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FlywayMigrationTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void testFlywayMigrationsApplied() {
        Optional<User> admin = userRepository.findByUsername("admin");
        assertThat(admin).isPresent();
        assertThat(admin.get().getEmail()).isEqualTo("admin@sivamachineworks.com");

        Optional<Role> adminRole = roleRepository.findByName("ADMIN");
        assertThat(adminRole).isPresent();
    }
}
