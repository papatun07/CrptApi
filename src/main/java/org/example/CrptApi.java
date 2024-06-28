package org.example;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import javax.print.Doc;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.util.concurrent.*;

import static java.net.http.HttpClient.Version.HTTP_1_1;

@Data
public class CrptApi {

    private final ObjectMapper objectMapper;

    private final HttpClient httpClient;

    private final Semaphore semaphore;

    private final ScheduledExecutorService executorService;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.semaphore = new Semaphore(requestLimit);
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.executorService = Executors.newScheduledThreadPool(1);
        long interval = timeUnit.toMillis(1);
        this.executorService.scheduleAtFixedRate(() -> semaphore.release(), interval, interval, TimeUnit.MILLISECONDS);
    }


    public void createDocument(Document document, String signature) throws IOException, InterruptedException {
        semaphore.acquire();
        String json = objectMapper.writeValueAsString(document);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                .version(HTTP_1_1)
                .header("Content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }
}

class Document {
    @JsonProperty("description")
    private Description description;
    @JsonProperty("doc_id")
    private String docId;
    @JsonProperty("doc_type")
    private String docType;
    @JsonProperty("doc_status")
    private String docStatus;
    @JsonProperty("importRequest")
    private boolean importRequest;
    @JsonProperty("owner_inn")
    private String ownerInn;
    @JsonProperty("participant_inn")
    private String participantInn;
    @JsonProperty("producer_inn")
    private String producerInn;
    @JsonProperty("production_date")
    private LocalDate productionDate;
    @JsonProperty("production_type")
    private String productionType;
    @JsonProperty("products")
    private Product[] products;
    @JsonProperty("reg_date")
    private LocalDate regDate;
    @JsonProperty("reg_number")
    private String regNumber;
}

class Description {
    @JsonProperty("participantInn")
    private String participantInn;
}

class Product {
    @JsonProperty("certificate_document")
    private String certificateDocument;
    @JsonProperty("certificate_document_date")
    private LocalDate certificateDocumentDate;
    @JsonProperty("certificate_document_number")
    private String certificateDocumentNumber;
    @JsonProperty("owner_inn")
    private String ownerInn;
    @JsonProperty("producer_inn")
    private String producerInn;
    @JsonProperty("production_date")
    private LocalDate productionDate;
    @JsonProperty("tnved_code")
    private String tnvedCode;
    @JsonProperty("uit_code")
    private String uitCode;
    @JsonProperty("uitu_code")
    private String uituCode;
}