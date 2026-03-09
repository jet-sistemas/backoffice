package backoffice.v1.services;

import backoffice.common.database.Pageable;
import backoffice.common.exceptions.MessageErrorEnum;
import backoffice.common.exceptions.customs.ConflictException;
import backoffice.common.mappers.SponsorMapper;
import backoffice.common.utils.PasswordUtils;
import backoffice.v1.dtos.common.PageDTO;
import backoffice.v1.dtos.sponsor.SponsorCreateDTO;
import backoffice.v1.dtos.sponsor.SponsorDTO;
import backoffice.v1.entities.Sponsor;
import backoffice.v1.entities.User;
import backoffice.v1.entities.enums.SponsorTierEnum;
import backoffice.v1.entities.enums.UserTypeEnum;
import backoffice.v1.repositories.SponsorRepository;
import backoffice.v1.repositories.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SponsorService {
  @Inject
  private UserService userService;

  @Inject
  private UserRepository userRepository;

  @Inject
  private SponsorRepository sponsorRepository;

  @Transactional
  public SponsorDTO createSponsor(SponsorCreateDTO dto) {
    SponsorCreateDTO.UserData userData = dto.getUser();

    userService.validateUniqueFields(userData.getEmail(), userData.getDocument(), userData.getCode());

    if (dto.getWhatsapp() != null && !dto.getWhatsapp().isBlank()) {
      validateUniqueWhatsapp(dto.getWhatsapp());
    }

    User user = User.builder()
        .email(userData.getEmail())
        .password(PasswordUtils.hashPass("temp@1234"))
        .name(userData.getName())
        .document(userData.getDocument())
        .code(userData.getCode())
        .type(UserTypeEnum.SPONSOR)
        .avatarUrl(userData.getAvatarUrl())
        .build();

    userRepository.persistAndFlush(user);

    Sponsor sponsor = SponsorMapper.fromDTO(dto, user);

    sponsorRepository.persistAndFlush(sponsor);

    return SponsorMapper.fromEntityToSponsorDTO(sponsor);
  }

  public Pageable<SponsorDTO> listSponsors(SponsorTierEnum tier, PageDTO pageDTO) {
    var query = tier != null
        ? sponsorRepository.find("tier = ?1 and isActive = true", tier)
        : sponsorRepository.find("isActive = true");

    var paginatedQuery = query.page(pageDTO.getPagination());
    var pageable = new Pageable<Sponsor>(paginatedQuery, pageDTO.getOneBasePage());

    var dtos = pageable.getData().stream()
        .map(SponsorMapper::fromEntityToSponsorDTO)
        .toList();

    return Pageable.<SponsorDTO>builder()
        .data(dtos)
        .totalElements(pageable.getTotalElements())
        .totalPages(pageable.getTotalPages())
        .pageSize(pageable.getPageSize())
        .currentPage(pageable.getCurrentPage())
        .build();
  }

  private void validateUniqueWhatsapp(String whatsapp) {
    sponsorRepository.findByWhatsapp(whatsapp).ifPresent(s -> {
      throw new ConflictException(MessageErrorEnum.SPONSOR_ALREADY_EXISTS.getMessage());
    });
  }
}
