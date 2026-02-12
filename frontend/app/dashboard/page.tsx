"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import axios from "axios";
import { LogOut, User, Shield } from "lucide-react";

interface UserData {
  id: number;
  email: string;
  fullName: string;
  role: string;
}

export default function Dashboard() {
  const router = useRouter();
  const [user, setUser] = useState<UserData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem("token");

    // 1. If no token, kick them out immediately
    if (!token) {
      router.push("/");
      return;
    }

    // 2. If token exists, ask the backend "Who am I?"
    axios
      .get("/api/me", {
        headers: {
          Authorization: `Bearer ${token}`, // Show the Badge!
        },
      })
      .then((res) => {
        setUser(res.data); // Save the user info
        setLoading(false);
      })
      .catch(() => {
        // If the token is fake or expired, kick them out
        localStorage.removeItem("token");
        router.push("/");
      });
  }, [router]);

  const handleLogout = () => {
    localStorage.removeItem("token"); // Destroy the badge
    router.push("/"); // Go to login
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-950 flex items-center justify-center text-white">
        Loading your dashboard...
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-950 text-white p-8">
      {/* Navbar */}
      <nav className="flex justify-between items-center mb-12 border-b border-gray-800 pb-4">
        <h1 className="text-2xl font-bold text-blue-500 flex items-center gap-2">
          <Shield className="h-6 w-6" /> AssetCompass
        </h1>
        <button
          onClick={handleLogout}
          className="flex items-center gap-2 bg-red-500/10 hover:bg-red-500/20 text-red-400 px-4 py-2 rounded-lg transition-all"
        >
          <LogOut className="h-4 w-4" /> Sign Out
        </button>
      </nav>

      {/* Main Content */}
      <div className="max-w-4xl mx-auto">
        <div className="bg-gray-900 border border-gray-800 rounded-2xl p-8 shadow-xl">
          <div className="flex items-center gap-4 mb-6">
            <div className="h-16 w-16 bg-blue-600 rounded-full flex items-center justify-center text-2xl font-bold">
              {user?.fullName.charAt(0)}
            </div>
            <div>
              <h2 className="text-2xl font-bold">Welcome back, {user?.fullName}!</h2>
              <p className="text-gray-400">{user?.email}</p>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mt-8">
            <div className="bg-gray-950 p-4 rounded-xl border border-gray-800">
              <p className="text-sm text-gray-500 mb-1">Role</p>
              <p className="font-mono text-green-400">{user?.role}</p>
            </div>
            <div className="bg-gray-950 p-4 rounded-xl border border-gray-800">
              <p className="text-sm text-gray-500 mb-1">User ID</p>
              <p className="font-mono text-purple-400">#{user?.id}</p>
            </div>
            <div className="bg-gray-950 p-4 rounded-xl border border-gray-800">
              <p className="text-sm text-gray-500 mb-1">Status</p>
              <p className="font-mono text-blue-400">Active</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}