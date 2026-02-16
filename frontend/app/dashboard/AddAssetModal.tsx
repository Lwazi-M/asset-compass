'use client';

import { useState, useEffect } from 'react';
import { X, Search, Calculator, Loader2 } from 'lucide-react';

// FIX: Added 'initialData' to the interface to prevent build errors
interface AddAssetModalProps {
  isOpen: boolean;
  onClose: () => void;
  onAssetAdded: () => void;
  initialData?: any; // <--- This fixes the error!
}

interface Stock {
  symbol: string;
  name: string;
  type: string;
  price: number;
}

export default function AddAssetModal({ isOpen, onClose, onAssetAdded, initialData }: AddAssetModalProps) {
  const [step, setStep] = useState(1); // 1: Search, 2: Calculator
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<Stock[]>([]);
  const [selectedStock, setSelectedStock] = useState<Stock | null>(null);
  const [loading, setLoading] = useState(false);

  // Calculator State
  const [investedAmount, setInvestedAmount] = useState('');
  const [currency, setCurrency] = useState('ZAR');
  const [calculatedShares, setCalculatedShares] = useState<string | null>(null);
  const [convertedUsd, setConvertedUsd] = useState<number | null>(null);
  const [usdRate, setUsdRate] = useState(18.50);

  // Reset state when modal opens
  useEffect(() => {
    if (isOpen) {
      setStep(1);
      setInvestedAmount('');
      setSearchResults([]);
      setSearchQuery('');
      setSelectedStock(null);
    }
  }, [isOpen]);

  // 1. Search for Stocks (Simulated)
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
    setCalculatedShares(shares.toFixed(4));
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

            <div className="space-y-2">
                <label className="text-sm font-medium text-slate-300">I want to invest:</label>
                <div className="flex gap-2">
                    <select
                        className="bg-slate-800 text-white px-3 py-3 rounded-xl border border-slate-700 focus:outline-none"
                        value={currency}