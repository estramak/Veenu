import { useState } from 'react';
import Login from './pages/Login';

export default function App() {
  const [loggedIn, setLoggedIn] = useState(false);

  return loggedIn
    ? <p>You're logged in! (Admin queue page coming next)</p>
    : <Login onLoggedIn={() => setLoggedIn(true)} />;
}
