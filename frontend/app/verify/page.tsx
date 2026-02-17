"use client";

import { useState, useEffect, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import axios from "axios";

// We wrap the logic in a component to use searchParams safely
function VerifyForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const email = searchParams.get("email");

  const [code, setCode] = useState("");
  const [error, setError] = useState("");
  const [successMsg, setSuccessMsg] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  // --- TIMER STATE ---
  const [timeLeft, setTimeLeft] = useState(120); // 2 minutes in seconds
  const [canResend, setCanResend] = useState(false);

  // Countdown Logic
  useEffect(() => {
    if (timeLeft > 0) {
      const timerId = setTimeout(() => setTimeLeft(timeLeft - 1), 1000);
      return () => clearTimeout(timerId);
    } else {
      setCanResend(true);
    }
  }, [timeLeft]);

  // Helper to format MM:SS
  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs < 10 ? "0" : ""}${secs}`;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError("");
    setSuccessMsg("");

    try {
      // Use axios to send JSON body (cleaner than fetch for POST)
      const res = await axios.post(
        `${process.env.NEXT_PUBLIC_API_URL || 'https://asset-compass-production.up.railway.app'}/api/auth/verify`,
        { email, code }
      );

      if (res.status === 200) {
        localStorage.setItem("token", res.data.token);
        router.push("/dashboard");
      }
    } catch (err: any) {
      setError(err.response?.data || "Verification failed");
    } finally {
      setIsLoading(false);
    }
  };

  const handleResend = async () => {
    if (!canResend) return;

    setError("");
    setSuccessMsg("");
    setCanResend(false);
    setTimeLeft(300); // Reset timer to 5 minutes (300s)

    try {
      await axios.post(
        `${process.env.NEXT_PUBLIC_API_URL || 'https://asset-compass-production.up.railway.app'}/api/auth/resend-code`,
        { email }
      );
      setSuccessMsg("New code sent! Check your inbox.");
    } catch (err: any) {
      setError("Failed to resend code. Please try again later.");
      setTimeLeft(0); // Allow retry immediately on fail
      setCanResend(true);
    }
  };

  return (
    <div className="text-center">
      <h2 className="text-3xl font-bold text-white">Verify Your Email</h2>
      <p className="mt-2 text-gray-400">
        We sent a code to <span className="text-blue-400">{email}</span>
      </p>

      <form onSubmit={handleSubmit} className="mt-8 space-y-6">
        {error && (
          <div className="rounded bg-red-900/50 p-3 text-sm text-red-200 border border-red-800">
            {error}
          </div>
        )}

        {successMsg && (
          <div className="rounded bg-green-900/50 p-3 text-sm text-green-200 border border-green-800">
            {successMsg}
          </div>
        )}

        <div>
          <label className="text-sm font-medium text-gray-300">Verification Code</label>
          <input
            type="text"
            required
            maxLength={6}
            className="mt-1 block w-full rounded-lg bg-gray-700 border border-gray-600 p-2.5 text-center text-2xl tracking-widest text-white placeholder-gray-500 focus:border-blue-500 focus:ring-blue-500 outline-none"
            placeholder="123456"
            value={code}
            onChange={(e) => setCode(e.target.value)}
          />
        </div>

        <button
          type="submit"
          disabled={isLoading}
          className="w-full rounded-lg bg-green-600 px-5 py-3 text-center text-sm font-medium text-white hover:bg-green-700 focus:outline-none focus:ring-4 focus:ring-green-800 disabled:opacity-50 transition-all"
        >
          {isLoading ? "Verifying..." : "Verify Account"}
        </button>

        {/* --- RESEND LOGIC UI --- */}
        <div className="mt-4 text-sm text-gray-400">
          Didn't get the code?{" "}
          {canResend ? (
            <button
              type="button"
              onClick={handleResend}
              className="font-medium text-blue-400 hover:text-blue-300 underline cursor-pointer hover:no-underline transition-all"
            >
              Resend Code
            </button>
          ) : (
            <span className="text-gray-500 cursor-not-allowed">
              Resend available in {formatTime(timeLeft)}
            </span>
          )}
        </div>
      </form>
    </div>
  );
}

export default function VerifyPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-900 p-4">
      <div className="w-full max-w-md space-y-8 rounded-xl bg-gray-800 p-8 shadow-lg border border-gray-700">
        <Suspense fallback={<div className="text-white">Loading...</div>}>
          <VerifyForm />
        </Suspense>
      </div>
    </div>
  );
}