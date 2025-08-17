package com.policycompare;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class StorageService {
  private final S3Client s3;
  private final String inputBucket;
  private final String outputBucket;

  public StorageService(S3Client s3,
                        @Value("${app.inputBucket}") String inputBucket,
                        @Value("${app.outputBucket}") String outputBucket) {
    this.s3 = s3; this.inputBucket = inputBucket; this.outputBucket = outputBucket;
  }

  public void putInput(byte[] bytes, String key) {
    s3.putObject(b -> b.bucket(inputBucket).key(key), RequestBody.fromBytes(bytes));
  }

  public Optional<String> getResultIfExists(String key) {
    try {
      var bytes = s3.getObjectAsBytes(GetObjectRequest.builder()
          .bucket(outputBucket).key(key).build());
      return Optional.of(bytes.asString(StandardCharsets.UTF_8));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public String getInputBucket(){ return inputBucket; }
  public String getOutputBucket(){ return outputBucket; }
}
