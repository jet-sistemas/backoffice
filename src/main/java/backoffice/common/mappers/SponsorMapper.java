package backoffice.common.mappers;

import java.util.List;

import backoffice.common.database.Pageable;
import backoffice.v1.dtos.sponsor.SponsorCreateDTO;
import backoffice.v1.dtos.sponsor.SponsorDTO;
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
        .persona(SponsorPersonaEnum.valueOf(dto.getPersona().toUpperCase()))
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
        .createdAt(sponsor.getCreatedAt())
        .build();
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
