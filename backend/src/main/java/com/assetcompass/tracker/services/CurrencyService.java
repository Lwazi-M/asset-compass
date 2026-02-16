package com.assetcompass.tracker.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CurrencyService {

    @Value("${app.alphavantage.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // Fallback rate in case API fails (Prevents app crash)
    private BigDecimal cachedUsdToZarRate = new BigDecimal("18.50");

    /**
     * Fetches the live USD -> ZAR exchange rate.
     * Example: Returns 19.25
     */
    public BigDecimal getUsdToZarRate() {
        String url = "https://www.alphavantage.co/query?function=CURRENCY_EXCHANGE_RATE&from_currency=USD&to_currency=ZAR&apikey=" + apiKey;

        try {
            String response = restTemplate.getForObject(url, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            // Navigate the JSON response from AlphaVantage
            JsonNode rateNode = root.path("Realtime Currency Exchange Rate").path("5. Exchange Rate");

            if (!rateNode.isMissingNode()) {
                cachedUsdToZarRate = new BigDecimal(rateNode.asText());
                System.out.println("✅ Live USD/ZAR Rate Fetched: R" + cachedUsdToZarRate);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Currency API Failed (Using Cache): " + e.getMessage());
        }
        return cachedUsdToZarRate;
    }

    /**
     * Converts ZAR Amount to USD.
     * Example: R5000 @ 18.50 rate = $270.27
     */
    public BigDecimal convertZarToUsd(BigDecimal zarAmount) {
        BigDecimal rate = getUsdToZarRate();
        // Divide ZAR by Rate (e.g., 5000 / 18.50), round to 2 decimals
        return zarAmount.divide(rate, 2, RoundingMode.HALF_UP);
    }
}