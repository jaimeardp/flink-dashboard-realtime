
import React, { useEffect, useState, useRef } from 'react';
import { Line } from 'react-chartjs-2';
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

const RealTimeChart = ({ dataStream }) => {
  const chartRef = useRef();
  const [chartData, setChartData] = useState({
    labels: [],
    datasets: [],
  });

  useEffect(() => {
    if (dataStream.length === 0) return;

    console.log('Data stream:', dataStream);
    console.log('Data stream length:', dataStream.length);

    const latestData = dataStream[dataStream.length - 1];
    const timestamp = new Date();

    console.log('Latest data:', latestData);
    console.log('Timestamp:', timestamp);

    setChartData((prevData) => {
      const updatedLabels = [...prevData.labels, timestamp];
      const assetIndex = prevData.datasets.findIndex(dataset => dataset.label === latestData.asset);

      let updatedDatasets;

      if (assetIndex >= 0) {
        updatedDatasets = prevData.datasets.map((dataset, index) =>
          index === assetIndex
            ? {
                ...dataset,
                data: [...dataset.data, { x: timestamp, y: latestData.value }],
              }
            : dataset
        );
      } else {
        updatedDatasets = [
          ...prevData.datasets,
          {
            label: latestData.asset,
            data: [{ x: timestamp, y: latestData.value }],
            borderColor: COLORS[latestData.asset] || `rgba(${Math.floor(Math.random() * 256)}, ${Math.floor(Math.random() * 256)}, ${Math.floor(Math.random() * 256)}, 0.7)`,
            backgroundColor: COLORS[latestData.asset] || `rgba(${Math.floor(Math.random() * 256)}, ${Math.floor(Math.random() * 256)}, ${Math.floor(Math.random() * 256)}, 0.2)`,
            fill: false,
            pointRadius: 3,
          },
        ];
      }

      const maxLength = 30; // Show only the last 30 data points
      const truncatedLabels = updatedLabels.slice(-maxLength);
      const truncatedDatasets = updatedDatasets.map(dataset => ({
        ...dataset,
        data: dataset.data.slice(-maxLength),
      }));

      return {
        labels: truncatedLabels,
        datasets: truncatedDatasets,
      };
    });
  }, [dataStream]);

  return (
    <div className="chart-container" style={{ position: 'relative', height: '600px', width: '100%' }}>

    <Line
      ref={chartRef}
      data={chartData}
      options={{
        scales: {
          x: {
            type: 'time',
            time: {
              unit: 'second',
              tooltipFormat: 'll HH:mm:ss',
            },
            title: {
              display: true,
              text: 'Time',
            },
          },
          y: {
            title: {
              display: true,
              text: 'Value',
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

export default RealTimeChart;
