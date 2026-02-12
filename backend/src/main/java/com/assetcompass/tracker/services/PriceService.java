package com.assetcompass.tracker.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
public class PriceService {

    @Value("${app.alphavantage.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Fetches the latest price for a symbol (e.g., "IBM", "BTC")
    public BigDecimal fetchPrice(String symbol) {
        try {
            // Check if it's Crypto or Stock (Simple logic: BTC/ETH are crypto)
            String url;
            if (symbol.equalsIgnoreCase("BTC") || symbol.equalsIgnoreCase("ETH")) {
                // CRYPTO URL
                url = "https://www.alphavantage.co/query?function=CURRENCY_EXCHANGE_RATE&from_currency=" + symbol + "&to_currency=USD&apikey=" + apiKey;
            } else {
                // STOCK URL
                url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=" + apiKey;
            }

            String jsonResponse = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(jsonResponse);

            if (symbol.equalsIgnoreCase("BTC") || symbol.equalsIgnoreCase("ETH")) {
                // Parse Crypto Response
                String price = root.path("Realtime Currency Exchange Rate").path("5. Exchange Rate").asText();
                return new BigDecimal(price);
            } else {
                // Parse Stock Response
                String price = root.path("Global Quote").path("05. price").asText();
                return new BigDecimal(price);
            }

        } catch (Exception e) {
            System.out.println("Error fetching price for " + symbol + ": " + e.getMessage());
            return BigDecimal.ZERO; // Return 0 if API fails
        }
    }
}