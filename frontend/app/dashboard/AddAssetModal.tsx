"use client";

import { useState, useEffect } from "react";
import axios from "axios";
import { X, CheckCircle, Loader2 } from "lucide-react";

// Define Asset Type locally to avoid circular dependencies
interface Asset {
  id: number;
  name: string;
  type: string;
  value: number;
  currency: string;
  lastUpdated: string;
}

interface AddAssetModalProps {
  isOpen: boolean;
  onClose: () => void;
  onAssetAdded: () => void;
  initialData?: Asset | null; // <--- New Prop: Data to Edit
}

export default function AddAssetModal({ isOpen, onClose, onAssetAdded, initialData }: AddAssetModalProps) {
  const [name, setName] = useState("");
  const [type, setType] = useState("STOCK");
  const [value, setValue] = useState("");
  const [currency, setCurrency] = useState("USD");
  const [loading, setLoading] = useState(false);

  // POPULATE FORM WHEN EDITING
  useEffect(() => {
    if (initialData) {
      setName(initialData.name);
      setType(initialData.type);
      setValue(initialData.value.toString());
      setCurrency(initialData.currency);
    } else {
      // Clear form if adding new
      setName("");
      setType("STOCK");
      setValue("");
      setCurrency("USD");
    }
  }, [initialData, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const token = localStorage.getItem("token");
      const payload = {
        name,
        type,
        value: parseFloat(value),
        currency,
      };

      if (initialData) {
        // EDIT MODE (PUT)
        await axios.put(`/api/assets/${initialData.id}`, payload, {
          headers: { Authorization: `Bearer ${token}` },
        });
      } else {
        // CREATE MODE (POST)
        await axios.post("/api/assets", payload, {
          headers: { Authorization: `Bearer ${token}` },
        });
      }

      onAssetAdded(); // Refresh Dashboard
      onClose();
    } catch (err) {
      alert("Failed to save asset. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4 animate-in fade-in duration-200">
      <div className="w-full max-w-md bg-gray-900 border border-gray-800 rounded-2xl shadow-2xl p-6 relative">

        <button onClick={onClose} className="absolute top-4 right-4 text-gray-500 hover:text-white">
          <X className="h-5 w-5" />
        </button>

        <h2 className="text-xl font-bold mb-6 text-white">
          {initialData ? "Edit Asset" : "Add New Asset"}
        </h2>

        <form onSubmit={handleSubmit} className="space-y-4">

          <div>
            <label className="block text-sm font-medium text-gray-400 mb-1">Asset Name</label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Apple Stock"
              className="w-full bg-gray-950 border border-gray-800 rounded-lg py-2.5 px-4 focus:ring-2 focus:ring-blue-500 outline-none text-white"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-400 mb-1">Type</label>
              <select
                value={type}
                onChange={(e) => setType(e.target.value)}
                className="w-full bg-gray-950 border border-gray-800 rounded-lg py-2.5 px-4 focus:ring-2 focus:ring-blue-500 outline-none text-white appearance-none"
              >
                <option value="STOCK">Stock</option>
                <option value="CRYPTO">Crypto</option>
                <option value="REAL_ESTATE">Real Estate</option>
                <option value="CASH">Cash</option>
                <option value="VEHICLE">Vehicle</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-400 mb-1">Currency</label>
              <select
                value={currency}
                onChange={(e) => setCurrency(e.target.value)}
                className="w-full bg-gray-950 border border-gray-800 rounded-lg py-2.5 px-4 focus:ring-2 focus:ring-blue-500 outline-none text-white appearance-none"
              >
                <option value="USD">USD ($)</option>
                <option value="EUR">EUR (€)</option>
                <option value="ZAR">ZAR (R)</option>
                <option value="GBP">GBP (£)</option>
              </select>
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-400 mb-1">Current Value</label>
            <div className="relative">
              <span className="absolute left-4 top-2.5 text-gray-500">$</span>
              <input
                type="number"
                required
                step="0.01"
                value={value}
                onChange={(e) => setValue(e.target.value)}
                placeholder="0.00"
                className="w-full bg-gray-950 border border-gray-800 rounded-lg py-2.5 pl-8 pr-4 focus:ring-2 focus:ring-blue-500 outline-none text-white font-mono"
              />
            </div>
          </div>

          <button
            disabled={loading}
            className="w-full bg-blue-600 hover:bg-blue-500 text-white font-semibold py-3 rounded-lg transition-all flex items-center justify-center gap-2 mt-4"
          >
            {loading ? <Loader2 className="h-5 w-5 animate-spin" /> : <>Save Asset <CheckCircle className="h-4 w-4" /></>}
          </button>
        </form>
      </div>
    </div>
  );
}