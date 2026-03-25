package backoffice.v1.services;

import backoffice.common.database.Pageable;
import backoffice.common.exceptions.MessageErrorEnum;
import backoffice.common.exceptions.customs.NotFoundException;
import backoffice.common.mappers.BenefitMapper;
import backoffice.v1.dtos.benefit.BenefitCreateDTO;
import backoffice.v1.dtos.benefit.BenefitDTO;
import backoffice.v1.dtos.benefit.BenefitUpdateDTO;
import backoffice.v1.dtos.common.PageDTO;
import backoffice.v1.entities.Benefit;
import backoffice.v1.entities.Sponsor;
import backoffice.v1.repositories.BenefitRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class BenefitService {

  @Inject
  private BenefitRepository benefitRepository;

  @Inject
  private SponsorService sponsorService;

  @Transactional
  public BenefitDTO create(BenefitCreateDTO dto) {
    Sponsor sponsor = resolveSponsor(dto.getSponsorId());

    Benefit benefit = BenefitMapper.fromDTO(dto, sponsor);
    benefitRepository.persistAndFlush(benefit);

    return BenefitMapper.fromEntityToDto(benefit);
  }

  @Transactional
  public BenefitDTO update(Long benefitId, BenefitUpdateDTO dto) {
    Benefit benefit = findEntityById(benefitId);
    Sponsor sponsor = dto.getSponsorId() != null
        ? resolveSponsor(dto.getSponsorId())
        : benefit.getSponsor();

    BenefitMapper.applyUpdate(dto, benefit, sponsor);
    benefitRepository.persistAndFlush(benefit);

    return BenefitMapper.fromEntityToDto(benefit);
  }

  public BenefitDTO findById(Long benefitId) {
    return BenefitMapper.fromEntityToDto(findEntityById(benefitId));
  }

  public Pageable<BenefitDTO> list(Long sponsorId, Boolean isActive, PageDTO pageDTO) {
    Pageable<Benefit> pageable = benefitRepository.findAllPaginated(sponsorId, isActive, pageDTO);
    return BenefitMapper.fromEntityToPageableDTO(pageable);
  }

  @Transactional
  public void deactivate(Long benefitId) {
    Benefit benefit = findEntityById(benefitId);
    benefit.setActive(false);
    benefitRepository.persistAndFlush(benefit);
  }

  @Transactional
  public void delete(Long benefitId) {
    Benefit benefit = findEntityById(benefitId);
    benefitRepository.delete(benefit);
  }

  private Benefit findEntityById(Long benefitId) {
    return benefitRepository.findByIdOptional(benefitId)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.BENEFIT_NOT_FOUND.getMessage()));
  }

  private Sponsor resolveSponsor(Long sponsorId) {
    if (sponsorId == null) return null;

    return sponsorService.findById(sponsorId)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.SPONSOR_NOT_FOUND.getMessage()));
  }
}
