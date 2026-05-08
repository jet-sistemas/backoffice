package backoffice.v1.services;

import java.util.Optional;

import backoffice.common.database.Pageable;
import backoffice.common.exceptions.MessageErrorEnum;
import backoffice.common.exceptions.customs.ConflictException;
import backoffice.common.exceptions.customs.NotFoundException;
import backoffice.common.mappers.MemberMapper;
import backoffice.v1.dtos.common.PageDTO;
import backoffice.v1.dtos.member.MemberDTO;
import backoffice.v1.dtos.member.MemberDataCreateDTO;
import backoffice.v1.entities.Member;
import backoffice.v1.entities.User;
import backoffice.v1.entities.enums.MemberTypeEnum;
import backoffice.v1.entities.enums.UserTypeEnum;
import backoffice.v1.repositories.MemberRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MemberService {
  @Inject
  private MemberRepository memberRepository;

  public MemberDTO create(MemberDataCreateDTO dto, User user) {
    validateMemberForUser(user);
    validateUniqueWhatsapp(dto.getWhatsapp());

    Member member = MemberMapper.fromCreateData(dto, user);
    memberRepository.persistAndFlush(member);
    return MemberMapper.fromEntityToDTO(member);
  }

  public Optional<Member> findById(Long memberId) {
    return memberRepository.findByIdOptional(memberId);
  }

  public MemberDTO findDTOById(Long memberId) {
    Member member = memberRepository.findByIdOptional(memberId)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.MEMBER_NOT_FOUND.getMessage()));
    validateMemberForUser(member.getUser());
    return MemberMapper.fromEntityToDTO(member);
  }

  public Pageable<MemberDTO> list(MemberTypeEnum type, Boolean isActive, String search, PageDTO pageDTO) {
    Pageable<Member> pageable = memberRepository.findAllPaginated(type, isActive, search, pageDTO);
    return MemberMapper.fromEntityToPageableDTO(pageable);
  }

  public Optional<Member> findByUserId(Long userId) {
    return memberRepository.findByUserId(userId);
  }

  public void validateUniqueWhatsapp(String whatsapp) {
    if (memberRepository.existsByWhatsapp(whatsapp)) {
      throw new ConflictException(MessageErrorEnum.MEMBER_ALREADY_EXISTS.getMessage());
    }
  }

  public void validateMemberForUser(User user) {
    if (!UserTypeEnum.MEMBER.equals(user.getType())) {
      throw new ConflictException(MessageErrorEnum.USER_INVALID_TYPE_ENUM.getMessage());
    }
  }
}
