package com.lifeplus.healthcare.model;

import com.google.gson.annotations.SerializedName;

public class BloodRequest implements java.io.Serializable {
    @SerializedName("id")                  public long id;
    @SerializedName("requestedByUserId")   public Long requestedByUserId;
    @SerializedName("donorId")             public Long donorId;
    @SerializedName("patientName")         public String patientName;
    @SerializedName("hospitalName")        public String hospitalName;
    @SerializedName("bloodGroup")          public String bloodGroup;
    @SerializedName("district")            public String district;
    @SerializedName("upazila")             public String upazila;
    @SerializedName("contactPhone")        public String contactPhone;
    @SerializedName("status")              public String status;
    @SerializedName("notes")               public String notes;
    @SerializedName("donatedByUserId")     public Long donatedByUserId;
    @SerializedName("adminNotes")          public String adminNotes;
    @SerializedName("urgency")             public String urgency; // NORMAL, URGENT
    @SerializedName("createdAt")           public String createdAt;
}
