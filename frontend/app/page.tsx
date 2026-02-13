"use client";

import { useState } from "react";
import axios from "axios";
import { Lock, Mail, Key, ArrowRight, CheckCircle } from "lucide-react";
import { useRouter } from "next/navigation";

export default function LoginPage() {
  const [step, setStep] = useState<"login" | "verify">("login");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [code, setCode] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const router = useRouter();

  // STEP 1: Send Password -> Get Code
  const handleLogin = async (e: React.FormEvent) => {
      e.preventDefault();
      setLoading(true);
      setError("");

      try {
          // Change from "/api/auth/login" to the full production URL
          await axios.post(`${process.env.NEXT_PUBLIC_API_URL}/api/auth/login`, {
              email,
              password
          });
          setStep("verify");
      } catch (err: any) {
          setError("Login failed. Check your credentials.");
      } finally {
          setLoading(false);
      }
  };

  // STEP 2: Send Code -> Get Token
  const handleVerify = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError("");

    try {
      const res = await axios.post("/api/auth/verify", { email, code });
      const token = res.data.token;

      // Save the VIP Badge!
      localStorage.setItem("token", token);

      // Step 3.4: Redirect to Dashboard 🚀
      router.push("/dashboard");

    } catch (err) {
      setError("Invalid code. Please check your email console.");
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

        {/* Error Message */}
        {error && (
          <div className="mb-6 p-3 bg-red-500/10 border border-red-500/50 rounded-lg text-red-400 text-sm text-center">
            {error}
          </div>
        )}

        {/* STEP 1 FORM */}
        {step === "login" && (
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
              {loading ? "Checking..." : <>Sign In <ArrowRight className="h-4 w-4" /></>}
            </button>
          </form>
        )}

        {/* STEP 2 FORM */}
        {step === "verify" && (
          <form onSubmit={handleVerify} className="space-y-6 animate-in fade-in slide-in-from-right-8 duration-300">
            <div className="text-center mb-4">
              <div className="inline-flex items-center justify-center h-12 w-12 rounded-full bg-blue-500/10 text-blue-400 mb-3">
                <Mail className="h-6 w-6" />
              </div>
              <h3 className="text-lg font-medium">Check your Console</h3>
              <p className="text-sm text-gray-400">We sent a code to {email}</p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-400 mb-1">Verification Code</label>
              <div className="relative">
                <Key className="absolute left-3 top-3 h-5 w-5 text-gray-500" />
                <input
                  type="text"
                  value={code}
                  onChange={(e) => setCode(e.target.value)}
                  className="w-full bg-gray-950 border border-gray-800 rounded-lg py-2.5 pl-10 pr-4 focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition-all text-center tracking-widest font-mono text-lg"
                  placeholder="000000"
                  maxLength={6}
                  required
                />
              </div>
            </div>

            <button
              disabled={loading}
              className="w-full bg-green-600 hover:bg-green-500 text-white font-semibold py-2.5 rounded-lg transition-all flex items-center justify-center gap-2"
            >
              {loading ? "Verifying..." : <>Verify Code <CheckCircle className="h-4 w-4" /></>}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}