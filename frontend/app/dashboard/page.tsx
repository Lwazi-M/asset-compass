'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { LogOut, Plus, Wallet, TrendingUp, RefreshCw, Trash2, Edit2, PieChart } from 'lucide-react';
import AddAssetModal from './AddAssetModal';
import HistoryModal from './HistoryModal';
import PortfolioChart from './PortfolioChart';

interface Asset {
  id: number;
  name: string;
  ticker: string;
  assetType: string;
  quantity: number;
  buyPrice: number;
  currency: string;
  lastUpdated: string;
}

interface User {
  fullName: string;
  email: string;
  role: string;
  netWorthZAR?: number;
}

export default function Dashboard() {
  const router = useRouter();
  const [user, setUser] = useState<User | null>(null);
  const [assets, setAssets] = useState<Asset[]>([]);
  const [loading, setLoading] = useState(true);

  // Track which asset is currently refreshing
  const [refreshingId, setRefreshingId] = useState<number | null>(null);
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);

  const [selectedAsset, setSelectedAsset] = useState<Asset | null>(null);
  const [isHistoryOpen, setIsHistoryOpen] = useState(false);
  const [editingAsset, setEditingAsset] = useState<Asset | null>(null);

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    const token = localStorage.getItem('token');
    if (!token) {
      router.push('/login');
      return;
    }

    try {
      const userRes = await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'https://asset-compass-production.up.railway.app'}/api/me`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (!userRes.ok) throw new Error('Failed to fetch user');
      const userData = await userRes.json();
      setUser(userData);

      const assetRes = await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'https://asset-compass-production.up.railway.app'}/api/assets`, {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (assetRes.ok) {
        const assetData = await assetRes.json();
        setAssets(assetData);
      }
    } catch (err) {
      console.error(err);
      localStorage.removeItem('token');
      router.push('/login');
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('token');
    router.push('/login');
  };

  const handleDelete = async (id: number) => {
    if(!confirm("Are you sure you want to delete this asset?")) return;

    const token = localStorage.getItem('token');
    try {
        await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'https://asset-compass-production.up.railway.app'}/api/assets/${id}`, {
            method: 'DELETE',
            headers: { Authorization: `Bearer ${token}` }
        });
        fetchData(); // Refresh list and net worth
    } catch (err) {
        console.error("Failed to delete", err);
    }
  };

  // --- FIX: Add Refresh Logic ---
  const handleRefreshPrice = async (id: number) => {
    setRefreshingId(id);
    const token = localStorage.getItem('token');
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'https://asset-compass-production.up.railway.app'}/api/assets/${id}/refresh`, {
        method: 'PUT',
        headers: { Authorization: `Bearer ${token}` }
      });
      if (res.ok) {
        fetchData(); // Refreshes UI to show new net worth and asset price
      } else {
        alert("Failed to refresh live price from market.");
      }
    } catch (err) {
      console.error("Failed to refresh", err);
    } finally {
      setRefreshingId(null);
    }
  };

  const openHistory = (asset: Asset) => {
    setSelectedAsset(asset);
    setIsHistoryOpen(true);
  };

  const handleEdit = (asset: Asset) => {
    setEditingAsset(asset);
    setIsAddModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsAddModalOpen(false);
    setEditingAsset(null);
  };

  if (loading) return <div className="flex h-screen items-center justify-center bg-slate-950 text-white">Loading WealthOS...</div>;

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 font-sans selection:bg-blue-500/30">

      {/* Navbar */}
      <nav className="border-b border-slate-800 bg-slate-900/50 backdrop-blur-md sticky top-0 z-30">
        <div className="max-w-7xl mx-auto px-6 h-20 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="bg-blue-600 p-2 rounded-xl shadow-lg shadow-blue-900/20">
              <TrendingUp className="text-white h-6 w-6" />
            </div>
            <span className="text-2xl font-bold bg-gradient-to-r from-white to-slate-400 bg-clip-text text-transparent">
              AssetCompass
            </span>
          </div>
          <div className="flex items-center gap-6">
            <div className="text-right hidden sm:block">
              <p className="text-sm text-slate-400">Welcome back,</p>
              <p className="text-sm font-semibold text-white">{user?.fullName}</p>
            </div>
            <button
              onClick={handleLogout}
              className="flex items-center gap-2 bg-slate-800 hover:bg-red-500/10 hover:text-red-400 text-slate-300 px-4 py-2 rounded-lg transition-all duration-300 border border-slate-700 hover:border-red-500/50"
            >
              <LogOut size={18} />
              <span className="hidden sm:inline">Sign Out</span>
            </button>
          </div>
        </div>
      </nav>

      <main className="max-w-7xl mx-auto px-6 py-8 space-y-8">

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">

          {/* Net Worth Card */}
          <div className="bg-slate-900/80 p-6 rounded-2xl border border-slate-800 shadow-xl relative overflow-hidden group">
            <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
              <Wallet size={80} />
            </div>
            <div className="flex items-center gap-3 mb-2 text-blue-400">
              <Wallet size={20} />
              <h3 className="font-medium">Net Worth (ZAR)</h3>
            </div>
            <p className="text-4xl font-bold text-white tracking-tight">
              R {user?.netWorthZAR?.toLocaleString('en-ZA', { maximumFractionDigits: 2 }) || '0.00'}
            </p>
          </div>

          {/* Asset Count Card */}
          <div className="bg-slate-900/80 p-6 rounded-2xl border border-slate-800 shadow-xl relative overflow-hidden group">
            <div className="absolute top-0 right-0 p-4 opacity-10 group-hover:opacity-20 transition-opacity">
              <TrendingUp size={80} className="text-emerald-500" />
            </div>
            <div className="flex items-center gap-3 mb-2 text-emerald-400">
              <TrendingUp size={20} />
              <h3 className="font-medium">Total Assets</h3>
            </div>
            <p className="text-4xl font-bold text-white tracking-tight">{assets.length}</p>
          </div>

          {/* Add Asset Button Card */}
          <button
            onClick={() => setIsAddModalOpen(true)}
            className="bg-slate-900/80 p-6 rounded-2xl border border-slate-800 border-dashed hover:border-blue-500 hover:bg-blue-500/5 transition-all duration-300 flex flex-col items-center justify-center gap-3 group cursor-pointer h-full"
          >
            <div className="bg-blue-600 rounded-full p-3 group-hover:scale-110 transition-transform shadow-lg shadow-blue-900/30">
              <Plus className="text-white" size={24} />
            </div>
            <span className="font-semibold text-slate-300 group-hover:text-white">Add New Asset</span>
          </button>
        </div>

        {/* Charts & Table Section */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">

          {/* Chart Area */}
          <div className="lg:col-span-1 bg-slate-900/80 p-6 rounded-2xl border border-slate-800 shadow-xl">
             <div className="flex items-center gap-3 mb-6">
                <PieChart className="text-purple-400" size={20} />
                <h3 className="font-semibold text-white">Allocation</h3>
             </div>
             <PortfolioChart assets={assets.map(a => ({
                name: a.ticker,
                value: a.quantity * a.buyPrice
             }))} />
          </div>

          {/* Assets List */}
          <div className="lg:col-span-2 bg-slate-900/80 rounded-2xl border border-slate-800 shadow-xl overflow-hidden flex flex-col">
            <div className="p-6 border-b border-slate-800 bg-slate-900/50 backdrop-blur-sm">
              <h3 className="font-bold text-lg text-white">Your Portfolio</h3>
            </div>

            <div className="overflow-y-auto max-h-[400px]">
              {assets.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-16 text-slate-500">
                  <div className="bg-slate-800/50 p-4 rounded-full mb-4">
                    <TrendingUp size={32} />
                  </div>
                  <p>No assets found. Start building your wealth!</p>
                </div>
              ) : (
                <div className="divide-y divide-slate-800">
                  {assets.map((asset) => (
                    <div key={asset.id} className="p-4 hover:bg-slate-800/50 transition-colors flex items-center justify-between group">

                      {/* Left: Ticker & Name */}
                      <div className="flex items-center gap-4 cursor-pointer" onClick={() => openHistory(asset)}>
                        <div className="bg-slate-800 p-3 rounded-xl border border-slate-700 text-slate-300 font-bold w-12 h-12 flex items-center justify-center">
                          {asset.ticker ? asset.ticker.substring(0, 2) : 'AS'}
                        </div>
                        <div>
                          <h4 className="font-bold text-white">{asset.name}</h4>
                          <div className="flex items-center gap-2 text-sm text-slate-400">
                            <span className="bg-slate-800 px-1.5 rounded text-xs border border-slate-700">{asset.assetType}</span>
                            <span>{asset.quantity.toFixed(4)} shares</span>
                          </div>
                        </div>
                      </div>

                      {/* Right: Value & Actions */}
                      <div className="flex items-center gap-6">
                        <div className="text-right">
                          <p className="font-bold text-white font-mono text-lg">
                            ${(asset.quantity * asset.buyPrice).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                          </p>
                          <p className="text-xs text-slate-500">
                            Current @ ${asset.buyPrice}
                          </p>
                        </div>

                        <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                          {/* FIX: Connect Refresh Logic */}
                          <button
                            onClick={() => handleRefreshPrice(asset.id)}
                            disabled={refreshingId === asset.id}
                            className={`p-2 rounded-lg transition ${refreshingId === asset.id ? 'text-blue-500' : 'text-slate-400 hover:text-blue-400 hover:bg-slate-700'}`}
                            title="Refresh Price"
                          >
                            <RefreshCw size={16} className={refreshingId === asset.id ? "animate-spin" : ""} />
                          </button>

                          <button onClick={() => handleDelete(asset.id)} className="p-2 hover:bg-slate-700 rounded-lg text-slate-400 hover:text-red-400 transition" title="Delete">
                            <Trash2 size={16} />
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>

      </main>

      <AddAssetModal
         isOpen={isAddModalOpen}
         onClose={handleCloseModal}
         onAssetAdded={fetchData}
         initialData={editingAsset}
      />

      {selectedAsset && (
        <HistoryModal
          isOpen={isHistoryOpen}
          onClose={() => setIsHistoryOpen(false)}
          asset={selectedAsset}
        />
      )}

    </div>
  );
}