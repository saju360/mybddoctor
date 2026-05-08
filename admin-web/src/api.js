// Centralised API client — all requests go through here.
// JWT token is injected automatically via the headers() helper.

const API_BASE = (import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1").replace(/\/$/, "");

// ── Token management ──────────────────────────────────────────────────────────

let _token = localStorage.getItem("admin_token") || null;

export const setToken = (t) => {
  _token = t;
  if (t) localStorage.setItem("admin_token", t);
  else localStorage.removeItem("admin_token");
};

export const getToken = () => _token;
export const isLoggedIn = () => !!_token;

const authHeaders = () => {
  const h = { "Content-Type": "application/json" };
  if (_token) h["Authorization"] = `Bearer ${_token}`;
  return h;
};

// ── Core fetch wrapper ────────────────────────────────────────────────────────

async function request(method, path, body) {
  const res = await fetch(`${API_BASE}/${path}`, {
    method,
    headers: authHeaders(),
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (res.status === 401) {
    // Token expired — clear session and reload to login
    setToken(null);
    localStorage.removeItem("admin_role");
    window.location.href = "/login";
    throw new Error("Session expired. Please log in again.");
  }

  if (res.status === 204 || res.headers.get("content-length") === "0") {
    return null; // DELETE / no-content responses
  }

  const text = await res.text();
  let json;
  try { json = JSON.parse(text); } catch { json = { message: text }; }

  if (!res.ok) {
    throw new Error(json?.message || `HTTP ${res.status}`);
  }
  return json;
}

// ── Auth ──────────────────────────────────────────────────────────────────────

export const login = (phone, password) =>
  request("POST", "auth/login", { phone, password });

export const logout = () => {
  setToken(null);
  localStorage.removeItem("admin_role");
};

// ── Generic CRUD ──────────────────────────────────────────────────────────────

export const getAll    = (ep)         => request("GET",    ep);
export const getById   = (ep, id)     => request("GET",    `${ep}/${id}`);
export const create    = (ep, data)   => request("POST",   ep, data);
export const update    = (ep, id, d)  => request("PUT",    `${ep}/${id}`, d);
export const del       = (ep, id)     => request("DELETE", `${ep}/${id}`);
export const putAction = (ep, id, action, body) =>
  request("PUT", `${ep}/${id}/${action}`, body);

// ── Approvals ─────────────────────────────────────────────────────────────────

export const approveRequest = (id)        => putAction("approvals", id, "approve");
export const rejectRequest  = (id, notes) => putAction("approvals", id, "reject", { notes });

// ── Specific endpoints ────────────────────────────────────────────────────────

export const getPendingApprovals = () => getAll("approvals/pending");
export const getMe               = () => request("GET", "users/me");

// ── Global Settings ───────────────────────────────────────────────────────────

export const getSettings    = () => getAll("settings");
export const updateSetting  = (key, value, desc) => request("POST", "settings", { settingKey: key, settingValue: value, description: desc });
