package com.assetcompass.tracker.controllers;

import com.assetcompass.tracker.services.CurrencyService;
import com.assetcompass.tracker.services.StockService;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/market")
public class StockController {

    private final StockService stockService;
    private final CurrencyService currencyService;

    public StockController(StockService stockService, CurrencyService currencyService) {
        this.stockService = stockService;
        this.currencyService = currencyService;
    }

    // 1. Get Live USD/ZAR Rate
    @GetMapping("/rate")
    public Map<String, BigDecimal> getExchangeRate() {
        return Map.of("rate", currencyService.getUsdToZarRate());
    }

    // 2. Search for Real Stocks
    @GetMapping("/search")
    public String search(@RequestParam String query) {
        return stockService.searchStocks(query);
    }
}