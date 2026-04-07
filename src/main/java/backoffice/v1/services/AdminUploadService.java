package backoffice.v1.services;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import backoffice.common.exceptions.MessageErrorEnum;
import backoffice.common.exceptions.customs.BadRequestException;
import backoffice.common.exceptions.customs.BusinessException;
import backoffice.common.exceptions.customs.NotFoundException;
import backoffice.common.services.StorageService;
import backoffice.common.services.UploadRateLimiter;
import backoffice.v1.dtos.upload.UploadConfirmDTO;
import backoffice.v1.dtos.upload.UploadConfirmResponseDTO;
import backoffice.v1.dtos.upload.UploadDeleteDTO;
import backoffice.v1.dtos.upload.UploadDeleteResponseDTO;
import backoffice.v1.dtos.upload.UploadInitDTO;
import backoffice.v1.dtos.upload.UploadInitResponseDTO;
import backoffice.v1.dtos.upload.UploadTargetEnum;
import backoffice.v1.entities.Sponsor;
import backoffice.v1.entities.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AdminUploadService {

  private static final Logger LOG = Logger.getLogger(AdminUploadService.class);

  private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
      "image/png",
      "image/jpeg",
      "image/webp");

  @Inject
  StorageService storageService;

  @Inject
  UploadRateLimiter uploadRateLimiter;

  @Inject
  UserService userService;

  @Inject
  SponsorService sponsorService;

  @Inject
  @ConfigProperty(name = "backoffice.storage.key-env-prefix")
  String keyEnvPrefix;

  @Inject
  @ConfigProperty(name = "backoffice.storage.cdn.public-base-url")
  Optional<String> cdnPublicBaseUrl;

  @Inject
  @ConfigProperty(name = "backoffice.storage.upload.max-bytes")
  long maxUploadBytes;

  @Inject
  @ConfigProperty(name = "backoffice.storage.upload.sign-ttl-seconds")
  int signTtlSeconds;

  public UploadInitResponseDTO initUpload(UploadInitDTO dto, Optional<Long> actorId) {
    uploadRateLimiter.consumeOrThrow(rateLimitKey(actorId));
    String contentType = normalizeContentType(dto.getContentType());
    validateContentType(contentType);
    validateSize(dto.getSize());

    if (!storageService.isConfigured()) {
      throw new BusinessException(MessageErrorEnum.STORAGE_NOT_CONFIGURED.getMessage(), 503);
    }

    resolveEntity(dto.getEntity(), dto.getEntityId());

    String prefix = buildKeyPrefix(dto.getEntity(), dto.getEntityId());
    String objectKey = prefix + LocalDate.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy/MM")) + "/"
        + System.currentTimeMillis() + "_" + randomSuffix() + "." + fileExtension(contentType);

    LOG.infof("upload init entity=%s entityId=%d actorId=%s objectKey=%s",
        dto.getEntity(), dto.getEntityId(), actorId.map(Object::toString).orElse("-"), objectKey);

    Duration ttl = Duration.ofSeconds(Math.max(60, signTtlSeconds));
    String uploadUrl = storageService.presignPut(objectKey, contentType, ttl);
    String publicUrl = buildPublicUrl(objectKey);

    return UploadInitResponseDTO.builder()
        .objectKey(objectKey)
        .uploadUrl(uploadUrl)
        .publicUrl(publicUrl)
        .expiresIn(signTtlSeconds)
        .build();
  }

  @Transactional
  public UploadConfirmResponseDTO confirmUpload(UploadConfirmDTO dto, Optional<Long> actorId) {
    uploadRateLimiter.consumeOrThrow(rateLimitKey(actorId));
    if (!storageService.isConfigured()) {
      throw new BusinessException(MessageErrorEnum.STORAGE_NOT_CONFIGURED.getMessage(), 503);
    }

    String prefix = buildKeyPrefix(dto.getEntity(), dto.getEntityId());
    if (!storageService.keyMatchesEntityPrefix(dto.getObjectKey(), prefix)) {
      throw new BadRequestException(MessageErrorEnum.UPLOAD_KEY_INVALID.getMessage());
    }

    if (!storageService.objectExists(dto.getObjectKey())) {
      throw new BusinessException(MessageErrorEnum.UPLOAD_OBJECT_NOT_FOUND.getMessage(), 404);
    }

    String previousKey = null;
    switch (dto.getEntity()) {
      case USER -> {
        User user = userService.findById(dto.getEntityId())
            .orElseThrow(() -> new NotFoundException(MessageErrorEnum.USER_NOT_FOUND.getMessage()));
        previousKey = user.getAvatarUrl();
        user.setAvatarUrl(dto.getObjectKey());
      }
      case SPONSOR -> {
        Sponsor sponsor = sponsorService.findById(dto.getEntityId())
            .orElseThrow(() -> new NotFoundException(MessageErrorEnum.SPONSOR_NOT_FOUND.getMessage()));
        previousKey = sponsor.getLogoUrl();
        sponsor.setLogoUrl(dto.getObjectKey());
      }
    }

    if (previousKey != null && !previousKey.isBlank() && !previousKey.equals(dto.getObjectKey())) {
      storageService.deleteObject(previousKey);
    }

    LOG.infof("upload confirm entity=%s entityId=%d actorId=%s objectKey=%s",
        dto.getEntity(), dto.getEntityId(), actorId.map(Object::toString).orElse("-"), dto.getObjectKey());

    return UploadConfirmResponseDTO.builder()
        .id(dto.getEntityId())
        .entity(entityJsonValue(dto.getEntity()))
        .entityId(dto.getEntityId())
        .objectKey(dto.getObjectKey())
        .url(buildPublicUrl(dto.getObjectKey()))
        .build();
  }

  @Transactional
  public UploadDeleteResponseDTO deleteUpload(UploadDeleteDTO dto, Optional<Long> actorId) {
    String prefix = buildKeyPrefix(dto.getEntity(), dto.getEntityId());
    if (!storageService.keyMatchesEntityPrefix(dto.getObjectKey(), prefix)) {
      throw new BadRequestException(MessageErrorEnum.UPLOAD_KEY_INVALID.getMessage());
    }

    String current = switch (dto.getEntity()) {
      case USER -> userService.findById(dto.getEntityId())
          .orElseThrow(() -> new NotFoundException(MessageErrorEnum.USER_NOT_FOUND.getMessage()))
          .getAvatarUrl();
      case SPONSOR -> sponsorService.findById(dto.getEntityId())
          .orElseThrow(() -> new NotFoundException(MessageErrorEnum.SPONSOR_NOT_FOUND.getMessage()))
          .getLogoUrl();
    };

    if (current == null || current.isBlank()) {
      LOG.infof("upload delete noop (sem imagem) entity=%s entityId=%d", dto.getEntity(), dto.getEntityId());
      return UploadDeleteResponseDTO.builder().success(true).build();
    }

    if (!dto.getObjectKey().equals(current)) {
      throw new BadRequestException(MessageErrorEnum.UPLOAD_KEY_MISMATCH.getMessage());
    }

    switch (dto.getEntity()) {
      case USER -> {
        User user = userService.findById(dto.getEntityId()).orElseThrow();
        user.setAvatarUrl(null);
      }
      case SPONSOR -> {
        Sponsor sponsor = sponsorService.findById(dto.getEntityId()).orElseThrow();
        sponsor.setLogoUrl(null);
      }
    }

    if (storageService.isConfigured()) {
      storageService.deleteObject(dto.getObjectKey());
    }

    LOG.infof("upload delete entity=%s entityId=%d actorId=%s objectKey=%s",
        dto.getEntity(), dto.getEntityId(), actorId.map(Object::toString).orElse("-"), dto.getObjectKey());

    return UploadDeleteResponseDTO.builder().success(true).build();
  }

  private void resolveEntity(UploadTargetEnum target, Long entityId) {
    switch (target) {
      case USER -> userService.findById(entityId)
          .orElseThrow(() -> new NotFoundException(MessageErrorEnum.USER_NOT_FOUND.getMessage()));
      case SPONSOR -> sponsorService.findById(entityId)
          .orElseThrow(() -> new NotFoundException(MessageErrorEnum.SPONSOR_NOT_FOUND.getMessage()));
    }
  }

  private String buildKeyPrefix(UploadTargetEnum target, Long entityId) {
    return switch (target) {
      case USER -> keyEnvPrefix + "/users/avatar/" + entityId + "/";
      case SPONSOR -> keyEnvPrefix + "/sponsors/logo/" + entityId + "/";
    };
  }

  private void validateContentType(String normalized) {
    if (!ALLOWED_CONTENT_TYPES.contains(normalized)) {
      throw new BusinessException(MessageErrorEnum.UPLOAD_CONTENT_TYPE_NOT_ALLOWED.getMessage(), 415);
    }
  }

  private void validateSize(long size) {
    if (size > maxUploadBytes) {
      throw new BusinessException(MessageErrorEnum.UPLOAD_SIZE_EXCEEDED.getMessage(), 413);
    }
  }

  private static String normalizeContentType(String raw) {
    if (raw == null) {
      return "";
    }
    return raw.trim().toLowerCase(Locale.ROOT);
  }

  private static String fileExtension(String normalizedContentType) {
    return switch (normalizedContentType) {
      case "image/png" -> "png";
      case "image/jpeg" -> "jpg";
      case "image/webp" -> "webp";
      default -> "bin";
    };
  }

  private String buildPublicUrl(String objectKey) {
    String base = cdnPublicBaseUrl.map(String::trim).filter(s -> !s.isEmpty()).orElse("");
    if (base.isEmpty()) {
      return objectKey;
    }
    String normalized = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    return normalized + "/" + objectKey;
  }

  private static String randomSuffix() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
  }

  private static String rateLimitKey(Optional<Long> actorId) {
    return actorId.map(String::valueOf).orElse("anonymous");
  }

  private static String entityJsonValue(UploadTargetEnum entity) {
    return entity == UploadTargetEnum.USER ? "user" : "sponsor";
  }
}
