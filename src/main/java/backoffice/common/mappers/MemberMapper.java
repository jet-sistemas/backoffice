package backoffice.common.mappers;

import java.util.List;

import backoffice.common.database.Pageable;
import backoffice.v1.dtos.member.MemberDataCreateDTO;
import backoffice.v1.dtos.member.MemberDTO;
import backoffice.v1.entities.Member;
import backoffice.v1.entities.User;
import backoffice.v1.entities.enums.MemberTypeEnum;

public class MemberMapper {
  public static Member fromCreateData(MemberDataCreateDTO dto, User user) {
    return Member.builder()
        .user(user)
        .fullname(dto.getFullname())
        .whatsapp(dto.getWhatsapp())
        .type(MemberTypeEnum.valueOf(dto.getType().toUpperCase()))
        .build();
  }

  public static MemberDTO fromEntityToDTO(Member member) {
    return MemberDTO.builder()
        .id(member.getId())
        .userId(member.getUser().getId())
        .email(member.getUser().getEmail())
        .code(member.getUser().getCode())
        .document(member.getUser().getDocument())
        .fullname(member.getFullname())
        .whatsapp(member.getWhatsapp())
        .type(member.getType())
        .isActive(member.isActive())
        .createdAt(member.getCreatedAt())
        .build();
  }

  public static Pageable<MemberDTO> fromEntityToPageableDTO(Pageable<Member> data) {
    List<MemberDTO> dtos = data.getData().stream()
        .map(MemberMapper::fromEntityToDTO)
        .toList();

    return Pageable.<MemberDTO>builder()
        .data(dtos)
        .totalElements(data.getTotalElements())
        .totalPages(data.getTotalPages())
        .pageSize(data.getPageSize())
        .currentPage(data.getCurrentPage())
        .build();
  }
}
