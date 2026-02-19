"use client";

import { useState, Suspense } from "react";
import axios from "axios";
import { Lock, Mail, ArrowRight } from "lucide-react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";

function LoginForm() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const router = useRouter();
  const searchParams = useSearchParams();
  const registered = searchParams.get("registered");

  const handleLogin = async (e: React.FormEvent) => {
      e.preventDefault();
      setLoading(true);
      setError("");

      try {
          const res = await axios.post(`${process.env.NEXT_PUBLIC_API_URL || 'https://asset-compass-production.up.railway.app'}/api/auth/login`, {
              email,
              password
          });

          // Direct login bypass! Save token and go to dashboard
          localStorage.setItem("token", res.data.token);
          router.push("/dashboard");

      } catch (err: any) {
          setError("Login failed. Check your credentials.");
      } finally {
          setLoading(false);
      }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-950 text-white p-4">
      <div className="w-full max-w-md bg-gray-900 border border-gray-800 rounded-2xl shadow-xl p-8">

        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold tracking-tight text-blue-500">AssetCompass</h1>
          <p className="text-gray-400 mt-2">Professional Portfolio Tracking</p>
        </div>

        {/* Success Message from Registration */}
        {registered && (
          <div className="mb-6 p-3 bg-green-500/10 border border-green-500/50 rounded-lg text-green-400 text-sm text-center">
            Account created successfully! You can now log in.
          </div>
        )}

        {/* Error Message */}
        {error && (
          <div className="mb-6 p-3 bg-red-500/10 border border-red-500/50 rounded-lg text-red-400 text-sm text-center">
            {error}
          </div>
        )}

        <form onSubmit={handleLogin} className="space-y-6">
          <div>
            <label className="block text-sm font-medium text-gray-400 mb-1">Email Address</label>
            <div className="relative">
              <Mail className="absolute left-3 top-3 h-5 w-5 text-gray-500" />
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full bg-gray-950 border border-gray-800 rounded-lg py-2.5 pl-10 pr-4 focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition-all"
                placeholder="admin@assetcompass.com"
                required
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-400 mb-1">Password</label>
            <div className="relative">
              <Lock className="absolute left-3 top-3 h-5 w-5 text-gray-500" />
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full bg-gray-950 border border-gray-800 rounded-lg py-2.5 pl-10 pr-4 focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition-all"
                placeholder="••••••••"
                required
              />
            </div>
          </div>

          <button
            disabled={loading}
            className="w-full bg-blue-600 hover:bg-blue-500 text-white font-semibold py-2.5 rounded-lg transition-all flex items-center justify-center gap-2"
          >
            {loading ? "Signing In..." : <>Sign In <ArrowRight className="h-4 w-4" /></>}
          </button>

          <div className="mt-4 text-center text-sm text-gray-400">
            Don't have an account?{" "}
            <Link href="/register" className="font-medium text-blue-400 hover:text-blue-300 transition-colors">
              Sign up now
            </Link>
          </div>
        </form>

      </div>
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense fallback={<div className="flex min-h-screen items-center justify-center bg-gray-950 text-white">Loading...</div>}>
      <LoginForm />
    </Suspense>
  );
}