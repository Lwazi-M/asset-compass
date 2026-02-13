"use client";

import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from "recharts";

interface Asset {
  type: string;
  value: number;
}

interface PortfolioChartProps {
  assets: Asset[];
}

const COLORS = ["#3B82F6", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6", "#EC4899"];

export default function PortfolioChart({ assets }: PortfolioChartProps) {
  // Group assets by Type
  const data = assets.reduce((acc: any[], asset) => {
    const existing = acc.find((item) => item.name === asset.type);
    if (existing) {
      existing.value += asset.value;
    } else {
      acc.push({ name: asset.type, value: asset.value });
    }
    return acc;
  }, []);

  if (assets.length === 0) {
    return (
      <div className="h-80 flex items-center justify-center text-gray-500 bg-gray-900 rounded-2xl border border-gray-800">
        Add assets to see allocation
      </div>
    );
  }

  return (
    <div className="bg-gray-900 border border-gray-800 p-6 rounded-2xl shadow-xl">
      <h3 className="text-lg font-bold text-white mb-4">Allocation</h3>

      {/* Explicit Height Container (Fixes the disappearing chart issue) */}
      <div className="h-64 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={data}
              cx="50%"
              cy="50%"
              innerRadius={60}
              outerRadius={80}
              paddingAngle={5}
              dataKey="value"
              stroke="none"
            >
              {data.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip
              contentStyle={{ backgroundColor: '#111827', borderColor: '#374151', borderRadius: '8px', color: '#fff' }}
              itemStyle={{ color: '#fff' }}
              formatter={(value: number | undefined) => [`$${value?.toLocaleString() ?? "0"}`, 'Value']}
            />
            <Legend
              verticalAlign="bottom"
              height={36}
              iconType="circle"
            />
          </PieChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}