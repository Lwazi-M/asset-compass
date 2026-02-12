"use client";

import AddAssetModal from "./AddAssetModal";
import { useEffect, useState, useCallback } from "react";
import { useRouter } from "next/navigation";
import axios from "axios";
import { LogOut, Shield, Plus, Wallet, TrendingUp, DollarSign, Trash2 } from "lucide-react";

// Types
interface UserData {
  id: number;
  email: string;
  fullName: string;
  role: string;
}

interface Asset {
  id: number;
  name: string;
  type: string;
  value: number;
  currency: string;
  lastUpdated: string;
}

export default function Dashboard() {
  const router = useRouter();
  const [user, setUser] = useState<UserData | null>(null);
  const [assets, setAssets] = useState<Asset[]>([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);

  // Define fetchAssets outside useEffect so we can call it when an asset is added or deleted
  const fetchAssets = useCallback(async () => {
    const token = localStorage.getItem("token");
    if (!token) return;

    try {
      const assetRes = await axios.get("/api/assets", {
        headers: { Authorization: `Bearer ${token}` },
      });
      setAssets(assetRes.data);
    } catch (err) {
      console.error("Failed to refresh assets");
    }
  }, []);

  useEffect(() => {
    const token = localStorage.getItem("token");

    if (!token) {
      router.push("/");
      return;
    }

    const init = async () => {
      try {
        // 1. Get User Data
        const userRes = await axios.get("/api/me", {
          headers: { Authorization: `Bearer ${token}` },
        });
        setUser(userRes.data);

        // 2. Get Assets
        await fetchAssets();

        setLoading(false);
      } catch (err) {
        localStorage.removeItem("token");
        router.push("/");
      }
    };

    init();
  }, [router, fetchAssets]);

  const handleLogout = () => {
    localStorage.removeItem("token");
    router.push("/");
  };

  // NEW: Delete Handler
  const handleDelete = async (id: number) => {
    if (!confirm("Are you sure you want to delete this asset?")) return;

    try {
      const token = localStorage.getItem("token");
      await axios.delete(`/api/assets/${id}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      // Refresh list immediately after delete
      fetchAssets();
    } catch (err) {
      alert("Failed to delete asset. You might not be authorized.");
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-950 flex items-center justify-center text-white">
        <div className="animate-pulse flex flex-col items-center">
          <Shield className="h-12 w-12 text-blue-600 mb-4" />
          <p className="text-gray-400">Loading your portfolio...</p>
        </div>
      </div>
    );
  }

  // Calculate Total Net Worth
  const totalValue = assets.reduce((sum, asset) => sum + asset.value, 0);

  return (
    <div className="min-h-screen bg-gray-950 text-white p-6 md:p-10 relative">

      {/* Navbar */}
      <nav className="flex justify-between items-center mb-10 border-b border-gray-800 pb-6">
        <div className="flex items-center gap-3">
            <div className="bg-blue-600/20 p-2 rounded-lg">
                <Shield className="h-6 w-6 text-blue-500" />
            </div>
            <h1 className="text-2xl font-bold tracking-tight">AssetCompass</h1>
        </div>
        <div className="flex items-center gap-4">
            <div className="text-right hidden md:block">
                <p className="text-sm font-medium">{user?.fullName}</p>
                <p className="text-xs text-gray-400">{user?.role}</p>
            </div>
            <button
            onClick={handleLogout}
            className="flex items-center gap-2 bg-red-500/10 hover:bg-red-500/20 text-red-400 px-4 py-2 rounded-lg transition-all text-sm font-medium"
            >
            <LogOut className="h-4 w-4" />
            </button>
        </div>
      </nav>

      {/* Overview Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-10">
        <div className="bg-gradient-to-br from-blue-900/50 to-gray-900 border border-blue-500/30 p-6 rounded-2xl">
            <div className="flex items-center gap-3 mb-2">
                <Wallet className="h-5 w-5 text-blue-400" />
                <h3 className="text-sm font-medium text-gray-300">Net Worth</h3>
            </div>
            <p className="text-3xl font-bold text-white">${totalValue.toLocaleString()}</p>
        </div>

        <div className="bg-gray-900 border border-gray-800 p-6 rounded-2xl">
             <div className="flex items-center gap-3 mb-2">
                <TrendingUp className="h-5 w-5 text-green-400" />
                <h3 className="text-sm font-medium text-gray-300">Total Assets</h3>
            </div>
            <p className="text-3xl font-bold text-white">{assets.length}</p>
        </div>

        <button
            onClick={() => setIsModalOpen(true)}
            className="group bg-gray-900 border border-dashed border-gray-700 hover:border-blue-500 hover:bg-gray-800 p-6 rounded-2xl flex flex-col items-center justify-center transition-all cursor-pointer"
        >
            <div className="bg-blue-500/20 p-3 rounded-full mb-3 group-hover:bg-blue-500 group-hover:text-white transition-colors text-blue-400">
                <Plus className="h-6 w-6" />
            </div>
            <span className="text-sm font-medium text-gray-400 group-hover:text-white">Add New Asset</span>
        </button>
      </div>

      {/* Asset List */}
      <div className="bg-gray-900 border border-gray-800 rounded-2xl overflow-hidden shadow-xl">
        <div className="p-6 border-b border-gray-800 flex justify-between items-center">
            <h2 className="text-xl font-bold">Your Portfolio</h2>
        </div>

        {assets.length === 0 ? (
            <div className="p-12 text-center text-gray-500">
                You have no assets yet. Click "Add New Asset" to start tracking.
            </div>
        ) : (
            <div className="divide-y divide-gray-800">
                {assets.map((asset) => (
                    <div key={asset.id} className="p-6 flex justify-between items-center hover:bg-gray-800/50 transition-colors group">
                        <div className="flex items-center gap-4">
                            <div className="h-10 w-10 bg-gray-800 rounded-full flex items-center justify-center text-gray-400">
                                <DollarSign className="h-5 w-5" />
                            </div>
                            <div>
                                <p className="font-bold text-white">{asset.name}</p>
                                <p className="text-xs text-gray-500 uppercase tracking-wide">{asset.type}</p>
                            </div>
                        </div>

                        <div className="flex items-center gap-6">
                            <div className="text-right">
                                <p className="font-bold text-white text-lg">${asset.value.toLocaleString()}</p>
                                <p className="text-xs text-gray-500">{asset.currency}</p>
                            </div>

                            {/* Delete Button (Only visible on hover) */}
                            <button
                                onClick={() => handleDelete(asset.id)}
                                className="text-gray-600 hover:text-red-500 transition-colors opacity-0 group-hover:opacity-100"
                                title="Delete Asset"
                            >
                                <Trash2 className="h-5 w-5" />
                            </button>
                        </div>
                    </div>
                ))}
            </div>
        )}
      </div>

      <AddAssetModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onAssetAdded={fetchAssets}
      />

    </div>
  );
}