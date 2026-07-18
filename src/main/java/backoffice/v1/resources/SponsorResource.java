package backoffice.v1.resources;

import java.util.Optional;

import org.eclipse.microprofile.jwt.JsonWebToken;

import backoffice.common.database.Pageable;
import backoffice.common.exceptions.MessageErrorEnum;
import backoffice.common.exceptions.customs.ForbiddenException;
import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.benefit.BenefitDTO;
import backoffice.v1.dtos.sponsor.ListSponsorBenefitsQueryDTO;
import backoffice.v1.dtos.sponsor.ListSponsorCheckinsQueryDTO;
import backoffice.v1.dtos.sponsor.SponsorCheckinCreateDTO;
import backoffice.v1.dtos.sponsor.SponsorCheckinDTO;
import backoffice.v1.dtos.sponsor.SponsorMemberLookupQueryDTO;
import backoffice.v1.dtos.sponsor.SponsorMemberPreviewDTO;
import backoffice.v1.openapi.api.SponsorApi;
import backoffice.v1.services.SponsorService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

public class SponsorResource implements SponsorApi {

  @Inject
  private SponsorService service;

  @Inject
  SecurityIdentity identity;

  private Long currentUserId() {
    return currentActorId()
        .orElseThrow(() -> new ForbiddenException(MessageErrorEnum.SPONSOR_NOT_FOUND.getMessage()));
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

  @Override
  public Response previewMember(@Valid @BeanParam SponsorMemberLookupQueryDTO query) {
    SponsorMemberPreviewDTO result = service.previewMember(currentUserId(), query.getLookup());
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @Override
  public Response createCheckin(@Valid SponsorCheckinCreateDTO dto) {
    SponsorCheckinDTO result = service.createCheckin(currentUserId(), dto);
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @Override
  public Response listCheckins(@Valid @BeanParam ListSponsorCheckinsQueryDTO query) {
    Pageable<SponsorCheckinDTO> result = service.listCheckins(currentUserId(), query);
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @Override
  public Response listBenefits(@Valid @BeanParam ListSponsorBenefitsQueryDTO query) {
    Pageable<BenefitDTO> result = service.listOwnBenefits(currentUserId(), query);
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }
}
