"use client";

import { useState, Suspense } from "react";
import { useRouter, useSearchParams } from "next/navigation";

// We wrap the logic in a component to use searchParams safely
function VerifyForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const email = searchParams.get("email");

  const [code, setCode] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError("");

    try {
      // Note: We use GET request here because verify is a query param endpoint
      const res = await fetch(
        `https://asset-compass-production.up.railway.app/api/auth/verify?email=${email}&code=${code}`,
        { method: "POST" } // Changed to POST if your backend expects POST, but usually verify logic varies.
        // Based on your backend AuthService, verify takes params.
        // If your backend controller uses @RequestParam, it might be GET or POST.
        // Let's assume POST for safety, but check AuthController.
      );

      if (res.ok) {
        // Success! Go to login
        router.push("/?verified=true");
      } else {
        const text = await res.text();
        setError(text || "Verification failed");
      }
    } catch (err) {
      setError("Something went wrong.");
    } finally {
      setIsLoading(false);
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

        <div>
          <label className="text-sm font-medium text-gray-300">Verification Code</label>
          <input
            type="text"
            required
            maxLength={6}
            className="mt-1 block w-full rounded-lg bg-gray-700 border border-gray-600 p-2.5 text-center text-2xl tracking-widest text-white placeholder-gray-500 focus:border-blue-500 focus:ring-blue-500"
            placeholder="123456"
            value={code}
            onChange={(e) => setCode(e.target.value)}
          />
        </div>

        <button
          type="submit"
          disabled={isLoading}
          className="w-full rounded-lg bg-green-600 px-5 py-3 text-center text-sm font-medium text-white hover:bg-green-700 focus:outline-none focus:ring-4 focus:ring-green-800 disabled:opacity-50"
        >
          {isLoading ? "Verifying..." : "Verify Account"}
        </button>
      </form>
    </div>
  );
}

export default function VerifyPage() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-900 p-4">
      <div className="w-full max-w-md space-y-8 rounded-xl bg-gray-800 p-8 shadow-lg">
        <Suspense fallback={<div className="text-white">Loading...</div>}>
          <VerifyForm />
        </Suspense>
      </div>
    </div>
  );
}