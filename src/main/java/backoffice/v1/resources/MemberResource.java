package backoffice.v1.resources;

import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.jwt.JsonWebToken;

import backoffice.common.database.Pageable;
import backoffice.common.exceptions.MessageErrorEnum;
import backoffice.common.exceptions.customs.ForbiddenException;
import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.member.ListMemberBenefitsQueryDTO;
import backoffice.v1.dtos.member.ListMemberCheckinsQueryDTO;
import backoffice.v1.dtos.member.MemberBenefitDTO;
import backoffice.v1.dtos.member.MemberCardDTO;
import backoffice.v1.dtos.member.MemberCheckinHistoryDTO;
import backoffice.v1.dtos.member.MemberCheckinSponsorOptionDTO;
import backoffice.v1.openapi.api.MemberApi;
import backoffice.v1.services.MemberService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

public class MemberResource implements MemberApi {

  @Inject
  private MemberService service;

  @Inject
  SecurityIdentity identity;

  @Override
  public Response getMyCard() {
    MemberCardDTO result = service.findCardByUserId(currentUserId());
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @Override
  public Response listCheckins(@Valid @BeanParam ListMemberCheckinsQueryDTO query) {
    Pageable<MemberCheckinHistoryDTO> result = service.listCheckinsByUserId(
        currentUserId(),
        query.getSponsorId(),
        query.getStartDate(),
        query.getEndDate(),
        query.toPageDTO());
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @Override
  public Response listCheckinSponsors() {
    List<MemberCheckinSponsorOptionDTO> result = service.listCheckinSponsorOptionsByUserId(currentUserId());
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @Override
  public Response listBenefits(@Valid @BeanParam ListMemberBenefitsQueryDTO query) {
    Pageable<MemberBenefitDTO> result = service.listBenefitsByUserId(
        currentUserId(),
        query.getSponsorId(),
        query.toPageDTO());
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  private Long currentUserId() {
    return currentActorId()
        .orElseThrow(() -> new ForbiddenException(MessageErrorEnum.MEMBER_CARD_USER_NOT_MEMBER.getMessage()));
  }

  private Optional<Long> currentActorId() {
    if (identity == null || identity.isAnonymous()) {
      return Optional.empty();
    }
    if (identity.getPrincipal() instanceof JsonWebToken jwt) {
      Object id = jwt.getClaim("id");
      if (id == null) {
        return Optional.empty();
      }
      if (id instanceof Number n) {
        return Optional.of(n.longValue());
      }
      try {
        return Optional.of(Long.parseLong(id.toString()));
      } catch (NumberFormatException e) {
        return Optional.empty();
      }
    }
    return Optional.empty();
  }
}
