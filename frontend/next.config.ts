import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  async rewrites() {
    return [
      {
        source: "/api/:path*", // When frontend asks for /api/...
        destination: "http://localhost:8080/api/:path*", // Send it to Backend 8080
      },
    ];
  },
};

export default nextConfig;