package com.pokedex.infrastructure.persistence;

import com.pokedex.application.port.UserRepository;
import com.pokedex.domain.model.User;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository jpa;

    public JpaUserRepository(SpringDataUserRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpa.findByUsername(username).map(JpaUserRepository::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpa.existsByUsername(username);
    }

    @Override
    @Transactional
    public User save(User user) {
        UserEntity entity = new UserEntity(user.id(), user.username(), user.passwordHash(), user.role(),
                user.createdAt());
        return toDomain(jpa.saveAndFlush(entity));
    }

    private static User toDomain(UserEntity entity) {
        return new User(entity.getId(), entity.getUsername(), entity.getPasswordHash(), entity.getRole(),
                entity.getCreatedAt());
    }
}
