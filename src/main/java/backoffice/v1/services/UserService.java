package backoffice.v1.services;

import java.util.Optional;

import backoffice.common.database.Pageable;
import backoffice.common.exceptions.customs.ConflictException;
import backoffice.common.mappers.UserMapper;
import backoffice.v1.dtos.common.PageDTO;
import backoffice.v1.dtos.user.UserCreateDTO;
import backoffice.v1.entities.User;
import backoffice.v1.entities.enums.SponsorTierEnum;
import backoffice.v1.entities.enums.UserTypeEnum;
import backoffice.v1.repositories.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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

  public void validateUniqueFields(String email, String document, String code) {
    if (userRepository.existsByEmail(email)) {
      throw new ConflictException("Já existe um usuário com o e-mail informado.");
    }

    if (userRepository.existsByDocument(document)) {
      throw new ConflictException("Já existe um usuário com o documento informado.");
    }

    if (userRepository.existsByCode(code)) {
      throw new ConflictException("Já existe um usuário com o código informado.");
    }
  }

  public void validateUniqueFieldsForUpdate(Long userId, String email, String document) {
    if (email != null && userRepository.existsByEmailAndIdNot(email, userId)) {
      throw new ConflictException("Já existe um usuário com o e-mail informado.");
    }

    if (document != null && userRepository.existsByDocumentAndIdNot(document, userId)) {
      throw new ConflictException("Já existe um usuário com o documento informado.");
    }
  }

  public User create(UserCreateDTO dto) {
    User user = UserMapper.fromDTO(dto);

    userRepository.persistAndFlush(user);

    return user;
  }

  public Pageable<User> listUsers(UserTypeEnum type, SponsorTierEnum tier, Boolean isActive, PageDTO pageDTO) {
    return userRepository.findAllPaginated(type, tier, isActive, pageDTO);
  }

  public void persistAndFlush(User user) {
    userRepository.persistAndFlush(user);
  }

  public void delete(User user) {
    userRepository.delete(user);
  }
}
