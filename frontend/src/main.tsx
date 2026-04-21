import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
<<<<<<< HEAD
import 'leaflet/dist/leaflet.css';
import { AppLayout } from './components/layout/AppLayout';
import './index.css';
import { DashboardPage } from './pages/DashboardPage';
import { CollectionPage } from './pages/CollectionPage';
=======
import { AppLayout } from './components/layout/AppLayout';
import './index.css';
import { DashboardPage } from './pages/DashboardPage';
>>>>>>> 8b2dcbe42d67f585adb5fa766588e67e21470521
import { LoginPage } from './pages/LoginPage';
import { MapPage } from './pages/MapPage';
import { SensorsPage } from './pages/SensorsPage';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route element={<AppLayout />}>
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/sensores" element={<SensorsPage />} />
<<<<<<< HEAD
          <Route path="/coleta" element={<CollectionPage />} />
=======
>>>>>>> 8b2dcbe42d67f585adb5fa766588e67e21470521
          <Route path="/mapa" element={<MapPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  </React.StrictMode>,
);
