import { useState } from 'react';
import { api } from '../api/client';

export default function Login({ onLoggedIn }) {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState(null);

    async function handleSubmit(e) {
        e.preventDefault();
        setError(null);
        try {
            await api.login(email, password);
            onLoggedIn();
        } catch (err) {
            setError(err.message);
        }
    }

    return (
      <div style={{ maxWidth: 360, margin: '80px auto', padding: 24 }}>
          <h1>Veenu Admin</h1>
          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              <input
                type="email"
                placeholder="Email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                style={inputStyle}
              />
              <input
                type="password"
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                style={inputStyle}
              />
              {error && <p style={{ color: '#e88' }}>{error}</p>}
              <button type="submit" style={buttonStyle}>Log in</button>
          </form>
      </div>
    );
}

const inputStyle = {
    padding: 10,
    borderRadius: 8,
    border: '1px solid #1a1428',
    background: '#1a1428',
    color: '#f0ece0',
};

const buttonStyle = {
    padding: 10,
    borderRadius: 8,
    border: 'none',
    background: '#c0674a',
    color: '#f0ece0',
    fontWeight: 600,
    cursor: 'pointer',
};