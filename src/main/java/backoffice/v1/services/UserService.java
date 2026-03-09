package backoffice.v1.services;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import backoffice.common.mappers.UserMapper;
import backoffice.common.utils.PasswordUtils;
import backoffice.v1.dtos.user.UserCreateDTO;
import backoffice.v1.entities.User;
import backoffice.v1.repositories.UserRepository;

@ApplicationScoped
public class UserService {
  @Inject
  private UserRepository userRepository;

  public Optional<User> findById(Long userId) {
    return userRepository.findByIdOptional(userId);
  }

  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  public User create(UserCreateDTO dto) {
    dto.setPassword(PasswordUtils.hashPass(dto.getPassword()));

    User user = UserMapper.fromDTO(dto);

    userRepository.persist(user);

    return user;
  }
}
