package com.example.assignment_student_att_system.service;

import javafx.application.Platform;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ApiService {
    // Thread Pool (ExecutorService) for asynchronous HTTP operations
    private final ExecutorService executor;
    private final HttpClient httpClient;

    public ApiService() {
        // Create a fixed thread pool with 3 worker threads
        this.executor = Executors.newFixedThreadPool(3, r -> {
            Thread t = new Thread(r, "api-worker-thread");
            t.setDaemon(true);
            return t;
        });
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Asynchronously fetches educational quote or system sync status using background Thread Pool.
     * Triggers consumer callback on JavaFX UI thread via Platform.runLater.
     */
    public void fetchEducationalQuoteAsync(Consumer<String> callback) {
        executor.submit(() -> {
            String quoteText;
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://dummyjson.com/quotes/random"))
                        .timeout(Duration.ofSeconds(4))
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JSONObject json = new JSONObject(response.body());
                    String q = json.optString("quote", "Education is the key to unlock the golden door of freedom.");
                    String author = json.optString("author", "George Washington Carver");
                    quoteText = "\"" + q + "\"\n— " + author;
                } else {
                    quoteText = "\"Education is the most powerful weapon which you can use to change the world.\"\n— Nelson Mandela";
                }
            } catch (Exception e) {
                // Fallback quote if offline
                quoteText = "\"The secret of getting ahead is getting started.\"\n— Mark Twain";
            }

            final String result = quoteText;
            // Post result back to JavaFX UI Thread
            Platform.runLater(() -> callback.accept(result));
        });
    }

    /**
     * Simulates background cloud synchronization / external API post using Thread Pool.
     */
    public void syncAttendanceDataAsync(int recordCount, Consumer<Boolean> onComplete) {
        executor.submit(() -> {
            try {
                // Simulate network latency (1.5 seconds) in thread pool
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Platform.runLater(() -> onComplete.accept(true));
        });
    }

    public void shutdown() {
        executor.shutdown();
    }
}
