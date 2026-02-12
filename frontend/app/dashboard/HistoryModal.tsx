"use client";

import { useState, useEffect } from "react";
import axios from "axios";
import { X, History, TrendingUp, TrendingDown, Clock } from "lucide-react";

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
}

export default function HistoryModal({ isOpen, onClose, assetId, assetName }: HistoryModalProps) {
  const [history, setHistory] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen && assetId) {
      const fetchHistory = async () => {
        setLoading(true);
        try {
          const token = localStorage.getItem("token");
          const res = await axios.get(`/api/assets/${assetId}/history`, {
            headers: { Authorization: `Bearer ${token}` },
          });
          setHistory(res.data);
        } catch (err) {
          console.error("Failed to fetch history");
        } finally {
          setLoading(false);
        }
      };
      fetchHistory();
    }
  }, [isOpen, assetId]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4 animate-in fade-in duration-200">
      <div className="w-full max-w-lg bg-gray-900 border border-gray-800 rounded-2xl shadow-2xl overflow-hidden">

        {/* Header */}
        <div className="p-6 border-b border-gray-800 flex justify-between items-center bg-gray-900/50">
          <div className="flex items-center gap-3">
            <History className="h-5 w-5 text-blue-500" />
            <h2 className="text-xl font-bold text-white">{assetName} History</h2>
          </div>
          <button onClick={onClose} className="text-gray-500 hover:text-white transition-colors">
            <X className="h-6 w-6" />
          </button>
        </div>

        {/* Content */}
        <div className="max-h-[60vh] overflow-y-auto p-4 custom-scrollbar">
          {loading ? (
            <div className="py-20 text-center text-gray-500 animate-pulse">Loading history...</div>
          ) : history.length === 0 ? (
            <div className="py-20 text-center text-gray-500">No transaction logs found.</div>
          ) : (
            <div className="space-y-3">
              {history.map((log, index) => {
                const prevValue = history[index + 1]?.valueAtTime;
                const isHigher = prevValue ? log.valueAtTime > prevValue : null;

                return (
                  <div key={log.id} className="bg-gray-950 border border-gray-800 p-4 rounded-xl flex justify-between items-center">
                    <div className="flex items-center gap-4">
                      <div className={`p-2 rounded-lg ${
                        log.type === 'PRICE_REFRESH' ? 'bg-blue-500/10 text-blue-400' : 'bg-green-500/10 text-green-400'
                      }`}>
                        <Clock className="h-4 w-4" />
                      </div>
                      <div>
                        <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider">
                          {log.type.replace('_', ' ')}
                        </p>
                        <p className="text-sm text-gray-300">
                          {new Date(log.timestamp).toLocaleString()}
                        </p>
                      </div>
                    </div>

                    <div className="text-right flex items-center gap-3">
                      <div>
                        <p className="font-mono font-bold text-white text-lg">
                          ${log.valueAtTime.toLocaleString(undefined, { minimumFractionDigits: 2 })}
                        </p>
                        {isHigher !== null && (
                          <span className={`text-[10px] flex items-center justify-end gap-1 ${isHigher ? 'text-green-400' : 'text-red-400'}`}>
                            {isHigher ? <TrendingUp className="h-3 w-3" /> : <TrendingDown className="h-3 w-3" />}
                            {isHigher ? 'Increased' : 'Decreased'}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="p-4 bg-gray-950/50 border-t border-gray-800 text-center text-[10px] text-gray-600 italic">
          Transactions are logged automatically during price refreshes and manual updates.
        </div>
      </div>
    </div>
  );
}