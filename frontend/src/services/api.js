import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8081',
  withCredentials: true,
});

export const getListings = (lat, lon, radius = 10) =>
  api.get(`/api/listings?lat=${lat}&lon=${lon}&radius=${radius}`);

export default api;
