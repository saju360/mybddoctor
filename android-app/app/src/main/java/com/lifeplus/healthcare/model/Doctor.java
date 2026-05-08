package com.lifeplus.healthcare.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Doctor implements Serializable {
    @SerializedName("id")                       public long id;
    @SerializedName("fullName")                 public String fullName;
    @SerializedName("specialty")                public String specialty;
    @SerializedName("hospitalId")               public long hospitalId;
    @SerializedName("qualifications")           public String qualifications;
    @SerializedName("chamberSchedule")          public String chamberSchedule;
    @SerializedName("phone")                    public String phone;
    @SerializedName("availableForTelemedicine") public boolean availableForTelemedicine;
    @SerializedName("status")                   public String status; // PENDING, APPROVED, REJECTED
    @SerializedName("adminNotes")               public String adminNotes;
    @SerializedName("createdAt")                public String createdAt;
}
