package com.lifeplus.healthcare.model;

import com.google.gson.annotations.SerializedName;

public class Appointment {
    @SerializedName("id")              public long id;
    @SerializedName("userId")          public long userId;
    @SerializedName("doctorId")        public long doctorId;
    @SerializedName("appointmentDate") public String appointmentDate;
    @SerializedName("timeSlot")        public String timeSlot;
    @SerializedName("status")          public String status;
    @SerializedName("notes")           public String notes;
    @SerializedName("createdAt")       public String createdAt;
}
