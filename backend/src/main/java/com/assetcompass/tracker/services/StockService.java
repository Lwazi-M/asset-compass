package com.assetcompass.tracker.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;

@Service
public class StockService {

    @Value("${app.alphavantage.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // 1. Get Live Price (Existing)
    public BigDecimal getStockPrice(String ticker) {
        String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + ticker + "&apikey=" + apiKey;

        try {
            String response = restTemplate.getForObject(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            JsonNode quote = root.path("Global Quote");
            if (quote.has("05. price")) {
                String priceString = quote.get("05. price").asText();
                return new BigDecimal(priceString);
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch stock price for " + ticker + ": " + e.getMessage());
        }
        return null;
    }

    // 2. NEW: Search for Stocks (e.g. "Take-Two" -> "TTWO")
    public String searchStocks(String query) {
        String url = "https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords=" + query + "&apikey=" + apiKey;
        try {
            // We return the raw JSON string so the frontend can parse the list of matches
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            System.err.println("Search failed: " + e.getMessage());
            return "{\"bestMatches\": []}"; // Return empty list on error
        }
    }
}