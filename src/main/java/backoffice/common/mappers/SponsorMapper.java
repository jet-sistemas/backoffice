package backoffice.common.mappers;

import java.time.Instant;
import java.util.List;

import backoffice.common.database.Pageable;
import backoffice.v1.dtos.sponsor.SponsorCreateDTO;
import backoffice.v1.dtos.sponsor.SponsorDTO;
import backoffice.v1.dtos.sponsor.SponsorUpdateDTO;
import backoffice.v1.entities.Sponsor;
import backoffice.v1.entities.User;
import backoffice.v1.entities.enums.SponsorEntityTypeEnum;
import backoffice.v1.entities.enums.SponsorPersonaEnum;
import backoffice.v1.entities.enums.SponsorTierEnum;

public class SponsorMapper {

  public static Sponsor fromDTO(SponsorCreateDTO dto, User user) {
    return Sponsor.builder()
        .user(user)
        .publicName(dto.getPublicName())
        .tier(SponsorTierEnum.valueOf(dto.getTier().toUpperCase()))
        .entityType(SponsorEntityTypeEnum.valueOf(dto.getEntityType().toUpperCase()))
        .persona(dto.getPersona() != null && !dto.getPersona().isBlank()
            ? SponsorPersonaEnum.valueOf(dto.getPersona().toUpperCase())
            : null)
        .logoUrl(dto.getLogoUrl())
        .site(dto.getSite())
        .instagram(dto.getInstagram())
        .whatsapp(dto.getWhatsapp())
        .build();
  }

  public static SponsorDTO fromEntityToSponsorDTO(Sponsor sponsor) {
    return SponsorDTO.builder()
        .id(sponsor.getId())
        .user(UserMapper.fromEntityToMinimal(sponsor.getUser()))
        .publicName(sponsor.getPublicName())
        .tier(sponsor.getTier())
        .entityType(sponsor.getEntityType())
        .persona(sponsor.getPersona())
        .logoUrl(sponsor.getLogoUrl())
        .site(sponsor.getSite())
        .instagram(sponsor.getInstagram())
        .whatsapp(sponsor.getWhatsapp())
        .isActive(sponsor.isActive())
        .lastActiveSponsorship(sponsor.getLastActiveSponsorship())
        .createdAt(sponsor.getCreatedAt())
        .build();
  }

  public static void applyUpdate(SponsorUpdateDTO dto, Sponsor sponsor, User user) {
    if (dto.getName() != null) user.setName(dto.getName());
    if (dto.getEmail() != null) user.setEmail(dto.getEmail());
    if (dto.getDocument() != null) user.setDocument(dto.getDocument());
    if (dto.getAvatarUrl() != null) user.setAvatarUrl(dto.getAvatarUrl());

    if (dto.getPublicName() != null) sponsor.setPublicName(dto.getPublicName());
    if (dto.getTier() != null) sponsor.setTier(SponsorTierEnum.valueOf(dto.getTier().toUpperCase()));
    if (dto.getEntityType() != null) sponsor.setEntityType(SponsorEntityTypeEnum.valueOf(dto.getEntityType().toUpperCase()));
    if (dto.getPersona() != null) {
      sponsor.setPersona(dto.getPersona().isBlank() ? null : SponsorPersonaEnum.valueOf(dto.getPersona().toUpperCase()));
    }
    if (dto.getLogoUrl() != null) sponsor.setLogoUrl(dto.getLogoUrl());
    if (dto.getSite() != null) sponsor.setSite(dto.getSite());
    if (dto.getInstagram() != null) sponsor.setInstagram(dto.getInstagram());
    if (dto.getWhatsapp() != null) sponsor.setWhatsapp(dto.getWhatsapp());

    if (dto.getIsActive() != null) {
      boolean wasActive = sponsor.isActive();
      sponsor.setActive(dto.getIsActive());

      if (wasActive && !dto.getIsActive()) {
        sponsor.setLastActiveSponsorship(Instant.now());
      }
    }
  }

  public static List<SponsorDTO> fromEntityToListDTO(List<Sponsor> sponsors) {
    return sponsors.stream()
        .map(SponsorMapper::fromEntityToSponsorDTO)
        .toList();
  }

  public static Pageable<SponsorDTO> fromEntityToPageableDTO(Pageable<Sponsor> data) {
    List<SponsorDTO> dtos = data.getData().stream()
        .map(SponsorMapper::fromEntityToSponsorDTO)
        .toList();

    return Pageable.<SponsorDTO>builder()
        .data(dtos)
        .totalElements(data.getTotalElements())
        .totalPages(data.getTotalPages())
        .pageSize(data.getPageSize())
        .currentPage(data.getCurrentPage())
        .build();
  }
}
