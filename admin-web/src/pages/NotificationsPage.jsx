import React, { useState } from "react";
import api from "../api/axios";
import PageHeader from "../components/PageHeader";
import { useToast } from "../components/Toast";

export default function NotificationsPage() {
  const toast = useToast();
  const [loading, setLoading] = useState(false);

  const [fcmToken, setFcmToken] = useState("");
  const [fcmTitle, setFcmTitle] = useState("LifePlus Notification");
  const [fcmMessage, setFcmMessage] = useState("");

  const [topic, setTopic] = useState("user_1");
  const [topicTitle, setTopicTitle] = useState("LifePlus Update");
  const [topicMessage, setTopicMessage] = useState("");

  const [userId, setUserId] = useState("1");
  const [userTitle, setUserTitle] = useState("Account Notice");
  const [userMessage, setUserMessage] = useState("");

  const [district, setDistrict] = useState("Dhaka");
  const [districtType, setDistrictType] = useState("blood");
  const [districtTitle, setDistrictTitle] = useState("District Alert");
  const [districtMessage, setDistrictMessage] = useState("");

  const [smsTo, setSmsTo] = useState("");
  const [smsMessage, setSmsMessage] = useState("");

  async function sendRequest(requester, successText) {
    setLoading(true);
    try {
      const res = await requester();
      toast.success(successText + (res?.data?.topic ? ` (${res.data.topic})` : ""));
    } catch (err) {
      toast.error(err.response?.data?.message || "Notification send failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page-content">
      <PageHeader
        title="Notifications Control"
        subtitle="Control Android push + SMS from web admin."
      />

      <div className="notifications-grid">
        <section className="section-card">
          <h3>Push to FCM Token</h3>
          <input value={fcmToken} onChange={(e) => setFcmToken(e.target.value)} placeholder="FCM device token" />
          <input value={fcmTitle} onChange={(e) => setFcmTitle(e.target.value)} placeholder="Title" />
          <textarea value={fcmMessage} onChange={(e) => setFcmMessage(e.target.value)} placeholder="Message" rows={3} />
          <button className="btn-primary" disabled={loading || !fcmToken || !fcmMessage}
            onClick={() => sendRequest(() => api.post("/notifications/fcm", { to: fcmToken, title: fcmTitle, message: fcmMessage }), "Push sent")}>Send</button>
        </section>

        <section className="section-card">
          <h3>Push to Topic</h3>
          <input value={topic} onChange={(e) => setTopic(e.target.value)} placeholder="topic e.g. user_5" />
          <input value={topicTitle} onChange={(e) => setTopicTitle(e.target.value)} placeholder="Title" />
          <textarea value={topicMessage} onChange={(e) => setTopicMessage(e.target.value)} placeholder="Message" rows={3} />
          <button className="btn-primary" disabled={loading || !topic || !topicMessage}
            onClick={() => sendRequest(() => api.post("/notifications/topic", { topic, title: topicTitle, message: topicMessage }), "Topic push sent")}>Send</button>
        </section>

        <section className="section-card">
          <h3>Push to User ID</h3>
          <input value={userId} onChange={(e) => setUserId(e.target.value)} placeholder="User ID" />
          <input value={userTitle} onChange={(e) => setUserTitle(e.target.value)} placeholder="Title" />
          <textarea value={userMessage} onChange={(e) => setUserMessage(e.target.value)} placeholder="Message" rows={3} />
          <button className="btn-primary" disabled={loading || !userId || !userMessage}
            onClick={() => sendRequest(() => api.post(`/notifications/user/${userId}`, { title: userTitle, message: userMessage }), "User push sent")}>Send</button>
        </section>

        <section className="section-card">
          <h3>District Broadcast</h3>
          <input value={district} onChange={(e) => setDistrict(e.target.value)} placeholder="District (e.g. Dhaka)" />
          <select value={districtType} onChange={(e) => setDistrictType(e.target.value)}>
            <option value="blood">Blood Request Topic</option>
            <option value="emergency">Emergency Topic</option>
          </select>
          <input value={districtTitle} onChange={(e) => setDistrictTitle(e.target.value)} placeholder="Title" />
          <textarea value={districtMessage} onChange={(e) => setDistrictMessage(e.target.value)} placeholder="Message" rows={3} />
          <button className="btn-primary" disabled={loading || !district || !districtMessage}
            onClick={() => sendRequest(() => api.post(`/notifications/broadcast/district/${encodeURIComponent(district)}`, { topicType: districtType, title: districtTitle, message: districtMessage }), "District push sent")}>Send</button>
        </section>

        <section className="section-card">
          <h3>SMS Send</h3>
          <input value={smsTo} onChange={(e) => setSmsTo(e.target.value)} placeholder="Phone number" />
          <textarea value={smsMessage} onChange={(e) => setSmsMessage(e.target.value)} placeholder="SMS text" rows={3} />
          <button className="btn-primary" disabled={loading || !smsTo || !smsMessage}
            onClick={() => sendRequest(() => api.post("/notifications/sms", { to: smsTo, message: smsMessage }), "SMS queued")}>Send</button>
        </section>
      </div>

      <style>{`
        .notifications-grid {
          display: grid;
          grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
          gap: 16px;
        }
        .section-card h3 {
          margin-top: 0;
          margin-bottom: 12px;
          font-size: 16px;
        }
        .section-card input,
        .section-card textarea,
        .section-card select {
          width: 100%;
          margin-bottom: 8px;
          border: 1px solid var(--border);
          background: var(--bg3);
          color: var(--text);
          border-radius: 8px;
          padding: 10px;
          font-size: 13px;
        }
        .section-card button { width: 100%; }
      `}</style>
    </div>
  );
}
