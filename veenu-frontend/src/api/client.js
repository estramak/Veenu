const BASE_URL = 'http://localhost:8080';

async function request(path, options = {}) {
    const res = await fetch(`${BASE_URL}${path}`, {
        ...options,
        credentials: 'include',
        headers: {
        'Content-Type': 'application/json',
        ...options.headers,
        },
    });

    if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.message || `Request failed: ${res.status}`);
    }

    if (res.status === 204) return null;
    return res.json();
}

export const api = {
    login: (email, password) =>
        request('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ email, password }),
        }),
    getQueue: () => request('/api/admin/queue'),
    suspendBusiness: (id, reason)=>
        request(`/api/admin/business/${id}/suspend`, {
            method: 'POST',
            body: JSON.stringify({ reason }),
        }),
    approveBusiness: (id) =>
        request(`/api/admin/business/${id}/approve`, { method: 'POST' }),
    suspendUser: (id, reason) =>
        request(`/api/admin/users/${id}/suspend`, {
            method: 'POST',
            body: JSON.stringify({ reason }),
        }),
    suspendListing: (id, reason) =>
        request(`/api/admin/listings/${id}/suspend`, {
            method: 'POST',
            body: JSON.stringify({ reason }),
        }),
    approveListing: (id) =>
        request(`/api/admin/listings/${id}/approve`, { method: 'POST' }),
};