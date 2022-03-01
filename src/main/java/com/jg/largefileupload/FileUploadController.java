package com.jg.largefileupload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FileUploadController {

    private final SelfApiClient selfApiClient;

    @Value("${upload.directory:files}")
    private String uploadDirectory;

    @PostMapping("/bridge-upload")
    public String handleBridgeUpload(@RequestParam("file") final MultipartFile file) {
        final RestTemplate restTemplate = new RestTemplate();
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setBufferRequestBody(false);
        restTemplate.setRequestFactory(requestFactory);

        log.info("Bridge upload started.");
        final String uploadUrl = "http://localhost:8080/upload";
        final HttpMethod httpMethod = HttpMethod.POST;
        final MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
        bodyMap.add("file", file);
        final HttpHeaders header = new HttpHeaders();
        final HttpEntity<MultiValueMap> request = new HttpEntity<>(bodyMap, header);

        final String response = restTemplate.exchange(uploadUrl, httpMethod, request, String.class).getBody();
        log.info("Bridge upload ended.");

        return response;


//        log.info("Bridge upload started.");
//        final String result = selfApiClient.fileUpload(file);
//        log.info("Bridge upload ended.");
//        return result;
    }

    @PostMapping("/upload")
    public String handleUpload(@RequestParam("file") final MultipartFile file) throws Exception {
        final InputStream inputStream = file.getInputStream();
        final OutputStream outputStream = new FileOutputStream(uploadDirectory + "/" + file.getOriginalFilename());

        byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        log.info("Writing InputStream to OutputStream.");
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        log.info("Finished writing InputStream to OutputStream.");
        IOUtils.closeQuietly(inputStream);
        IOUtils.closeQuietly(outputStream);

        return "success!";
    }
}
