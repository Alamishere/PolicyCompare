package com.policycompare;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
public class CompareController {
  private final StorageService storage;
  private final WorkFlowService workflow;

  public CompareController(StorageService storage, WorkFlowService workflow) {
    this.storage = storage; this.workflow = workflow;
  }

  @PostMapping(value="/compare", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
  public Map<String,String> compare(@RequestPart("fileA") MultipartFile fileA,
                                    @RequestPart("fileB") MultipartFile fileB) throws Exception {
    String jobId = UUID.randomUUID().toString();
    String keyA = jobId + "/inputs/policyA" + ext(fileA.getOriginalFilename());
    String keyB = jobId + "/inputs/policyB" + ext(fileB.getOriginalFilename());

    storage.putInput(fileA.getBytes(), keyA);
    storage.putInput(fileB.getBytes(), keyB);

    workflow.startCompare(jobId, storage.getInputBucket(), storage.getOutputBucket(), keyA, keyB);
    return Map.of("jobId", jobId);
  }

  @GetMapping("/result/{jobId}")
  public ResponseEntity<?> result(@PathVariable String jobId) {
    Optional<String> json = storage.getResultIfExists(jobId + "/result.json");
    return json.<ResponseEntity<?>>map(ResponseEntity::ok)
      .orElseGet(() -> ResponseEntity.ok(Map.of("status","PENDING")));
  }

  private static String ext(String name) {
    if (name == null) return ".txt";
    int i = name.lastIndexOf('.');
    return i>=0 ? name.substring(i) : ".txt";
  }
}
