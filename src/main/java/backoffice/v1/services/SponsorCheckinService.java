package backoffice.v1.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import backoffice.common.database.Pageable;
import backoffice.common.exceptions.MessageErrorEnum;
import backoffice.common.exceptions.customs.BadRequestException;
import backoffice.common.exceptions.customs.BusinessException;
import backoffice.common.exceptions.customs.NotFoundException;
import backoffice.common.mappers.SponsorCheckinMapper;
import backoffice.common.utils.DocumentUtils;
import backoffice.v1.dtos.checkin.AdminCheckinDTO;
import backoffice.v1.dtos.common.PageDTO;
import backoffice.v1.dtos.member.MemberCheckinHistoryDTO;
import backoffice.v1.dtos.member.MemberCheckinSponsorOptionDTO;
import backoffice.v1.dtos.sponsor.SponsorCheckinCreateDTO;
import backoffice.v1.dtos.sponsor.SponsorCheckinDTO;
import backoffice.v1.dtos.sponsor.SponsorMemberPreviewDTO;
import backoffice.v1.entities.Member;
import backoffice.v1.entities.Sponsor;
import backoffice.v1.entities.SponsorMemberCheckin;
import backoffice.v1.entities.User;
import backoffice.v1.entities.enums.CheckinLookupTypeEnum;
import backoffice.v1.entities.enums.UserTypeEnum;
import backoffice.v1.repositories.SponsorMemberCheckinRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SponsorCheckinService {

  @ConfigProperty(name = "backoffice.billing.zone-id", defaultValue = "America/Sao_Paulo")
  String zoneId;

  @Inject
  private SponsorMemberCheckinRepository checkinRepository;

  @Inject
  private UserService userService;

  @Inject
  private MemberService memberService;

  @Inject
  private SponsorService sponsorService;

  private ZoneId zone() {
    return ZoneId.of(zoneId);
  }

  public SponsorMemberPreviewDTO preview(Sponsor sponsor, String lookup) {
    ResolvedLookup resolved = resolveMember(lookup);
    Member member = resolved.member();
    boolean eligible = member.getUser().isAccountActive();
    String ineligibleReason = eligible ? null : MessageErrorEnum.CHECKIN_MEMBER_NOT_ELIGIBLE.getMessage();

    DayBounds today = dayBounds(LocalDate.now(zone()));
    Optional<SponsorMemberCheckin> latestToday = checkinRepository.findLatestTodayBySponsorAndMember(
        sponsor.getId(), member.getId(), today.start(), today.endExclusive());

    return SponsorCheckinMapper.toPreview(
        member,
        eligible,
        ineligibleReason,
        latestToday.isPresent(),
        latestToday.map(SponsorMemberCheckin::getCreatedAt).orElse(null));
  }

  @Transactional
  public SponsorCheckinDTO create(Sponsor sponsor, SponsorCheckinCreateDTO dto) {
    ResolvedLookup resolved = resolveMember(dto.getLookup());
    Member member = resolved.member();
    boolean eligible = member.getUser().isAccountActive();
    boolean confirmDuplicate = Boolean.TRUE.equals(dto.getConfirmDuplicateToday());

    DayBounds today = dayBounds(LocalDate.now(zone()));
    Optional<SponsorMemberCheckin> latestToday = checkinRepository.findLatestTodayBySponsorAndMember(
        sponsor.getId(), member.getId(), today.start(), today.endExclusive());

    if (latestToday.isPresent() && !confirmDuplicate) {
      throw new BusinessException(MessageErrorEnum.CHECKIN_DUPLICATE_CONFIRMATION_REQUIRED.getMessage(), 400);
    }

    SponsorMemberCheckin checkin = SponsorMemberCheckin.builder()
        .sponsor(sponsor)
        .member(member)
        .validated(eligible)
        .reason(eligible ? null : MessageErrorEnum.CHECKIN_MEMBER_NOT_ELIGIBLE.getMessage())
        .lookupType(resolved.lookupType())
        .duplicateConfirmed(confirmDuplicate)
        .build();

    checkinRepository.persistAndFlush(checkin);
    return SponsorCheckinMapper.fromEntity(checkin);
  }

  public Pageable<SponsorCheckinDTO> listBySponsor(
      Long sponsorId, LocalDate startDate, LocalDate endDate, PageDTO pageDTO) {
    InstantRange range = resolveHistoryRange(startDate, endDate);
    Pageable<SponsorMemberCheckin> pageable = checkinRepository.listBySponsor(
        sponsorId, range.startInclusive(), range.endExclusive(), pageDTO);
    return SponsorCheckinMapper.fromEntityToPageableDTO(pageable);
  }

  public Pageable<SponsorCheckinDTO> listByMember(
      Long memberId, LocalDate startDate, LocalDate endDate, PageDTO pageDTO) {
    InstantRange range = resolveHistoryRange(startDate, endDate);
    Pageable<SponsorMemberCheckin> pageable = checkinRepository.listByMember(
        memberId, range.startInclusive(), range.endExclusive(), pageDTO);
    return SponsorCheckinMapper.fromEntityToPageableDTO(pageable);
  }

  public Pageable<AdminCheckinDTO> listCheckinsForAdmin(
      Long sponsorId,
      Long memberUserId,
      LocalDate startDate,
      LocalDate endDate,
      Boolean validated,
      PageDTO pageDTO) {
    Long resolvedMemberId = null;
    if (memberUserId != null) {
      User user = userService.findById(memberUserId)
          .orElseThrow(() -> new NotFoundException(MessageErrorEnum.USER_NOT_FOUND.getMessage()));
      if (user.getType() != UserTypeEnum.MEMBER) {
        throw new BadRequestException(MessageErrorEnum.CHECKIN_USER_NOT_MEMBER.getMessage());
      }
      Member member = memberService.findByUserId(memberUserId)
          .orElseThrow(() -> new NotFoundException(MessageErrorEnum.MEMBER_NOT_FOUND.getMessage()));
      resolvedMemberId = member.getId();
    }

    if (sponsorId != null) {
      sponsorService.findById(sponsorId)
          .orElseThrow(() -> new NotFoundException(MessageErrorEnum.SPONSOR_NOT_FOUND.getMessage()));
    }

    InstantRange range = resolveHistoryRange(startDate, endDate);
    Pageable<SponsorMemberCheckin> pageable = checkinRepository.listAllForAdmin(
        sponsorId,
        resolvedMemberId,
        validated,
        range.startInclusive(),
        range.endExclusive(),
        pageDTO);
    return SponsorCheckinMapper.fromEntityToAdminCheckinPageableDTO(pageable);
  }

  public Pageable<SponsorCheckinDTO> listAll(LocalDate startDate, LocalDate endDate, PageDTO pageDTO) {
    InstantRange range = resolveHistoryRange(startDate, endDate);
    Pageable<SponsorMemberCheckin> pageable = checkinRepository.listAll(
        range.startInclusive(), range.endExclusive(), pageDTO);
    return SponsorCheckinMapper.fromEntityToPageableDTO(pageable);
  }

  public Pageable<MemberCheckinHistoryDTO> listValidatedCheckinsForMember(
      Long memberId, Long sponsorId, LocalDate startDate, LocalDate endDate, PageDTO pageDTO) {
    InstantRange range = resolveHistoryRange(startDate, endDate);
    Pageable<SponsorMemberCheckin> pageable = checkinRepository.listValidatedByMemberAndOptionalSponsor(
        memberId, sponsorId, range.startInclusive(), range.endExclusive(), pageDTO);
    return SponsorCheckinMapper.fromEntityToMemberHistoryPageableDTO(pageable);
  }

  public List<MemberCheckinSponsorOptionDTO> listSponsorsForMemberHistory(Long memberId) {
    return checkinRepository.listDistinctSponsorsByMember(memberId).stream()
        .map(SponsorCheckinMapper::fromSponsorToMemberCheckinSponsorOptionDTO)
        .toList();
  }

  private ResolvedLookup resolveMember(String rawLookup) {
    ParsedLookup parsed = parseLookup(rawLookup);

    User user = switch (parsed.lookupType()) {
      case CODE -> userService.findByCode(parsed.value())
          .orElseThrow(() -> new NotFoundException(MessageErrorEnum.MEMBER_NOT_FOUND.getMessage()));
      case DOCUMENT -> userService.findByDocument(parsed.value())
          .orElseThrow(() -> new NotFoundException(MessageErrorEnum.MEMBER_NOT_FOUND.getMessage()));
    };

    if (user.getType() != UserTypeEnum.MEMBER) {
      throw new BadRequestException(MessageErrorEnum.CHECKIN_USER_NOT_MEMBER.getMessage());
    }

    Member member = memberService.findByUserId(user.getId())
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.MEMBER_NOT_FOUND.getMessage()));

    return new ResolvedLookup(member, parsed.lookupType());
  }

  private ParsedLookup parseLookup(String rawLookup) {
    if (rawLookup == null || rawLookup.isBlank()) {
      throw new BadRequestException(MessageErrorEnum.CHECKIN_LOOKUP_INVALID.getMessage());
    }

    String trimmed = rawLookup.trim();
    String digits = DocumentUtils.normalize(trimmed);

    if (digits.length() == 11 && trimmed.replaceAll("[.\\-\\s]", "").equals(digits)) {
      return new ParsedLookup(CheckinLookupTypeEnum.DOCUMENT, digits);
    }

    // Aceita CPF mascarado (só dígitos após strip = 11)
    if (digits.length() == 11 && trimmed.chars().filter(Character::isDigit).count() == 11
        && trimmed.chars().allMatch(c -> Character.isDigit(c) || c == '.' || c == '-' || c == ' ')) {
      return new ParsedLookup(CheckinLookupTypeEnum.DOCUMENT, digits);
    }

    String codeCandidate = trimmed.toUpperCase(Locale.ROOT);
    if (codeCandidate.length() == 5 && codeCandidate.chars().allMatch(Character::isLetterOrDigit)) {
      return new ParsedLookup(CheckinLookupTypeEnum.CODE, codeCandidate);
    }

    throw new BadRequestException(MessageErrorEnum.CHECKIN_LOOKUP_INVALID.getMessage());
  }

  private InstantRange resolveHistoryRange(LocalDate startDate, LocalDate endDate) {
    if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
      throw new BadRequestException(MessageErrorEnum.CHECKIN_HISTORY_PERIOD_INVALID.getMessage());
    }
    Instant startInclusive = startDate != null ? dayBounds(startDate).start() : null;
    Instant endExclusive = endDate != null ? dayBounds(endDate).endExclusive() : null;
    return new InstantRange(startInclusive, endExclusive);
  }

  private DayBounds dayBounds(LocalDate day) {
    Instant start = day.atStartOfDay(zone()).toInstant();
    Instant endExclusive = day.plusDays(1).atStartOfDay(zone()).toInstant();
    return new DayBounds(start, endExclusive);
  }

  private record ParsedLookup(CheckinLookupTypeEnum lookupType, String value) {
  }

  private record ResolvedLookup(Member member, CheckinLookupTypeEnum lookupType) {
  }

  private record DayBounds(Instant start, Instant endExclusive) {
  }

  private record InstantRange(Instant startInclusive, Instant endExclusive) {
  }
}
