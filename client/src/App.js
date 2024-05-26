import React, { useEffect, useState, useRef } from 'react';
import RealTimeChart from './components/RealTimeChart';
import BarChart from './components/BarChart';


const URI_WEBSOCKET_API = process.env.MY_VARIABLE;

const App = () => {
  const [dataStream, setDataStream] = useState([]);
  const [currentTime, setCurrentTime] = useState(new Date());

  const ws = useRef(null);

  const connectWebSocket = () => {
    ws.current = new WebSocket(`${URI_WEBSOCKET_API}`);

    ws.current.onopen = () => {
      console.log('Connected to WebSocket');
    };

    ws.current.onmessage = (event) => {
      console.log('Message received:', event.data);
      const data = JSON.parse(event.data);
      setDataStream((prevStream) => [...prevStream, data]);
    };

    ws.current.onerror = (error) => {
      console.error('WebSocket error:', error);
    };

    ws.current.onclose = () => {
      console.log('Disconnected from WebSocket');
      setTimeout(() => {
        connectWebSocket();
      }, 5000);
    };
  };

  useEffect(() => {
    connectWebSocket();

    return () => {
      if (ws.current) {
        ws.current.close();
      }
    };
  }, []);

  useEffect(() => {
    const timerId = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);

    return () => clearInterval(timerId);
  }, []);

  return (
    <div className="container mt-4">

      <h1 className="text-center my-4">Real-Time Crypto Volume</h1>
      <div className="row g-4">
        <div className="col-md-12 col-lg-12 d-flex flex-column align-items-center">
            <h2 className="text-center">Line Chart</h2>
            <RealTimeChart dataStream={dataStream} />
          </div>
      </div>
      <div className="row">
          <div className="col-6 col-md-6 col-lg-6 d-flex flex-column">
            <h2 className="text-center">Bar Chart</h2>
            <BarChart dataStream={dataStream} />
          </div>
          <div className="col-6 col-md-6 col-lg-6 d-flex flex-column">
              <h2>Current Time: {currentTime.toLocaleTimeString()}</h2>
          </div>
      </div>
    </div>
  );
};

export default App;
