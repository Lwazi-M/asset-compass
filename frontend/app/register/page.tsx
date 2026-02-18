"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

export default function Register() {
  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    password: "",
  });
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const router = useRouter();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError("");

    try {
      const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL || 'https://asset-compass-production.up.railway.app'}/api/auth/register`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(formData),
        }
      );

      const data = await res.json();

      if (res.ok) {
        // Success! Extract the debug code if it exists
        const autoCode = data.debug_code ? `&autoCode=${data.debug_code}` : "";

        // Pass it to the verify page
        router.push(`/verify?email=${encodeURIComponent(formData.email)}${autoCode}`);
      } else {
        setError(data.message || "Registration failed");
      }
    } catch (err) {
      setError("Something went wrong. Please try again.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-900 p-4">
      <div className="w-full max-w-md space-y-8 rounded-xl bg-gray-800 p-8 shadow-lg">
        <div className="text-center">
          <h2 className="text-3xl font-bold text-white">Create Account</h2>
          <p className="mt-2 text-gray-400">Join AssetCompass today</p>
        </div>

        <form onSubmit={handleSubmit} className="mt-8 space-y-6">
          {error && (
            <div className="rounded bg-red-900/50 p-3 text-sm text-red-200 border border-red-800">
              {error}
            </div>
          )}

          <div className="space-y-4">
            <div>
              <label className="text-sm font-medium text-gray-300">Full Name</label>
              <input
                type="text"
                required
                className="mt-1 block w-full rounded-lg bg-gray-700 border border-gray-600 p-2.5 text-white placeholder-gray-400 focus:border-blue-500 focus:ring-blue-500"
                placeholder="John Doe"
                value={formData.fullName}
                onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
              />
            </div>

            <div>
              <label className="text-sm font-medium text-gray-300">Email Address</label>
              <input
                type="email"
                required
                className="mt-1 block w-full rounded-lg bg-gray-700 border border-gray-600 p-2.5 text-white placeholder-gray-400 focus:border-blue-500 focus:ring-blue-500"
                placeholder="name@company.com"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              />
            </div>

            <div>
              <label className="text-sm font-medium text-gray-300">Password</label>
              <input
                type="password"
                required
                className="mt-1 block w-full rounded-lg bg-gray-700 border border-gray-600 p-2.5 text-white placeholder-gray-400 focus:border-blue-500 focus:ring-blue-500"
                placeholder="••••••••"
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={isLoading}
            className="w-full rounded-lg bg-blue-600 px-5 py-3 text-center text-sm font-medium text-white hover:bg-blue-700 focus:outline-none focus:ring-4 focus:ring-blue-800 disabled:opacity-50"
          >
            {isLoading ? "Creating Account..." : "Sign Up"}
          </button>

          <p className="text-center text-sm text-gray-400">
            Already have an account?{" "}
            <Link href="/" className="font-medium text-blue-400 hover:text-blue-300">
              Sign in here
            </Link>
          </p>
        </form>
      </div>
    </div>
  );
}