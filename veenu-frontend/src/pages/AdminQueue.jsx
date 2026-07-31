import { useEffect, useState } from 'react';
import { api } from '../api/client';

const SECTIONS = [
  { key: 'flaggedBusinesses', title: 'Flagged Businesses', color: '#c0674a' },
  { key: 'pendingBusinesses', title: 'Pending Businesses', color: '#e8c547' },
  { key: 'pendingUsers', title: 'Pending Users', color: '#7a9e7e' },
  { key: 'pendingListings', title: 'Pending Listings', color: '#6e4f6e' },
  { key: 'suspendedListings', title: 'Suspended Listings', color: '#888' },
];

export default function AdminQueue() {
  const [queue, setQueue] = useState(null);
  const [error, setError] = useState(null);
  const [busyKey, setBusyKey] = useState(null);

  useEffect(() => {
    loadQueue();
  }, []);

  async function loadQueue() {
    try {
      const data = await api.getQueue();
      setQueue(data);
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleAction(item, action) {
    const itemKey = item.entityType + item.id;
    setBusyKey(itemKey);
    setError(null);

    try {
      const needsReason = action === 'suspend' || action === 'ban';
      const reason = needsReason
        ? window.prompt(`Reason for ${action}?`, '')
        : undefined;

      if (needsReason && reason === null) {
        // user clicked Cancel on the prompt
        setBusyKey(null);
        return;
      }

      if (item.entityType === 'BUSINESS') {
        if (action === 'suspend') await api.suspendBusiness(item.id, reason);
        if (action === 'approve') await api.approveBusiness(item.id);
      } else if (item.entityType === 'USER') {
        if (action === 'suspend') await api.suspendUser(item.id, reason);
        if (action === 'approve') await api.approveUser(item.id);
        if (action === 'ban') await api.banUser(item.id, reason);
      } else if (item.entityType === 'LISTING') {
        if (action === 'suspend') await api.suspendListing(item.id, reason);
        if (action === 'approve') await api.approveListing(item.id);
      }

      await loadQueue();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusyKey(null);
    }
  }

  if (error) return <p style={{ color: '#e88', padding: 24 }}>{error}</p>;
  if (!queue) return <p style={{ padding: 24 }}>Loading queue…</p>;

  return (
    <div style={{ maxWidth: 900, margin: '40px auto', padding: 24 }}>
      <h1>Admin Queue</h1>

      {SECTIONS.map((section) => {
        const items = queue[section.key] || [];
        if (items.length === 0) return null;

        return (
          <div key={section.key} style={{ marginTop: 32 }}>
            <h2 style={{ color: section.color, fontSize: 20 }}>{section.title}</h2>
            {items.map((item) => {
              const itemKey = item.entityType + item.id;
              const isBusy = busyKey === itemKey;

              return (
                <div
                  key={itemKey}
                  style={{
                    background: '#1a1428',
                    borderRadius: 10,
                    padding: 16,
                    marginTop: 8,
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                  }}
                >
                  <div>
                    <strong>{item.name}</strong>
                    <div style={{ fontSize: 12, opacity: 0.6 }}>
                      #{item.id} · {item.status}
                      {item.reason && ` · "${item.reason}"`}
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: 8 }}>
                    <button
                      disabled={isBusy}
                      onClick={() => handleAction(item, 'approve')}
                      style={actionButton('#7a9e7e')}
                    >
                      Approve
                    </button>
                    <button
                      disabled={isBusy}
                      onClick={() => handleAction(item, 'suspend')}
                      style={actionButton('#c0674a')}
                    >
                      Suspend
                    </button>
                    {item.entityType === 'USER' && (
                      <button
                        disabled={isBusy}
                        onClick={() => handleAction(item, 'ban')}
                        style={actionButton('#8a2c2c')}
                      >
                        Ban
                      </button>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        );
      })}
    </div>
  );
}

function actionButton(color) {
  return {
    padding: '6px 12px',
    borderRadius: 6,
    border: `1px solid ${color}`,
    background: 'transparent',
    color,
    cursor: 'pointer',
    fontSize: 13,
  };
}