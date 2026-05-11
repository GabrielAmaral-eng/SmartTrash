import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import 'leaflet/dist/leaflet.css';
import { AuthProvider } from './auth/AuthProvider';
import { ProtectedRoute } from './auth/ProtectedRoute';
import { RoleGate } from './auth/RoleGate';
import { AppLayout } from './components/layout/AppLayout';
import './index.css';
import { CollectionPage } from './pages/CollectionPage';
import { DashboardPage } from './pages/DashboardPage';
import { LoginPage } from './pages/LoginPage';
import { MapPage } from './pages/MapPage';
import { SensorsPage } from './pages/SensorsPage';
import { UsersPage } from './pages/UsersPage';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route element={<ProtectedRoute />}>
            <Route element={<AppLayout />}>
              <Route path="/" element={<Navigate to="/dashboard" replace />} />
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/sensores" element={<RoleGate allowed={['SUPER_ADMIN', 'ADMIN', 'OPERATOR']}><SensorsPage /></RoleGate>} />
              <Route path="/coleta" element={<RoleGate allowed={['SUPER_ADMIN', 'ADMIN', 'OPERATOR']}><CollectionPage /></RoleGate>} />
              <Route path="/mapa" element={<MapPage />} />
              <Route path="/usuarios" element={<RoleGate allowed={['SUPER_ADMIN']}><UsersPage /></RoleGate>} />
            </Route>
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>,
);
