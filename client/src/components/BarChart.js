import React, { useEffect, useRef, useState } from 'react';
import { Bar } from 'react-chartjs-2';
import { Chart as ChartJS, registerables } from 'chart.js';
import 'chartjs-adapter-moment';

ChartJS.register(...registerables);

const COLORS = {
  BTC: 'rgba(255, 0, 0, 0.7)',     // Red
  ETH: 'rgba(0, 255, 0, 0.7)',     // Green
  XRP: 'rgba(0, 0, 255, 0.7)',     // Blue
  LTC: 'rgba(255, 255, 0, 0.7)',   // Yellow
  BCH: 'rgba(255, 0, 255, 0.7)',   // Magenta
  EOS: 'rgba(0, 255, 255, 0.7)',   // Cyan
  BNB: 'rgba(255, 165, 0, 0.7)',   // Orange
  USDT: 'rgba(128, 0, 128, 0.7)',  // Purple
  ADA: 'rgba(0, 128, 0, 0.7)',     // Dark Green
  XLM: 'rgba(128, 128, 128, 0.7)', // Gray
  TRX: 'rgba(255, 192, 203, 0.7)', // Pink
  LINK: 'rgba(0, 0, 128, 0.7)',    // Navy
  NEO: 'rgba(128, 128, 0, 0.7)',   // Olive
  IOTA: 'rgba(128, 0, 0, 0.7)',    // Maroon
  DASH: 'rgba(0, 128, 128, 0.7)',  // Teal
  XMR: 'rgba(165, 42, 42, 0.7)',   // Brown
  ETC: 'rgba(127, 255, 0, 0.7)',   // Chartreuse
  ZEC: 'rgba(220, 20, 60, 0.7)',   // Crimson
  XTZ: 'rgba(0, 0, 0, 0.7)',       // Black
  DOGE: 'rgba(255, 215, 0, 0.7)'   // Gold
};

const BarChart = ({ dataStream }) => {
  const chartRef = useRef();
  const [chartData, setChartData] = useState({
    labels: [],
    datasets: [],
  });

  const updateChartData = () => {
    const assetData = {};

    dataStream.forEach((data) => {
      const { asset, value } = data;
      if (!assetData[asset]) {
        assetData[asset] = [];
      }
      assetData[asset].push(value);
    });

    const averageData = Object.keys(assetData).map(asset => ({
      asset,
      averageValue: assetData[asset].reduce((a, b) => a + b, 0) / assetData[asset].length,
    }));

    averageData.sort((a, b) => b.averageValue - a.averageValue);
    const top3Data = averageData.slice(0, 3);

    const labels = top3Data.map(data => data.asset);
    const dataValues = top3Data.map(data => data.averageValue);
    const backgroundColors = top3Data.map(data => COLORS[data.asset] || `rgba(${Math.floor(Math.random() * 256)}, ${Math.floor(Math.random() * 256)}, ${Math.floor(Math.random() * 256)}, 0.7)`);

    setChartData({
      labels,
      datasets: [
        {
          label: 'Top 3 Crypto Assets',
          data: dataValues,
          backgroundColor: backgroundColors,
          borderColor: backgroundColors,
          borderWidth: 1,
        },
      ],
    });
  };

  useEffect(() => {
    const intervalId = setInterval(updateChartData, 30 * 60 * 1000); // Update every 30 minutes

    // Initial update
    updateChartData();

    return () => clearInterval(intervalId);
  }, [dataStream]);

  return (
    <div className="chart-container" style={{ position: 'relative', height: '400px', width: '100%' }}>
      <Bar
        ref={chartRef}
        data={chartData}
        options={{
          scales: {
            x: {
              title: {
                display: true,
                text: 'Crypto Asset',
              },
            },
            y: {
              title: {
                display: true,
                text: 'Average Value',
              },
            },
          },
          plugins: {
            legend: {
              display: true,
              position: 'top',
            },
          },
        }}
      />
    </div>
  );
};

export default BarChart;
