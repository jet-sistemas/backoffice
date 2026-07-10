package backoffice.v1.services;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import backoffice.common.database.Pageable;
import backoffice.common.exceptions.MessageErrorEnum;
import backoffice.common.exceptions.customs.BusinessException;
import backoffice.common.exceptions.customs.ConflictException;
import backoffice.common.exceptions.customs.ForbiddenException;
import backoffice.common.exceptions.customs.NotFoundException;
import backoffice.common.mappers.BenefitMapper;
import backoffice.common.mappers.SponsorMapper;
import backoffice.v1.dtos.benefit.BenefitDTO;
import backoffice.v1.dtos.sponsor.ListSponsorBenefitsQueryDTO;
import backoffice.v1.dtos.sponsor.ListSponsorCheckinsQueryDTO;
import backoffice.v1.dtos.sponsor.SponsorCheckinCreateDTO;
import backoffice.v1.dtos.sponsor.SponsorCheckinDTO;
import backoffice.v1.dtos.sponsor.SponsorDataCreateDTO;
import backoffice.v1.dtos.sponsor.SponsorDataUpdateDTO;
import backoffice.v1.dtos.sponsor.SponsorMemberPreviewDTO;
import backoffice.v1.entities.Benefit;
import backoffice.v1.entities.Sponsor;
import backoffice.v1.entities.User;
import backoffice.v1.entities.enums.SponsorEntityTypeEnum;
import backoffice.v1.repositories.BenefitRepository;
import backoffice.v1.repositories.SponsorRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SponsorService {
  @Inject
  private SponsorRepository sponsorRepository;

  @Inject
  private BenefitRepository benefitRepository;

  @Inject
  private SponsorCheckinService sponsorCheckinService;

  public Sponsor create(SponsorDataCreateDTO dto, User user) {
    if (dto.getWhatsapp() != null && !dto.getWhatsapp().isBlank()) {
      validateUniqueWhatsapp(dto.getWhatsapp());
    }

    validatePersonaByEntityType(dto.getEntityType(), dto.getPersona());

    var sponsor = SponsorMapper.fromDataDTO(dto, user);
    sponsorRepository.persistAndFlush(sponsor);

    return sponsor;
  }

  public Sponsor update(Sponsor sponsor, SponsorDataUpdateDTO dto) {
    if (dto.getWhatsapp() != null && !dto.getWhatsapp().isBlank()) {
      validateUniqueWhatsappForUpdate(dto.getWhatsapp(), sponsor.getId());
    }

    validatePersonaByEntityTypeForUpdate(dto, sponsor);

    SponsorMapper.applyDataUpdate(dto, sponsor);
    sponsorRepository.persistAndFlush(sponsor);

    return sponsor;
  }

  public Optional<Sponsor> findById(Long sponsorId) {
    return sponsorRepository.findByIdOptional(sponsorId);
  }

  public Optional<Sponsor> findByUserId(Long userId) {
    return sponsorRepository.findByUserId(userId);
  }

  public Map<Long, Sponsor> findByUserIds(List<Long> userIds) {
    if (userIds.isEmpty()) return Map.of();

    return sponsorRepository
        .find("user.id in ?1", userIds)
        .list()
        .stream()
        .collect(Collectors.toMap(s -> s.getUser().getId(), s -> s));
  }

  public SponsorMemberPreviewDTO previewMember(Long userId, String lookup) {
    Sponsor sponsor = requireActiveSponsor(userId);
    return sponsorCheckinService.preview(sponsor, lookup);
  }

  public SponsorCheckinDTO createCheckin(Long userId, SponsorCheckinCreateDTO dto) {
    Sponsor sponsor = requireActiveSponsor(userId);
    return sponsorCheckinService.create(sponsor, dto);
  }

  public Pageable<SponsorCheckinDTO> listCheckins(Long userId, ListSponsorCheckinsQueryDTO query) {
    Sponsor sponsor = requireSponsor(userId);
    return sponsorCheckinService.listBySponsor(
        sponsor.getId(),
        query.getStartDate(),
        query.getEndDate(),
        query.toPageDTO());
  }

  public Pageable<BenefitDTO> listOwnBenefits(Long userId, ListSponsorBenefitsQueryDTO query) {
    Sponsor sponsor = requireActiveSponsor(userId);
    Pageable<Benefit> pageable = benefitRepository.findActiveBySponsorId(sponsor.getId(), query.toPageDTO());
    return BenefitMapper.fromEntityToPageableDTO(pageable);
  }

  private Sponsor requireSponsor(Long userId) {
    return findByUserId(userId)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.SPONSOR_NOT_FOUND.getMessage()));
  }

  private Sponsor requireActiveSponsor(Long userId) {
    Sponsor sponsor = requireSponsor(userId);
    if (!sponsor.isActive()) {
      throw new ForbiddenException(MessageErrorEnum.SPONSOR_NOT_ACTIVE.getMessage());
    }
    return sponsor;
  }

  public void deactivateByUserId(Long userId) {
    findByUserId(userId).ifPresent(sponsor -> {
      sponsor.setActive(false);
      sponsor.setLastActiveSponsorship(Instant.now());
      sponsorRepository.persistAndFlush(sponsor);

      benefitRepository.deactivateBySponsorId(sponsor.getId());
    });
  }

  public void activateByUserId(Long userId) {
    findByUserId(userId).ifPresent(sponsor -> {
      sponsor.setActive(true);
      sponsorRepository.persistAndFlush(sponsor);
      benefitRepository.activateBySponsorId(sponsor.getId());
    });
  }

  public void deleteByUserId(Long userId) {
    findByUserId(userId).ifPresent(sponsor -> {
      benefitRepository.deleteBySponsorId(sponsor.getId());
      sponsorRepository.delete(sponsor);
    });
  }

  public void validateUniqueWhatsapp(String whatsapp) {
    sponsorRepository.findByWhatsapp(whatsapp).ifPresent(s -> {
      throw new ConflictException(MessageErrorEnum.SPONSOR_ALREADY_EXISTS.getMessage());
    });
  }

  private void validateUniqueWhatsappForUpdate(String whatsapp, Long sponsorId) {
    if (sponsorRepository.existsByWhatsappAndIdNot(whatsapp, sponsorId)) {
      throw new ConflictException(MessageErrorEnum.SPONSOR_ALREADY_EXISTS.getMessage());
    }
  }

  private void validatePersonaByEntityType(String entityTypeStr, String persona) {
    SponsorEntityTypeEnum entityType = SponsorEntityTypeEnum.valueOf(entityTypeStr.toUpperCase());

    if (SponsorEntityTypeEnum.PERSON.equals(entityType)
        && (persona == null || persona.isBlank())) {
      throw new BusinessException(MessageErrorEnum.SPONSOR_PERSONA_REQUIRED_FOR_PERSON.getMessage(), 400);
    }
  }

  private void validatePersonaByEntityTypeForUpdate(SponsorDataUpdateDTO dto, Sponsor sponsor) {
    SponsorEntityTypeEnum effectiveEntityType = dto.getEntityType() != null
        ? SponsorEntityTypeEnum.valueOf(dto.getEntityType().toUpperCase())
        : sponsor.getEntityType();

    String effectivePersona = dto.getPersona() != null
        ? dto.getPersona()
        : (sponsor.getPersona() != null ? sponsor.getPersona().name() : null);

    if (SponsorEntityTypeEnum.PERSON.equals(effectiveEntityType)
        && (effectivePersona == null || effectivePersona.isBlank())) {
      throw new BusinessException(MessageErrorEnum.SPONSOR_PERSONA_REQUIRED_FOR_PERSON.getMessage(), 400);
    }
  }
}
