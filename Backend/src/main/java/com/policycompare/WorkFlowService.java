package com.policycompare;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;

@Service
public class WorkFlowService {
  private final LambdaClient lambda;
  private final String lambdaName;

  public WorkFlowService(LambdaClient lambda, @Value("${app.lambdaName}") String lambdaName) {
    this.lambda = lambda; this.lambdaName = lambdaName;
  }

  public void startCompare(String jobId, String inputBucket, String outputBucket, String keyA, String keyB) {
    String payload = """
      {"jobId":"%s","inputBucket":"%s","outputBucket":"%s","keyA":"%s","keyB":"%s"}
      """.formatted(jobId, inputBucket, outputBucket, keyA, keyB);
    lambda.invoke(b -> b.functionName(lambdaName).payload(SdkBytes.fromUtf8String(payload)));
  }
}

