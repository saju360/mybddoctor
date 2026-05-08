import { useState, useEffect, useCallback } from "react";
import { getAll } from "../api";

/**
 * Generic data-fetching hook with loading / error / reload.
 * @param {string|null} endpoint  — pass null to skip initial fetch
 */
export function useData(endpoint) {
  const [data,    setData]    = useState([]);
  const [loading, setLoading] = useState(!!endpoint);
  const [error,   setError]   = useState(null);

  const load = useCallback(async () => {
    if (!endpoint) return;
    setLoading(true);
    setError(null);
    try {
      const result = await getAll(endpoint);
      setData(Array.isArray(result) ? result : []);
    } catch (e) {
      setError(e.message || "Failed to load data");
    } finally {
      setLoading(false);
    }
  }, [endpoint]);

  useEffect(() => { load(); }, [load]);

  return { data, loading, error, reload: load, setData };
}
