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

    public BigDecimal getStockPrice(String ticker) {
        // Prepare the API URL
        String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + ticker + "&apikey=" + apiKey;

        try {
            // Fetch JSON
            String response = restTemplate.getForObject(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            // Extract "05. price" from the "Global Quote" object
            JsonNode quote = root.path("Global Quote");
            if (quote.has("05. price")) {
                String priceString = quote.get("05. price").asText();
                return new BigDecimal(priceString); // Return e.g., 255.50
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch stock price for " + ticker + ": " + e.getMessage());
        }

        // If API fails, return null (Controller will handle the error)
        return null;
    }
}