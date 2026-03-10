package backoffice.v1.services;

import backoffice.common.database.Pageable;
import backoffice.common.exceptions.MessageErrorEnum;
import backoffice.common.exceptions.customs.ConflictException;
import backoffice.common.exceptions.customs.BusinessException;
import backoffice.common.mappers.SponsorMapper;
import backoffice.common.utils.PasswordUtils;
import backoffice.v1.dtos.common.PageDTO;
import backoffice.v1.dtos.sponsor.SponsorCreateDTO;
import backoffice.v1.dtos.sponsor.SponsorDTO;
import backoffice.v1.dtos.user.UserCreateDTO;
import backoffice.v1.entities.User;
import backoffice.v1.entities.enums.SponsorEntityTypeEnum;
import backoffice.v1.entities.enums.SponsorTierEnum;
import backoffice.v1.entities.enums.UserTypeEnum;
import backoffice.v1.repositories.SponsorRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SponsorService {
  @Inject
  private UserService userService;

  @Inject
  private SponsorRepository sponsorRepository;

  @Transactional
  public SponsorDTO createSponsor(SponsorCreateDTO dto) {
    UserCreateDTO userData = dto.getUser();

    userService.validateUniqueFields(userData.getEmail(), userData.getDocument(), userData.getCode());

    if (dto.getWhatsapp() != null && !dto.getWhatsapp().isBlank()) {
      validateUniqueWhatsapp(dto.getWhatsapp());
    }

    validatePersonaByEntityType(dto);

    userData.setPassword(PasswordUtils.hashPass("temp@1234"));
    userData.setType(UserTypeEnum.SPONSOR.name());

    User user = userService.create(userData);

    var sponsor = SponsorMapper.fromDTO(dto, user);
    sponsorRepository.persistAndFlush(sponsor);

    return SponsorMapper.fromEntityToSponsorDTO(sponsor);
  }

  public Pageable<SponsorDTO> listSponsors(SponsorTierEnum tier, PageDTO pageDTO) {
    var pageable = sponsorRepository.findActiveSponsors(tier, pageDTO);

    return SponsorMapper.fromEntityToPageableDTO(pageable);
  }

  private void validateUniqueWhatsapp(String whatsapp) {
    sponsorRepository.findByWhatsapp(whatsapp).ifPresent(s -> {
      throw new ConflictException(MessageErrorEnum.SPONSOR_ALREADY_EXISTS.getMessage());
    });
  }

  private void validatePersonaByEntityType(SponsorCreateDTO dto) {
    SponsorEntityTypeEnum entityType = SponsorEntityTypeEnum.valueOf(dto.getEntityType());
    String persona = dto.getPersona();

    if (SponsorEntityTypeEnum.PERSON.equals(entityType)
        && (persona == null || persona.isBlank())) {
      throw new BusinessException(MessageErrorEnum.SPONSOR_PERSONA_REQUIRED_FOR_PERSON.getMessage(), 400);
    }
  }
}
