package com.lifeplus.healthcare.model;

import com.google.gson.annotations.SerializedName;

public class TelemedicineSession {
    @SerializedName("id")             public long id;
    @SerializedName("doctorId")       public long doctorId;
    @SerializedName("patientUserId")  public long patientUserId;
    @SerializedName("scheduledAt")    public String scheduledAt;
    @SerializedName("status")         public String status;
    @SerializedName("meetingLink")    public String meetingLink;
    @SerializedName("notes")          public String notes;
    @SerializedName("createdAt")      public String createdAt;
}
