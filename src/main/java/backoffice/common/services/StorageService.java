package backoffice.common.services;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import backoffice.common.exceptions.MessageErrorEnum;
import backoffice.common.exceptions.customs.BusinessException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@ApplicationScoped
public class StorageService {

  private static final Logger LOG = Logger.getLogger(StorageService.class);

  @Inject
  @ConfigProperty(name = "backoffice.storage.r2.endpoint")
  URI r2Endpoint;

  @Inject
  @ConfigProperty(name = "backoffice.storage.r2.access-key-id")
  Optional<String> accessKeyIdOpt;

  @Inject
  @ConfigProperty(name = "backoffice.storage.r2.secret-access-key")
  Optional<String> secretAccessKeyOpt;

  @Inject
  @ConfigProperty(name = "backoffice.storage.r2.bucket")
  Optional<String> bucketOpt;

  @Inject
  @ConfigProperty(name = "backoffice.storage.r2.region", defaultValue = "auto")
  String regionName;

  private volatile S3Client s3Client;
  private volatile S3Presigner presigner;

  private String bucket;

  @PostConstruct
  void init() {
    String accessKeyId = accessKeyIdOpt.map(String::trim).filter(s -> !s.isEmpty()).orElse("");
    String secretAccessKey = secretAccessKeyOpt.map(String::trim).filter(s -> !s.isEmpty()).orElse("");
    this.bucket = bucketOpt.map(String::trim).filter(s -> !s.isEmpty()).orElse("");
    if (isBlank(accessKeyId) || isBlank(secretAccessKey) || isBlank(this.bucket)) {
      LOG.warn("R2 não configurado (defina backoffice.storage.r2.* ou variáveis equivalentes); operações de storage falharão até configurado");
      return;
    }
    var credentials = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
    var s3Configuration = S3Configuration.builder()
        .pathStyleAccessEnabled(true)
        .build();
    Region region = Region.of(regionName);
    this.s3Client = S3Client.builder()
        .endpointOverride(r2Endpoint)
        .region(region)
        .credentialsProvider(credentials)
        .serviceConfiguration(s3Configuration)
        .build();
    this.presigner = S3Presigner.builder()
        .endpointOverride(r2Endpoint)
        .region(region)
        .credentialsProvider(credentials)
        .serviceConfiguration(s3Configuration)
        .build();
  }

  @PreDestroy
  void shutdown() {
    if (s3Client != null) {
      s3Client.close();
    }
    if (presigner != null) {
      presigner.close();
    }
  }

  public boolean isConfigured() {
    return s3Client != null && presigner != null;
  }

  public String presignPut(String objectKey, String contentType, Duration ttl) {
    ensureConfigured();
    try {
      PutObjectRequest put = PutObjectRequest.builder()
          .bucket(bucket)
          .key(objectKey)
          .contentType(contentType)
          .build();
      PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
          .signatureDuration(ttl)
          .putObjectRequest(put)
          .build();
      return presigner.presignPutObject(presignRequest).url().toString();
    } catch (SdkClientException e) {
      LOG.error("Falha ao gerar URL assinada para PUT", e);
      throw new BusinessException(MessageErrorEnum.STORAGE_OPERATION_FAILED.getMessage(), 500);
    }
  }

  public boolean objectExists(String objectKey) {
    ensureConfigured();
    try {
      s3Client.headObject(HeadObjectRequest.builder()
          .bucket(bucket)
          .key(objectKey)
          .build());
      return true;
    } catch (NoSuchKeyException e) {
      return false;
    } catch (SdkClientException e) {
      LOG.errorf(e, "HeadObject falhou para key=%s", objectKey);
      throw new BusinessException(MessageErrorEnum.STORAGE_OPERATION_FAILED.getMessage(), 500);
    }
  }

  public void deleteObject(String objectKey) {
    if (!isConfigured() || objectKey == null || objectKey.isBlank() || isBlank(bucket)) {
      return;
    }
    try {
      s3Client.deleteObject(DeleteObjectRequest.builder()
          .bucket(bucket)
          .key(objectKey)
          .build());
    } catch (SdkClientException e) {
      LOG.errorf(e, "DeleteObject falhou para key=%s", objectKey);
      throw new BusinessException(MessageErrorEnum.STORAGE_OPERATION_FAILED.getMessage(), 500);
    }
  }

  /**
   * Garante que a chave pertence ao prefixo esperado (entidade + id), evitando paths arbitrários do cliente.
   */
  public boolean keyMatchesEntityPrefix(String objectKey, String entityPrefix) {
    if (objectKey == null || entityPrefix == null) {
      return false;
    }
    return objectKey.startsWith(entityPrefix);
  }

  private void ensureConfigured() {
    if (!isConfigured()) {
      throw new BusinessException(MessageErrorEnum.STORAGE_NOT_CONFIGURED.getMessage(), 503);
    }
  }

  private static boolean isBlank(String s) {
    return s == null || s.isBlank();
  }
}
