import { useState } from 'react';
import Login from './pages/Login';
import AdminQueue from './pages/AdminQueue';

export default function App() {
  const [loggedIn, setLoggedIn] = useState(false);

  return loggedIn
    ? <AdminQueue />
    : <Login onLoggedIn={() => setLoggedIn(true)} />;
}
