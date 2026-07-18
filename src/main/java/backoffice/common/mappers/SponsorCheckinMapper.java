package backoffice.common.mappers;

import java.time.Instant;
import java.util.List;

import backoffice.common.database.Pageable;
import backoffice.common.utils.MaskUtils;
import backoffice.v1.dtos.checkin.AdminCheckinDTO;
import backoffice.v1.dtos.checkin.AdminCheckinMemberMinDTO;
import backoffice.v1.dtos.checkin.AdminCheckinSponsorMinDTO;
import backoffice.v1.dtos.member.MemberCheckinHistoryDTO;
import backoffice.v1.dtos.member.MemberCheckinHistorySponsorDTO;
import backoffice.v1.dtos.member.MemberCheckinSponsorOptionDTO;
import backoffice.v1.dtos.sponsor.SponsorCheckinDTO;
import backoffice.v1.dtos.sponsor.SponsorCheckinMemberMinDTO;
import backoffice.v1.dtos.sponsor.SponsorMemberPreviewDTO;
import backoffice.v1.entities.Member;
import backoffice.v1.entities.Sponsor;
import backoffice.v1.entities.SponsorMemberCheckin;
import backoffice.v1.entities.User;

public final class SponsorCheckinMapper {

  private SponsorCheckinMapper() {
  }

  public static SponsorMemberPreviewDTO toPreview(
      Member member,
      boolean eligible,
      String ineligibleReason,
      boolean alreadyCheckedInToday,
      Instant lastCheckinAt) {
    User user = member.getUser();
    return SponsorMemberPreviewDTO.builder()
        .id(member.getId())
        .name(member.getFullname() != null && !member.getFullname().isBlank()
            ? member.getFullname()
            : user.getName())
        .avatarUrl(user.getAvatarUrl())
        .code(user.getCode())
        .documentMasked(MaskUtils.maskCpfPartial(user.getDocument()))
        .memberType(member.getType())
        .eligible(eligible)
        .ineligibleReason(ineligibleReason)
        .alreadyCheckedInToday(alreadyCheckedInToday)
        .lastCheckinAt(lastCheckinAt)
        .build();
  }

  public static SponsorCheckinDTO fromEntity(SponsorMemberCheckin checkin) {
    Member member = checkin.getMember();
    User user = member.getUser();
    return SponsorCheckinDTO.builder()
        .id(checkin.getId())
        .validated(checkin.isValidated())
        .reason(checkin.getReason())
        .duplicateConfirmed(checkin.isDuplicateConfirmed())
        .lookupType(checkin.getLookupType())
        .createdAt(checkin.getCreatedAt())
        .member(SponsorCheckinMemberMinDTO.builder()
            .id(member.getId())
            .name(member.getFullname() != null && !member.getFullname().isBlank()
                ? member.getFullname()
                : user.getName())
            .code(user.getCode())
            .documentMasked(MaskUtils.maskCpfPartial(user.getDocument()))
            .build())
        .build();
  }

  public static Pageable<SponsorCheckinDTO> fromEntityToPageableDTO(Pageable<SponsorMemberCheckin> data) {
    List<SponsorCheckinDTO> dtos = data.getData().stream()
        .map(SponsorCheckinMapper::fromEntity)
        .toList();

    return Pageable.<SponsorCheckinDTO>builder()
        .data(dtos)
        .totalElements(data.getTotalElements())
        .totalPages(data.getTotalPages())
        .pageSize(data.getPageSize())
        .currentPage(data.getCurrentPage())
        .build();
  }

  public static MemberCheckinHistoryDTO fromEntityToMemberHistoryDTO(SponsorMemberCheckin checkin) {
    Sponsor sponsor = checkin.getSponsor();
    return MemberCheckinHistoryDTO.builder()
        .id(checkin.getId())
        .checkedInAt(checkin.getCreatedAt())
        .validated(checkin.isValidated())
        .duplicateConfirmed(checkin.isDuplicateConfirmed())
        .sponsor(fromSponsorToMemberHistorySponsorDTO(sponsor))
        .build();
  }

  public static Pageable<MemberCheckinHistoryDTO> fromEntityToMemberHistoryPageableDTO(
      Pageable<SponsorMemberCheckin> data) {
    List<MemberCheckinHistoryDTO> dtos = data.getData().stream()
        .map(SponsorCheckinMapper::fromEntityToMemberHistoryDTO)
        .toList();

    return Pageable.<MemberCheckinHistoryDTO>builder()
        .data(dtos)
        .totalElements(data.getTotalElements())
        .totalPages(data.getTotalPages())
        .pageSize(data.getPageSize())
        .currentPage(data.getCurrentPage())
        .build();
  }

  public static MemberCheckinHistorySponsorDTO fromSponsorToMemberHistorySponsorDTO(Sponsor sponsor) {
    return MemberCheckinHistorySponsorDTO.builder()
        .id(sponsor.getId())
        .publicName(sponsor.getPublicName())
        .logoUrl(sponsor.getLogoUrl())
        .tier(sponsor.getTier() != null ? sponsor.getTier().name() : null)
        .active(sponsor.isActive())
        .build();
  }

  public static MemberCheckinSponsorOptionDTO fromSponsorToMemberCheckinSponsorOptionDTO(Sponsor sponsor) {
    return MemberCheckinSponsorOptionDTO.builder()
        .id(sponsor.getId())
        .publicName(sponsor.getPublicName())
        .logoUrl(sponsor.getLogoUrl())
        .active(sponsor.isActive())
        .build();
  }

  public static AdminCheckinDTO fromEntityToAdminCheckinDTO(SponsorMemberCheckin checkin) {
    Member member = checkin.getMember();
    User user = member.getUser();
    Sponsor sponsor = checkin.getSponsor();
    return AdminCheckinDTO.builder()
        .id(checkin.getId())
        .checkedInAt(checkin.getCreatedAt())
        .validated(checkin.isValidated())
        .reason(checkin.getReason())
        .duplicateConfirmed(checkin.isDuplicateConfirmed())
        .lookupType(checkin.getLookupType())
        .sponsor(fromSponsorToAdminCheckinSponsorMinDTO(sponsor))
        .member(AdminCheckinMemberMinDTO.builder()
            .id(member.getId())
            .userId(user.getId())
            .name(member.getFullname() != null && !member.getFullname().isBlank()
                ? member.getFullname()
                : user.getName())
            .code(user.getCode())
            .documentMasked(MaskUtils.maskCpfPartial(user.getDocument()))
            .build())
        .build();
  }

  public static Pageable<AdminCheckinDTO> fromEntityToAdminCheckinPageableDTO(Pageable<SponsorMemberCheckin> data) {
    List<AdminCheckinDTO> dtos = data.getData().stream()
        .map(SponsorCheckinMapper::fromEntityToAdminCheckinDTO)
        .toList();

    return Pageable.<AdminCheckinDTO>builder()
        .data(dtos)
        .totalElements(data.getTotalElements())
        .totalPages(data.getTotalPages())
        .pageSize(data.getPageSize())
        .currentPage(data.getCurrentPage())
        .build();
  }

  public static AdminCheckinSponsorMinDTO fromSponsorToAdminCheckinSponsorMinDTO(Sponsor sponsor) {
    return AdminCheckinSponsorMinDTO.builder()
        .id(sponsor.getId())
        .publicName(sponsor.getPublicName())
        .tier(sponsor.getTier() != null ? sponsor.getTier().name() : null)
        .active(sponsor.isActive())
        .build();
  }
}
