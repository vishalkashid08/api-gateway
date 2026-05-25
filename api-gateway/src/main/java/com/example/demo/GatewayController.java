package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.server.reactive.ServerHttpRequest;

import reactor.core.publisher.Mono;
import org.springframework.core.io.buffer.DataBuffer;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/")
public class GatewayController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    // ✅ COMMON FORWARD METHOD (NOW SUPPORTS FILE UPLOAD)
    private Mono<ResponseEntity<byte[]>> forwardRequest(
            ServerHttpRequest request,
            String baseUrl,
            String pathPrefix
    ) {

        String path = request.getURI().getPath().replace(pathPrefix, "");
        String targetUrl = baseUrl + pathPrefix + path;

        WebClient.RequestBodySpec requestSpec = webClientBuilder.build()
                .method(request.getMethod())
                .uri(targetUrl);

        // ✅ Copy ALL headers (important for multipart)
        request.getHeaders().forEach((key, value) ->
                requestSpec.header(key, value.toArray(new String[0]))
        );

        // ✅ Forward BODY properly (important 🔥)
        return requestSpec
                .body(request.getBody(), DataBuffer.class)
                .retrieve()
                .toEntity(byte[].class);
    }

    // ✅ USER SERVICE
    @RequestMapping("/users/**")
    public Mono<ResponseEntity<byte[]>> routeUserService(ServerHttpRequest request) {
        return forwardRequest(request, "http://localhost:8081", "/users");
    }

    // ✅ QUESTION SERVICE
    @RequestMapping("/questions/**")
    public Mono<ResponseEntity<byte[]>> routeQuestionService(ServerHttpRequest request) {
        return forwardRequest(request, "http://localhost:8082", "/questions");
    }

    // ✅ ANSWER SERVICE
    @RequestMapping("/answers/**")
    public Mono<ResponseEntity<byte[]>> routeAnswerService(ServerHttpRequest request) {
        return forwardRequest(request, "http://localhost:8083", "/answers");
    }

    // ✅ NOTES SERVICE (FILE UPLOAD WILL WORK NOW)
    @RequestMapping("/notes/**")
    public Mono<ResponseEntity<byte[]>> routeNotesService(ServerHttpRequest request) {
        return forwardRequest(request, "http://localhost:8084", "/notes");
    }
}