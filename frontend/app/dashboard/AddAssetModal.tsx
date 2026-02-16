'use client';

import { useState, useEffect } from 'react';
import { X, Search, Calculator, Loader2 } from 'lucide-react';

interface AddAssetModalProps {
  isOpen: boolean;
  onClose: () => void;
  onAssetAdded: () => void;
}

interface Stock {
  symbol: string;
  name: string;
  type: string;
  price: number;
}

export default function AddAssetModal({ isOpen, onClose, onAssetAdded }: AddAssetModalProps) {
  const [step, setStep] = useState(1); // 1: Search, 2: Calculator
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<Stock[]>([]);
  const [selectedStock, setSelectedStock] = useState<Stock | null>(null);
  const [loading, setLoading] = useState(false);

  // Calculator State
  const [investedAmount, setInvestedAmount] = useState('');
  const [currency, setCurrency] = useState('ZAR'); // Default to Rands
  const [calculatedShares, setCalculatedShares] = useState<string | null>(null);
  const [convertedUsd, setConvertedUsd] = useState<number | null>(null);
  const [usdRate, setUsdRate] = useState(18.50); // Fallback rate

  // 1. Search for Stocks (Simulated for now)
  const handleSearch = async () => {
    setLoading(true);
    // Simulate API search delay
    setTimeout(() => {
      setSearchResults([
        { symbol: 'AAPL', name: 'Apple Inc.', type: 'STOCK', price: 255.00 },
        { symbol: 'TSLA', name: 'Tesla Inc.', type: 'STOCK', price: 210.00 },
        { symbol: 'VOO', name: 'Vanguard S&P 500 ETF', type: 'ETF', price: 480.00 },
        { symbol: 'BTC', name: 'Bitcoin', type: 'CRYPTO', price: 52000.00 },
      ]);
      setLoading(false);
    }, 800);
  };

  // 2. Select a Stock
  const selectStock = (stock: Stock) => {
    setSelectedStock(stock);
    setStep(2); // Move to Calculator Step
  };

  // 3. The "WealthOS" Calculation Logic
  useEffect(() => {
    if (!investedAmount || !selectedStock) return;

    const amount = parseFloat(investedAmount);
    if (isNaN(amount)) return;

    let usdAmount = amount;

    // Convert ZAR to USD
    if (currency === 'ZAR') {
      usdAmount = amount / usdRate;
    }

    setConvertedUsd(usdAmount);

    // Calculate Shares: Invested USD / Stock Price
    const shares = usdAmount / selectedStock.price;
    setCalculatedShares(shares.toFixed(4)); // Show 4 decimal places
  }, [investedAmount, currency, selectedStock, usdRate]);

  // 4. Submit to Backend
  const handleBuy = async () => {
    if (!selectedStock) return;

    setLoading(true);
    const token = localStorage.getItem('token');

    try {
      const response = await fetch('https://asset-compass-production.up.railway.app/api/assets/buy', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({
          ticker: selectedStock.symbol,
          name: selectedStock.name,
          assetType: selectedStock.type,
          amount: parseFloat(investedAmount),
          currency: currency
        }),
      });

      if (response.ok) {
        onAssetAdded(); // Refresh dashboard
        onClose();      // Close modal
        // Reset state for next time
        setStep(1);
        setInvestedAmount('');
        setSearchResults([]);
        setSearchQuery('');
      } else {
        alert("Failed to buy asset. Check console.");
      }
    } catch (error) {
      console.error("Buy error:", error);
    } finally {
      setLoading(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/80 flex items-center justify-center p-4 z-50 backdrop-blur-sm">
      <div className="bg-slate-900 border border-slate-700 w-full max-w-md rounded-2xl shadow-2xl overflow-hidden">

        {/* Header */}
        <div className="flex justify-between items-center p-6 border-b border-slate-800">
          <h2 className="text-xl font-bold text-white">
            {step === 1 ? 'Find Investment' : 'Buy ' + selectedStock?.symbol}
          </h2>
          <button onClick={onClose} className="text-slate-400 hover:text-white transition">
            <X size={24} />
          </button>
        </div>

        {/* STEP 1: SEARCH */}
        {step === 1 && (
          <div className="p-6 space-y-4">
            <div className="relative">
              <Search className="absolute left-3 top-3 text-slate-500" size={20} />
              <input
                type="text"
                placeholder="Search ticker (e.g. AAPL, TSLA)..."
                className="w-full bg-slate-800 text-white pl-10 pr-4 py-3 rounded-xl border border-slate-700 focus:border-blue-500 focus:outline-none transition"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              />
            </div>

            <button
                onClick={handleSearch}
                className="w-full bg-blue-600 hover:bg-blue-500 text-white font-semibold py-3 rounded-xl transition flex justify-center"
            >
                {loading ? <Loader2 className="animate-spin" /> : 'Search Market'}
            </button>

            <div className="space-y-2 mt-4">
              {searchResults.map((stock) => (
                <div
                    key={stock.symbol}
                    onClick={() => selectStock(stock)}
                    className="flex justify-between items-center p-4 bg-slate-800/50 hover:bg-slate-800 rounded-xl cursor-pointer border border-transparent hover:border-blue-500/30 transition group"
                >
                  <div>
                    <h3 className="font-bold text-white">{stock.symbol}</h3>
                    <p className="text-xs text-slate-400">{stock.name}</p>
                  </div>
                  <div className="text-right">
                    <p className="text-emerald-400 font-mono">${stock.price}</p>
                    <span className="text-[10px] bg-slate-700 text-slate-300 px-2 py-0.5 rounded uppercase">{stock.type}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* STEP 2: CALCULATOR */}
        {step === 2 && selectedStock && (
          <div className="p-6 space-y-6">

            {/* Stock Info Card */}
            <div className="bg-slate-800 p-4 rounded-xl flex justify-between items-center border border-slate-700">
                <div>
                    <h1 className="text-2xl font-bold text-white">{selectedStock.symbol}</h1>
                    <p className="text-slate-400 text-sm">Current Price</p>
                </div>
                <div className="text-right">
                    <p className="text-2xl font-mono text-emerald-400">${selectedStock.price}</p>
                    <p className="text-xs text-slate-500">USD Market</p>
                </div>
            </div>

            {/* Input Section */}
            <div className="space-y-2">
                <label className="text-sm font-medium text-slate-300">I want to invest:</label>
                <div className="flex gap-2">
                    <select
                        className="bg-slate-800 text-white px-3 py-3 rounded-xl border border-slate-700 focus:outline-none"
                        value={currency}
                        onChange={(e) => setCurrency(e.target.value)}
                    >
                        <option value="ZAR">ZAR (R)</option>
                        <option value="USD">USD ($)</option>
                    </select>
                    <input
                        type="number"
                        placeholder="Amount (e.g. 5000)"
                        className="flex-1 bg-slate-800 text-white px-4 py-3 rounded-xl border border-slate-700 focus:border-blue-500 focus:outline-none font-mono text-lg"
                        value={investedAmount}
                        onChange={(e) => setInvestedAmount(e.target.value)}
                    />
                </div>
            </div>

            {/* The "WealthOS" Calculation Display */}
            <div className="bg-blue-900/20 border border-blue-500/30 p-4 rounded-xl space-y-3">
                <div className="flex justify-between text-sm">
                    <span className="text-slate-400">Exchange Rate:</span>
                    <span className="text-slate-200 font-mono">1 USD ≈ {usdRate} ZAR</span>
                </div>
                {convertedUsd !== null && (
                    <div className="flex justify-between text-sm">
                        <span className="text-slate-400">USD Value:</span>
                        <span className="text-white font-mono">${convertedUsd.toFixed(2)}</span>
                    </div>
                )}
                <div className="border-t border-blue-500/30 pt-3 flex justify-between items-center">
                    <span className="text-blue-200 font-medium">Est. Shares Owned:</span>
                    <span className="text-2xl font-bold text-blue-400 font-mono">
                        {calculatedShares || '0.00'}
                    </span>
                </div>
            </div>

            <button
                onClick={handleBuy}
                disabled={!investedAmount || loading}
                className="w-full bg-emerald-600 hover:bg-emerald-500 disabled:bg-slate-700 disabled:cursor-not-allowed text-white font-bold py-4 rounded-xl transition flex justify-center items-center gap-2"
            >
                {loading ? <Loader2 className="animate-spin" /> : (
                    <>
                        <Calculator size={20} />
                        Confirm Investment
                    </>
                )}
            </button>
          </div>
        )}

      </div>
    </div>
  );
}