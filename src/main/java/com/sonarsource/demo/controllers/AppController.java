package com.sonarsource.demo.controllers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.sonarsource.demo.config.AWSConfig;

@Controller
public class AppController {

  @Autowired
  private AWSConfig config;
  private static final String S3_BUCKET_NAME = "sonar.devoxx2023.demo";

  @ResponseBody
  @GetMapping("/")
  public ResponseEntity<InputStreamResource> getImage(@RequestParam(value="imageUrl") String imageUrl) {

    try {
      String urlHash = this.getUrlHash(imageUrl);
      InputStreamResource resource = this.getImageResource(imageUrl, urlHash);

      return ResponseEntity
      .status(HttpStatus.OK)
      .header("Content-Disposition", "attachment; filename="+urlHash)
      .body(resource);
    } catch(IOException exception) {
      return ResponseEntity
        .status(HttpStatus.NOT_FOUND).build();
    } catch(NoSuchAlgorithmException exception) {
      return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  private String getUrlHash(String url) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("MD5");
    byte[] urlHash = md.digest(url.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(urlHash);
  }

  private InputStreamResource getImageResource(String imageUrl, String urlHash) throws IOException {

/*    if(!imageUrl.startsWith("https://www.gravatar.com/")) {
        throw new FileNotFoundException("Invalid domain for image "+imageUrl);
    }*/

    URL url = new URL(imageUrl);

    AmazonS3 s3client = this.config.amazonS3();
    if(!s3client.doesBucketExist(AppController.S3_BUCKET_NAME)) {
      throw new FileNotFoundException("No bucket with name "+AppController.S3_BUCKET_NAME);
    }

    InputStream content = null;
    try {
      S3Object object = s3client.getObject(AppController.S3_BUCKET_NAME, urlHash);
      content = object.getObjectContent();
    } catch (AmazonClientException exception) {
      Logger logger = LoggerFactory.getLogger(AppController.class);
      logger.debug("Image with hash {} not found in bucket {}", urlHash, AppController.S3_BUCKET_NAME);
    }

    if(content != null) {
      return new InputStreamResource(content);
    } else {
      URLConnection connection = url.openConnection();
      s3client.putObject(AppController.S3_BUCKET_NAME, urlHash, connection.getInputStream(), null);
      return new InputStreamResource(connection.getInputStream());
    }
  }
}
