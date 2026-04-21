export type BinStatus = 'EMPTY' | 'ATTENTION' | 'FULL';
<<<<<<< HEAD
export type CollectionStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'COLLECTED';
=======
>>>>>>> 8b2dcbe42d67f585adb5fa766588e67e21470521

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: {
    name: string;
    email: string;
    role: string;
  };
}

export interface DashboardSummary {
  totalSensors: number;
  byStatus: Record<BinStatus, number>;
  averageFillLevelPercent: number;
  totalAlerts: number;
}

export interface DashboardHistoryPoint {
  timestamp: string;
  averageFillLevelPercent: number;
}

export interface DashboardHistory {
  points: DashboardHistoryPoint[];
}

export interface RegionSummary {
  region: string;
  sensorCount: number;
  alertCount: number;
  averageFillLevelPercent: number;
}

export interface DashboardRegions {
  regions: RegionSummary[];
}

export interface SensorSummary {
  id: string;
  name: string;
  status: BinStatus;
  distanceCm: number;
  fillLevelPercent: number;
  region: string;
  lastUpdate: string;
}

export interface SensorReading {
  timestamp: string;
  distanceCm: number;
  fillLevelPercent: number;
}

export interface SensorDetail extends SensorSummary {
  binHeightCm: number;
  latitude: number;
  longitude: number;
  history: SensorReading[];
}

export interface SensorList {
  sensors: SensorSummary[];
}

export interface SensorHistory {
  sensorId: string;
  points: SensorReading[];
}

export interface SensorLocation {
  id: string;
  name: string;
  latitude: number;
  longitude: number;
  status: BinStatus;
  fillLevelPercent: number;
}

export interface SensorLocations {
  locations: SensorLocation[];
}
<<<<<<< HEAD

export interface CollectionAssignment {
  id: string;
  sensorId: string;
  sensorName: string;
  region: string;
  fillLevelPercent: number;
  status: CollectionStatus;
  departureTime: string;
  estimatedCollectionTime: string;
  responsibleTeam: string;
  progressPercent: number;
}

export interface CollectionList {
  collections: CollectionAssignment[];
}
=======
>>>>>>> 8b2dcbe42d67f585adb5fa766588e67e21470521
