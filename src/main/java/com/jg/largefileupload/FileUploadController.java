package com.jg.largefileupload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FileUploadController {

    @Value("${upload.directory:files}")
    private String uploadDirectory;

    @Value("${feign.self-client.url:http://file-api:8080}")
    private String selfClientUrl;

    // Single Microservice

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

    // Bridged (two microservices)

    // Microservice 1 Endpoint
    @PostMapping("/bridge-upload-from")
    public String bridgeUploadFrom(@RequestParam("file") final MultipartFile file) throws IOException {
        final RestTemplate restTemplate = new RestTemplate();

        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setBufferRequestBody(false);
        requestFactory.setConnectTimeout(1000 * 120);
        requestFactory.setReadTimeout(1000 * 120);
        // upload 16KB at a time.
        requestFactory.setChunkSize(16 * 1024);
        restTemplate.setRequestFactory(requestFactory);

        return restTemplate.postForEntity(selfClientUrl + "/bridge-upload-to" +
                        "?file-name=" + file.getOriginalFilename() +
                        "&file-size=" + file.getSize() +
                        "&content-type=" + file.getContentType(),
                new InputStreamResource(file.getInputStream()),
                String.class)
                .getBody();
    }

    // Microservice 2 Endpoint
    @PostMapping("/bridge-upload-to")
    public String bridgeUploadTo(@RequestParam("file-name") final String fileName,
                                 @RequestParam("file-size") final long fileSize,
                                 @RequestParam("content-type") final String contentType,
                                 @RequestBody final InputStreamResource file) throws Exception {
        final InputStream inputStream = file.getInputStream();
        final OutputStream outputStream = new FileOutputStream(uploadDirectory + "/" + fileName);

        // Read 16Kb at a time.
        byte[] buffer = new byte[16 * 1024];
        int totalBytesRead = 0;
        int bytesRead;
        log.info("Writing InputStream to OutputStream.");
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
            final Double percentage = (100d * (double) totalBytesRead) / (double) fileSize;
            System.out.println(new DecimalFormat("0.00").format(percentage) + "%");
        }
        log.info("Finished writing InputStream to OutputStream.");
        IOUtils.closeQuietly(inputStream);
        IOUtils.closeQuietly(outputStream);

        return "success!";
    }
}
