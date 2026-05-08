import React, { useCallback, useEffect, useState } from "react";
import api from "../api/axios";
import DataTable from "../components/DataTable";
import PageHeader from "../components/PageHeader";
import { useToast } from "../components/Toast";

const ROOM_COLUMNS = [
  { key: "id", label: "Room ID" },
  { key: "participantOneId", label: "Participant 1" },
  { key: "participantTwoId", label: "Participant 2" },
  { key: "lastMessage", label: "Last Message" },
  { key: "lastMessageTime", label: "Last Activity" },
];

const MSG_COLUMNS = [
  { key: "id", label: "Msg ID" },
  { key: "senderId", label: "Sender" },
  { key: "message", label: "Message" },
  { key: "timestamp", label: "Time" },
];

export default function ChatMonitorPage() {
  const toast = useToast();
  const [rooms, setRooms] = useState([]);
  const [messages, setMessages] = useState([]);
  const [loadingRooms, setLoadingRooms] = useState(true);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [activeRoomId, setActiveRoomId] = useState(null);

  const loadRooms = useCallback(async () => {
    setLoadingRooms(true);
    try {
      const res = await api.get("/chat/rooms/all");
      setRooms(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to load chat rooms.");
    } finally {
      setLoadingRooms(false);
    }
  }, [toast]);

  const loadMessages = useCallback(async (roomId) => {
    setLoadingMessages(true);
    setActiveRoomId(roomId);
    try {
      const res = await api.get(`/chat/messages/${roomId}`);
      setMessages(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      toast.error(err.response?.data?.message || "Failed to load messages.");
      setMessages([]);
    } finally {
      setLoadingMessages(false);
    }
  }, [toast]);

  useEffect(() => {
    loadRooms();
  }, [loadRooms]);

  return (
    <div className="page-content">
      <PageHeader
        title="Chat Monitor"
        subtitle="Monitor Android chat rooms and messages from admin panel."
      />

      <div className="header-actions" style={{ marginBottom: 12 }}>
        <button className="btn-refresh" onClick={loadRooms}>↻ Refresh Rooms</button>
      </div>

      <DataTable
        columns={ROOM_COLUMNS}
        data={rooms}
        loading={loadingRooms}
        actions={[
          {
            label: "View Messages",
            className: "btn-primary",
            onClick: (row) => loadMessages(row.id),
          },
        ]}
      />

      <div style={{ marginTop: 20 }}>
        <PageHeader
          title={activeRoomId ? `Room #${activeRoomId} Messages` : "Messages"}
          subtitle={activeRoomId ? "Live conversation history" : "Select a room to view messages"}
        />
        <DataTable
          columns={MSG_COLUMNS}
          data={messages}
          loading={loadingMessages}
          actions={[]}
        />
      </div>
    </div>
  );
}

