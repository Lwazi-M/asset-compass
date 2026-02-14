"use client";

import { useState, useEffect, useMemo } from "react";
import axios from "axios";
import { X, History, TrendingUp, Clock, Calendar } from "lucide-react";
import {
  AreaChart,
  Area,
  Tooltip,
  ResponsiveContainer,
  XAxis,
  YAxis
} from "recharts";

interface Transaction {
  id: number;
  valueAtTime: number;
  type: string;
  timestamp: string;
}

interface HistoryModalProps {
  isOpen: boolean;
  onClose: () => void;
  assetId: number | null;
  assetName: string;
  assetCurrency?: string;
}

export default function HistoryModal({
  isOpen,
  onClose,
  assetId,
  assetName,
  assetCurrency = "USD"
}: HistoryModalProps) {
  const [history, setHistory] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen && assetId) {
      const fetchHistory = async () => {
        setLoading(true);
        try {
          const token = localStorage.getItem("token");
          const res = await axios.get(`${process.env.NEXT_PUBLIC_API_URL}/api/assets/${assetId}/history`, {
            headers: { Authorization: `Bearer ${token}` },
          });

          const sortedData = res.data.sort((a: Transaction, b: Transaction) =>
            new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime()
          );
          setHistory(sortedData);
        } catch (err) {
          console.error("Failed to fetch history");
        } finally {
          setLoading(false);
        }
      };
      fetchHistory();
    }
  }, [isOpen, assetId]);

  const chartData = useMemo(() => {
    return history.map((item) => ({
      date: new Date(item.timestamp).toLocaleDateString(undefined, { month: 'short', day: 'numeric' }),
      fullDate: new Date(item.timestamp).toLocaleString(),
      value: item.valueAtTime,
    }));
  }, [history]);

  if (!isOpen) return null;

  const startValue = history.length > 0 ? history[0].valueAtTime : 0;
  const endValue = history.length > 0 ? history[history.length - 1].valueAtTime : 0;
  const isPositive = endValue >= startValue;
  const chartColor = isPositive ? "#10B981" : "#EF4444";
  const currencySymbol = assetCurrency === "ZAR" ? "R" : "$";

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4 animate-in fade-in duration-200">
      <div className="w-full max-w-2xl bg-gray-900 border border-gray-800 rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">

        {/* Header */}
        <div className="p-6 border-b border-gray-800 flex justify-between items-center bg-gray-900/50">
          <div>
             <div className="flex items-center gap-2 mb-1">
                <History className="h-4 w-4 text-blue-500" />
                <h2 className="text-lg font-bold text-white">{assetName} Performance</h2>
             </div>
             <p className="text-xs text-gray-500">Track value changes over time</p>
          </div>
          <button onClick={onClose} className="text-gray-500 hover:text-white transition-colors bg-gray-800 hover:bg-gray-700 p-2 rounded-full">
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* CHART SECTION */}
        <div className="h-64 w-full bg-gradient-to-b from-gray-900 to-gray-950 border-b border-gray-800 p-4 relative">
            {loading ? (
                <div className="h-full flex items-center justify-center text-gray-500 animate-pulse">Loading Chart...</div>
            ) : history.length < 2 ? (
                <div className="h-full flex flex-col items-center justify-center text-gray-500 text-sm gap-2">
                    <TrendingUp className="h-8 w-8 text-gray-700" />
                    <p>Not enough data points to graph yet.</p>
                </div>
            ) : (
                <ResponsiveContainer width="100%" height="100%">
                    <AreaChart data={chartData}>
                        <defs>
                            <linearGradient id="colorValue" x1="0" y1="0" x2="0" y2="1">
                                <stop offset="5%" stopColor={chartColor} stopOpacity={0.3}/>
                                <stop offset="95%" stopColor={chartColor} stopOpacity={0}/>
                            </linearGradient>
                        </defs>
                        <Tooltip
                            contentStyle={{ backgroundColor: '#111827', borderColor: '#374151', borderRadius: '8px', color: '#fff' }}
                            itemStyle={{ color: '#fff' }}
                            // FIXED: Using 'any' type to silence strict TypeScript check
                            formatter={(value: any) => [`${currencySymbol}${Number(value).toLocaleString()}`, "Value"]}
                            labelStyle={{ color: '#9CA3AF', marginBottom: '0.5rem' }}
                        />
                        <XAxis dataKey="date" hide />
                        <YAxis domain={['auto', 'auto']} hide />
                        <Area
                            type="monotone"
                            dataKey="value"
                            stroke={chartColor}
                            strokeWidth={2}
                            fillOpacity={1}
                            fill="url(#colorValue)"
                        />
                    </AreaChart>
                </ResponsiveContainer>
            )}
        </div>

        {/* HISTORY LIST */}
        <div className="flex-1 overflow-y-auto p-4 custom-scrollbar bg-gray-950">
          <h3 className="text-xs font-bold text-gray-500 uppercase tracking-wider mb-4 px-2">Transaction Log</h3>

          {loading ? (
            <div className="text-center text-gray-500 py-4">Syncing...</div>
          ) : history.length === 0 ? (
            <div className="py-10 text-center text-gray-500">No transaction logs found.</div>
          ) : (
            <div className="space-y-3">
              {[...history].reverse().map((log) => {
                return (
                  <div key={log.id} className="bg-gray-900 border border-gray-800 p-4 rounded-xl flex justify-between items-center hover:border-gray-700 transition-colors">
                    <div className="flex items-center gap-4">
                      <div className={`p-2 rounded-lg ${
                        log.type === 'PRICE_REFRESH' ? 'bg-blue-500/10 text-blue-400' : 'bg-green-500/10 text-green-400'
                      }`}>
                        {log.type === 'PRICE_REFRESH' ? <Clock className="h-4 w-4" /> : <TrendingUp className="h-4 w-4" />}
                      </div>
                      <div>
                        <p className="text-xs font-semibold text-gray-400 uppercase tracking-wider">
                          {log.type.replace('_', ' ')}
                        </p>
                        <div className="flex items-center gap-1 text-gray-500 text-[10px] mt-0.5">
                            <Calendar className="h-3 w-3" />
                            {new Date(log.timestamp).toLocaleString()}
                        </div>
                      </div>
                    </div>

                    <div className="text-right">
                      <p className="font-mono font-bold text-white text-lg">
                        {currencySymbol}{log.valueAtTime.toLocaleString(undefined, { minimumFractionDigits: 2 })}
                      </p>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}