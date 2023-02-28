package com.jocotech.vendingmachine.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  record CreateUserRequest(String username, String password, Role role) {
  }

  record UpdateUserRequest(@Nullable String username, @Nullable Role role) {}

  record ChangePasswordRequest(String existingPassword, String newPassword) {}

  @GetMapping("/{id}")
  public Mono<User> getUserById(@PathVariable String id) {
    log.info("Fetching user with id: {}", id);
    return userService.findById(id);
  }

  @PostMapping
  public Mono<User> createUser(@RequestBody CreateUserRequest user) {
    log.info("Creating user with username: {}", user.username());
    return userService.createUser(User.builder()
        .username(user.username())
        .password(user.password())
        .role(user.role())
        .build());
  }

  @PutMapping("/{id}")
  public Mono<User> updateUser(@PathVariable String id, @RequestBody UpdateUserRequest request) {
    log.info("Updating user with id: {}", id);
    return userService.updateUser(id, request);
  }

  @PutMapping("/{id}/change-password")
  public Mono<User> changePassword(@PathVariable String id, @RequestBody ChangePasswordRequest request) {
    log.info("Changing password for user with id: {}", id);
    return userService.changePassword(id, request);
  }

  @DeleteMapping("/{id}")
  public Mono<Void> deleteUser(@PathVariable String id) {
    log.info("Deleting user with id: {}", id);
    return userService.deleteById(id);
  }
}
