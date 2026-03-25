package backoffice.common.mappers;

import java.util.List;

import backoffice.common.database.Pageable;
import backoffice.v1.dtos.benefit.BenefitCreateDTO;
import backoffice.v1.dtos.benefit.BenefitDTO;
import backoffice.v1.dtos.benefit.BenefitUpdateDTO;
import backoffice.v1.dtos.sponsor.SponsorMinDTO;
import backoffice.v1.entities.Benefit;
import backoffice.v1.entities.Sponsor;

public class BenefitMapper {

  public static Benefit fromDTO(BenefitCreateDTO dto, Sponsor sponsor) {
    return Benefit.builder()
        .name(dto.getName())
        .description(dto.getDescription())
        .address(dto.getAddress())
        .sponsor(sponsor)
        .build();
  }

  public static BenefitDTO fromEntityToDto(Benefit benefit) {
    return BenefitDTO.builder()
        .id(benefit.getId())
        .name(benefit.getName())
        .description(benefit.getDescription())
        .address(benefit.getAddress())
        .isActive(benefit.isActive())
        .sponsor(benefit.getSponsor() != null ? toSponsorMinDTO(benefit.getSponsor()) : null)
        .createdAt(benefit.getCreatedAt())
        .build();
  }

  public static void applyUpdate(BenefitUpdateDTO dto, Benefit benefit, Sponsor sponsor) {
    if (dto.getName() != null) benefit.setName(dto.getName());
    if (dto.getDescription() != null) benefit.setDescription(dto.getDescription());
    if (dto.getAddress() != null) benefit.setAddress(dto.getAddress());
    benefit.setSponsor(sponsor);
  }

  public static Pageable<BenefitDTO> fromEntityToPageableDTO(Pageable<Benefit> data) {
    List<BenefitDTO> dtos = data.getData().stream()
        .map(BenefitMapper::fromEntityToDto)
        .toList();

    return Pageable.<BenefitDTO>builder()
        .data(dtos)
        .totalElements(data.getTotalElements())
        .totalPages(data.getTotalPages())
        .pageSize(data.getPageSize())
        .currentPage(data.getCurrentPage())
        .build();
  }

  private static SponsorMinDTO toSponsorMinDTO(Sponsor sponsor) {
    return SponsorMinDTO.builder()
        .id(sponsor.getId())
        .publicName(sponsor.getPublicName())
        .tier(sponsor.getTier())
        .isActive(sponsor.isActive())
        .build();
  }
}
