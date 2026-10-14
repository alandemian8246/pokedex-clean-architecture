package com.pokedex.infrastructure.config;

import com.pokedex.application.auth.RegisterUserUseCase;
import com.pokedex.application.port.UserRepository;
import com.pokedex.domain.model.Credentials;
import com.pokedex.domain.model.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Creates the demonstration accounts documented in the README. Passwords are hashed through the regular
 * registration use case, so no credential hashes are hard-coded in migrations.
 */
@Component
@ConditionalOnProperty(name = "app.seed.demo-users", havingValue = "true")
public class DemoUserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoUserSeeder.class);

    private final RegisterUserUseCase registerUser;
    private final UserRepository users;

    public DemoUserSeeder(RegisterUserUseCase registerUser, UserRepository users) {
        this.registerUser = registerUser;
        this.users = users;
    }

    @Override
    public void run(ApplicationArguments args) {
        seed(new Credentials("admin", "Admin1234"), Role.ADMIN);
        seed(new Credentials("demo", "Demo1234"), Role.USER);
    }

    private void seed(Credentials credentials, Role role) {
        if (!users.existsByUsername(credentials.username())) {
            registerUser.register(credentials, role);
            log.info("Seeded demo account '{}' with role {}", credentials.username(), role);
        }
    }
}
