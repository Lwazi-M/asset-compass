"use client";

import { useState, useEffect, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import axios from "axios";

function VerifyForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const email = searchParams.get("email");
  // Check if we passed the code from the Register page
  const autoCode = searchParams.get("autoCode");

  const [code, setCode] = useState(autoCode || "");
  const [error, setError] = useState("");
  const [successMsg, setSuccessMsg] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [timeLeft, setTimeLeft] = useState(120);
  const [canResend, setCanResend] = useState(false);

  useEffect(() => {
    if (autoCode) {
      setSuccessMsg(`DEV MODE: Your code is ${autoCode}`);
    }
  }, [autoCode]);

  useEffect(() => {
    if (timeLeft > 0) {
      const timerId = setTimeout(() => setTimeLeft(timeLeft - 1), 1000);
      return () => clearTimeout(timerId);
    } else {
      setCanResend(true);
    }
  }, [timeLeft]);

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
    // Force reset for UX
    setError("");
    setSuccessMsg("");
    setCanResend(false);
    setTimeLeft(300);

    try {
      const res = await axios.post(
        `${process.env.NEXT_PUBLIC_API_URL || 'https://asset-compass-production.up.railway.app'}/api/auth/resend-code`,
        { email }
      );

      // --- DEV MODE MAGIC ---
      // If the backend sent us the code, show it immediately!
      if (res.data.debug_code) {
        setSuccessMsg(`DEV MODE: Your new code is ${res.data.debug_code}`);
        setCode(res.data.debug_code); // Auto-fill input for convenience
      } else {
        setSuccessMsg("New code sent! Check your inbox.");
      }

    } catch (err: any) {
      setError("Failed to resend code.");
      setCanResend(true); // Let them try again if it failed
    }
  };

  return (
    <div className="text-center">
      <h2 className="text-3xl font-bold text-white">Verify Your Email</h2>
      <p className="mt-2 text-gray-400">
        We sent a code to <span className="text-blue-400">{email}</span>
      </p>

      <form onSubmit={handleSubmit} className="mt-8 space-y-6">
        {/* Success/Dev Mode Message */}
        {successMsg && (
          <div className="rounded bg-blue-900/50 p-4 text-sm text-blue-200 border border-blue-800 animate-pulse">
            <span className="font-bold">🔔 {successMsg}</span>
          </div>
        )}

        {error && (
          <div className="rounded bg-red-900/50 p-3 text-sm text-red-200 border border-red-800">
            {error}
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

        <div className="mt-4 text-sm text-gray-400">
          Didn't get the code?{" "}
          <button
            type="button"
            onClick={handleResend}
            // Allow clicking even if disabled logic triggers, for ease of testing
            disabled={!canResend}
            className={`font-medium underline transition-all ${
              canResend
                ? "text-blue-400 hover:text-blue-300 cursor-pointer"
                : "text-gray-600 cursor-not-allowed no-underline"
            }`}
          >
            {canResend ? "Resend Code Now" : `Resend in ${formatTime(timeLeft)}`}
          </button>
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