"use client";

import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from "recharts";

// Fix: Define the exact structure coming from the Dashboard
interface ChartData {
  name: string;
  value: number;
}

interface PortfolioChartProps {
  assets: ChartData[];
}

const COLORS = ["#3B82F6", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6", "#EC4899"];

export default function PortfolioChart({ assets }: PortfolioChartProps) {
  // Filter out assets with no value to prevent empty chart segments
  const data = assets.filter(asset => asset.value > 0);

  if (data.length === 0) {
    return (
      <div className="h-80 flex items-center justify-center text-gray-500 bg-gray-900 rounded-2xl border border-gray-800">
        Add assets to see allocation
      </div>
    );
  }

  return (
    <div className="bg-gray-900 border border-gray-800 p-6 rounded-2xl shadow-xl">
      <h3 className="text-lg font-bold text-white mb-4">Allocation</h3>

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
              nameKey="name" // Explicitly tell chart to use 'name' (Ticker) as the label
              stroke="none"
            >
              {data.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
              ))}
            </Pie>
            <Tooltip
              contentStyle={{ backgroundColor: '#111827', borderColor: '#374151', borderRadius: '8px', color: '#fff' }}
              itemStyle={{ color: '#fff' }}
              // FIX: Use 'any' for value type to satisfy strict TypeScript build
              formatter={(value: any) => [
                `$${Number(value || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`,
                'Value'
              ]}
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