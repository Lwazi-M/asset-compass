'use client';

import { useState, useEffect } from 'react';
import { X, Search, Calculator, Loader2 } from 'lucide-react';

interface AddAssetModalProps {
  isOpen: boolean;
  onClose: () => void;
  onAssetAdded: () => void;
  initialData?: any;
}

interface Stock {
  symbol: string;
  name: string;
  type: string;
  price: number;
}

export default function AddAssetModal({ isOpen, onClose, onAssetAdded, initialData }: AddAssetModalProps) {
  const [step, setStep] = useState(1);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<Stock[]>([]);
  const [selectedStock, setSelectedStock] = useState<Stock | null>(null);
  const [loading, setLoading] = useState(false);

  // Calculator State
  const [investedAmount, setInvestedAmount] = useState('');
  const [currency, setCurrency] = useState('ZAR');
  const [calculatedShares, setCalculatedShares] = useState<string | null>(null);
  const [convertedUsd, setConvertedUsd] = useState<number | null>(null);
  const [usdRate, setUsdRate] = useState(18.50); // Will update from API

  // 1. Fetch Real Exchange Rate on Open
  useEffect(() => {
    if (isOpen) {
        const fetchRate = async () => {
            try {
                const token = localStorage.getItem('token');
                const res = await fetch('https://asset-compass-production.up.railway.app/api/market/rate', {
                    headers: { Authorization: `Bearer ${token}` }
                });
                const data = await res.json();
                if (data.rate) setUsdRate(data.rate);
            } catch (e) {
                console.error("Failed to fetch rate", e);
            }
        };
        fetchRate();

        // Reset UI
        setStep(1);
        setInvestedAmount('');
        setSearchResults([]);
        setSearchQuery('');
        setSelectedStock(null);
    }
  }, [isOpen]);

  // 2. REAL SEARCH (AlphaVantage)
  const handleSearch = async () => {
    if (!searchQuery) return;
    setLoading(true);
    const token = localStorage.getItem('token');

    try {
        // Call our new Backend Search Endpoint
        const res = await fetch(`https://asset-compass-production.up.railway.app/api/market/search?query=${searchQuery}`, {
            headers: { Authorization: `Bearer ${token}` }
        });
        const data = await res.json();

        // Transform AlphaVantage data to our format
        const matches = data.bestMatches || [];
        const stocks = matches.map((item: any) => ({
            symbol: item['1. symbol'],
            name: item['2. name'],
            type: item['3. type'],
            price: 0 // We fetch the real price in Step 2
        }));
        setSearchResults(stocks);
    } catch (err) {
        console.error("Search failed", err);
    } finally {
        setLoading(false);
    }
  };

  // 3. Select Stock & Fetch Live Price
  const selectStock = async (stock: Stock) => {
    setLoading(true);
    // We need to fetch the price NOW because search results don't have price
    // But our /buy endpoint does the check anyway.
    // For UI, let's just move to step 2 and assume backend will check price.
    // OPTIONAL: You could fetch price here to show it, but to save API calls,
    // let's just show "Market Price" text for now.

    // Actually, let's fetch it for the calculator:
    // We will do a quick "dry run" buy or just use the buy endpoint logic?
    // Let's use a simple price check if you want accurate calculator.
    // For now, to keep it simple and save API calls (5/min limit on free tier),
    // we will proceed and let the backend handle the final price.
    // BUT the calculator needs a price.

    // Let's assume user inputs amount, and we get estimate.
    // For this "Pro" version, let's just set a placeholder or fetch it if possible.
    // Since we have limited API calls, let's manually fetch price:

    // NOTE: This might hit rate limits if you click too fast!
    // Ideally you'd cache this. For now, we proceed:
    setSelectedStock({ ...stock, price: 0 }); // Price 0 means "Loading..."
    setStep(2);
    setLoading(false);
  };

  // 4. Calculator Logic (Updated for Real Rate)
  useEffect(() => {
    if (!investedAmount || !selectedStock) return;

    const amount = parseFloat(investedAmount);
    if (isNaN(amount)) return;

    let usdAmount = amount;
    if (currency === 'ZAR') {
      usdAmount = amount / usdRate;
    }
    setConvertedUsd(usdAmount);

    // Note: Since we don't have the live price in the frontend yet (to save API calls),
    // we can only show the USD Value. The "Shares" will be calculated by the backend.
    // If you REALLY want to see shares here, we must fetch the price.
    // Let's stick to showing USD value to be safe on limits.
  }, [investedAmount, currency, selectedStock, usdRate]);

  // 5. Submit
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
        onAssetAdded();
        onClose();
      } else {
        const err = await response.text();
        alert("Purchase failed: " + err);
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
                placeholder="Search ticker (e.g. AMD, AMZN, TTWO)..."
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

            <div className="space-y-2 mt-4 max-h-60 overflow-y-auto">
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
                    <p className="text-slate-400 text-sm">{selectedStock.name}</p>
                </div>
            </div>

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
                        placeholder="Amount"
                        className="flex-1 bg-slate-800 text-white px-4 py-3 rounded-xl border border-slate-700 focus:border-blue-500 focus:outline-none font-mono text-lg"
                        value={investedAmount}
                        onChange={(e) => setInvestedAmount(e.target.value)}
                    />
                </div>
            </div>

            <div className="bg-blue-900/20 border border-blue-500/30 p-4 rounded-xl space-y-3">
                <div className="flex justify-between text-sm">
                    <span className="text-slate-400">Live Rate:</span>
                    <span className="text-slate-200 font-mono">1 USD ≈ {usdRate} ZAR</span>
                </div>
                {convertedUsd !== null && (
                    <div className="flex justify-between text-sm">
                        <span className="text-slate-400">USD Investment:</span>
                        <span className="text-white font-mono">${convertedUsd.toFixed(2)}</span>
                    </div>
                )}
                <div className="border-t border-blue-500/30 pt-3 text-center">
                    <p className="text-xs text-blue-200 mb-1">Shares will be calculated at live market price</p>
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