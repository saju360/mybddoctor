import React, { useState, useMemo } from "react";

/**
 * Premium data table with search, pagination, sorting, and bulk selection.
 */
export default function DataTable({
  columns,
  data = [],
  actions = [],
  loading,
  pageSize = 15,
  selectable = false,
  selected = new Set(),
  onSelectChange = () => {},
}) {
  const [search,  setSearch]  = useState("");
  const [page,    setPage]    = useState(1);
  const [sortKey, setSortKey] = useState(null);
  const [sortDir, setSortDir] = useState("asc");

  // Client-side search across all visible columns
  const filtered = useMemo(() =>
    data.filter(row =>
      Object.values(row).some(v =>
        String(v ?? "").toLowerCase().includes(search.toLowerCase())
      )
    ),
    [data, search]
  );

  // Client-side sort
  const sorted = useMemo(() => {
    if (!sortKey) return filtered;
    return [...filtered].sort((a, b) => {
      const aVal = a[sortKey] ?? "";
      const bVal = b[sortKey] ?? "";
      if (typeof aVal === "number" && typeof bVal === "number")
        return sortDir === "asc" ? aVal - bVal : bVal - aVal;
      return sortDir === "asc"
        ? String(aVal).localeCompare(String(bVal))
        : String(bVal).localeCompare(String(aVal));
    });
  }, [filtered, sortKey, sortDir]);

  const totalPages = Math.max(1, Math.ceil(sorted.length / pageSize));
  const safePage   = Math.min(page, totalPages);
  const rows       = sorted.slice((safePage - 1) * pageSize, safePage * pageSize);

  function toggleSort(key) {
    if (sortKey === key) setSortDir(d => d === "asc" ? "desc" : "asc");
    else { setSortKey(key); setSortDir("asc"); }
    setPage(1);
  }

  function toggleSelect(id) {
    const next = new Set(selected);
    if (next.has(id)) next.delete(id);
    else next.add(id);
    onSelectChange(next);
  }

  function toggleSelectAll() {
    if (selected.size === rows.length && rows.length > 0) onSelectChange(new Set());
    else onSelectChange(new Set(rows.map(r => r.id)));
  }

  const allSelected = rows.length > 0 && selected.size === rows.length;
  const someSelected = selected.size > 0 && selected.size < rows.length;

  return (
    <div className="datatable-wrap">
      {/* Toolbar */}
      <div className="datatable-toolbar">
        <input
          className="search-input"
          placeholder="Search all columns…"
          value={search}
          onChange={e => { setSearch(e.target.value); setPage(1); }}
          aria-label="Search table"
        />
        <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
          {selected.size > 0 && (
            <span className="badge badge-amber">{selected.size} selected</span>
          )}
          <span className="row-count">
            {filtered.length !== data.length
              ? `${filtered.length} of ${data.length} records`
              : `${data.length} records`}
          </span>
        </div>
      </div>

      {/* Table */}
      {loading ? (
        <div className="table-loading">
          <div style={{ textAlign: "center" }}>
            <div className="spinner" style={{ margin: "0 auto 12px" }} />
            <div style={{ color: "var(--text3)", fontSize: 13 }}>Loading data…</div>
          </div>
        </div>
      ) : (
        <div className="table-scroll">
          <table className="data-table">
            <thead>
              <tr>
                {selectable && (
                  <th style={{ width: 44 }}>
                    <input
                      type="checkbox"
                      checked={allSelected}
                      ref={el => { if (el) el.indeterminate = someSelected; }}
                      onChange={toggleSelectAll}
                      aria-label="Select all rows"
                    />
                  </th>
                )}
                {columns.map(c => (
                  <th
                    key={c.key}
                    onClick={() => toggleSort(c.key)}
                    style={{ cursor: "pointer", userSelect: "none" }}
                    title={`Sort by ${c.label}`}
                  >
                    <span style={{ display: "flex", alignItems: "center", gap: 4 }}>
                      {c.label}
                      {sortKey === c.key ? (
                        <span style={{ color: "var(--primary)", fontSize: 10 }}>
                          {sortDir === "asc" ? "▲" : "▼"}
                        </span>
                      ) : (
                        <span style={{ color: "var(--text3)", fontSize: 10, opacity: 0.5 }}>⇅</span>
                      )}
                    </span>
                  </th>
                ))}
                {actions.length > 0 && <th>Actions</th>}
              </tr>
            </thead>
            <tbody>
              {rows.length === 0 ? (
                <tr>
                  <td
                    colSpan={columns.length + (actions.length ? 1 : 0) + (selectable ? 1 : 0)}
                    className="empty-row"
                  >
                    <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 8 }}>
                      <span style={{ fontSize: 32 }}>🔍</span>
                      <span>{search ? `No results for "${search}"` : "No records found"}</span>
                    </div>
                  </td>
                </tr>
              ) : (
                rows.map((row, i) => (
                  <tr key={row.id ?? i} style={selected.has(row.id) ? { background: "rgba(79,142,247,0.06)" } : undefined}>
                    {selectable && (
                      <td>
                        <input
                          type="checkbox"
                          checked={selected.has(row.id)}
                          onChange={() => toggleSelect(row.id)}
                          aria-label={`Select row ${row.id}`}
                        />
                      </td>
                    )}
                    {columns.map(c => (
                      <td key={c.key}>
                        {c.render ? c.render(row[c.key], row) : (row[c.key] ?? "—")}
                      </td>
                    ))}
                    {actions.length > 0 && (
                      <td className="action-cell">
                        {actions.map(a => (
                          <button
                            key={a.label}
                            className={`btn-action ${a.className || ""}`}
                            onClick={() => a.onClick(row)}
                            title={a.label}
                          >
                            {a.label}
                          </button>
                        ))}
                      </td>
                    )}
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="pagination">
          <button disabled={safePage === 1} onClick={() => setPage(1)} title="First page">«</button>
          <button disabled={safePage === 1} onClick={() => setPage(p => p - 1)}>‹ Prev</button>
          <span style={{ minWidth: 100, textAlign: "center" }}>
            Page <strong>{safePage}</strong> of <strong>{totalPages}</strong>
          </span>
          <button disabled={safePage === totalPages} onClick={() => setPage(p => p + 1)}>Next ›</button>
          <button disabled={safePage === totalPages} onClick={() => setPage(totalPages)} title="Last page">»</button>
        </div>
      )}
    </div>
  );
}
