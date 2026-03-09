package backoffice.common.mappers;

import backoffice.v1.dtos.sponsor.SponsorCreateDTO;
import backoffice.v1.dtos.sponsor.SponsorDTO;
import backoffice.v1.dtos.user.UserMinDTO;
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
        .user(UserMinDTO.builder()
            .id(sponsor.getUser().getId())
            .email(sponsor.getUser().getEmail())
            .name(sponsor.getUser().getName())
            .type(sponsor.getUser().getType())
            .createdAt(sponsor.getUser().getCreatedAt())
            .build())
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
}
