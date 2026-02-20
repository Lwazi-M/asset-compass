package com.assetcompass.tracker.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class StockService {

    @Value("${app.alphavantage.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // 1. Get Live Price (With Fail-Safe)
    public BigDecimal getStockPrice(String ticker) {
        String url = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + ticker + "&apikey=" + apiKey;

        try {
            String response = restTemplate.getForObject(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            JsonNode quote = root.path("Global Quote");

            // If AlphaVantage returns the price successfully
            if (quote.has("05. price")) {
                String priceString = quote.get("05. price").asText();
                return new BigDecimal(priceString);
            } else {
                // LIMIT REACHED: AlphaVantage blocked us. Use fallback data!
                System.out.println("⚠️ AlphaVantage Limit Reached. Using Fallback Price for " + ticker);
                return generateFallbackPrice(ticker);
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch stock price for " + ticker + ". Using Fallback.");
            return generateFallbackPrice(ticker);
        }
    }

    // 2. Search for Stocks (With Fail-Safe)
    public String searchStocks(String query) {
        String url = "https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords=" + query + "&apikey=" + apiKey;

        try {
            String response = restTemplate.getForObject(url, String.class);

            // If AlphaVantage sends an error/limit message instead of search results, use fallback
            if (response == null || !response.contains("bestMatches")) {
                System.out.println("⚠️ AlphaVantage Limit Reached. Using Fallback Search.");
                return generateFallbackSearch(query);
            }
            return response;
        } catch (Exception e) {
            System.err.println("Search failed. Using fallback.");
            return generateFallbackSearch(query);
        }
    }

    // --- PORTFOLIO DEMO FALLBACKS ---

    private BigDecimal generateFallbackPrice(String ticker) {
        // Generates a consistent, realistic-looking fake price based on the letters in the ticker
        // (e.g., AAPL will always return the exact same fake price, making it look real)
        double simulatedPrice = Math.abs(ticker.hashCode() % 400) + 25.50;
        return BigDecimal.valueOf(simulatedPrice).setScale(2, RoundingMode.HALF_UP);
    }

    private String generateFallbackSearch(String query) {
        String safeQuery = query.toUpperCase().replaceAll("[^A-Z]", "");
        if (safeQuery.isEmpty()) safeQuery = "ASSET";

        // Returns perfectly formatted JSON so the frontend doesn't crash
        return String.format("""
            {
                "bestMatches": [
                    { "1. symbol": "%s", "2. name": "%s Corporation", "3. type": "Equity" },
                    { "1. symbol": "%sX", "2. name": "%s Innovation ETF", "3. type": "ETF" }
                ]
            }
            """, safeQuery, safeQuery, safeQuery, safeQuery);
    }
}