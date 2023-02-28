package com.jocotech.vendingmachine.user;


import com.jocotech.vendingmachine.common.security.InvalidUserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public Mono<User> createUser(User user) {
    log.trace("Creating new user user: {}", user.getUsername());
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    return userRepository.save(user)
        .doOnSuccess(savedUser -> log.info("User {} created successfully", savedUser.getUsername()));
  }

  public Mono<User> findById(String id) {
    log.trace("Finding user by id: {}", id);
    return userRepository.findById(UUID.fromString(id));
  }

  public Mono<Void> deleteById(String id) {
    log.trace("Deleting user by id: {}", id);
    return userRepository.deleteById(UUID.fromString(id))
        .doOnSuccess(v -> log.info("User with id {} deleted successfully", id))
        .onErrorMap(ex -> new RuntimeException("Error occurred while deleting user by id"));
  }

  public Mono<User> findByUsername(String username) {
    log.trace("Finding user by username: {}", username);
    return userRepository.findByUsername(username)
        .onErrorMap(ex -> new RuntimeException("Error occurred while finding user by username"));
  }

  public Mono<User> updateUser(String id, UserController.UpdateUserRequest updateUserRequest) {
    log.trace("Updating user: {} with {}", id, updateUserRequest);
    return userRepository.findById(UUID.fromString(id))
        .flatMap(existingUser -> {
          if (updateUserRequest.username() != null) {
            existingUser.setUsername(updateUserRequest.username());
          }
          if (updateUserRequest.role() != null) {
            existingUser.setRole(updateUserRequest.role());
          }
          return userRepository.save(existingUser);
        });
  }

  public Mono<User> update(User user) {
    log.trace("Updating user: {}", user);
    return userRepository.save(user);
  }

  public Mono<User> changePassword(String id, UserController.ChangePasswordRequest request) {
    log.trace("Updating user: {} password", id);
    return userRepository.findById(UUID.fromString(id))
        .filter(user -> passwordEncoder.matches(request.existingPassword(), user.getPassword()))
        .switchIfEmpty(Mono.error(InvalidUserException::new))
        .flatMap(existingUser -> {
          existingUser.setPassword(passwordEncoder.encode(request.newPassword()));
          return userRepository.save(existingUser);
        })
        .doOnSuccess(savedUser -> log.info("Password changed for user: {}", savedUser.getUsername()));
  }
}
