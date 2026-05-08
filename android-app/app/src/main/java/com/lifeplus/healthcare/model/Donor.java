package com.lifeplus.healthcare.model;

import com.google.gson.annotations.SerializedName;

public class Donor implements java.io.Serializable {
    @SerializedName("id")               public long id;
    @SerializedName("userId")           public long userId;
    @SerializedName("bloodGroup")       public String bloodGroup;
    @SerializedName("district")         public String district;
    @SerializedName("upazila")          public String upazila;
    @SerializedName("availableNow")     public boolean availableNow;
    @SerializedName("contactPhone")     public String contactPhone;
    @SerializedName("lastDonationDate") public String lastDonationDate;
    @SerializedName("rewardPoints")     public Integer rewardPoints;
    @SerializedName("physicalHistory")  public String physicalHistory;
    @SerializedName("status")           public String status; // PENDING, APPROVED, REJECTED
    @SerializedName("adminNotes")       public String adminNotes;
    @SerializedName("createdAt")        public String createdAt;

    /** Convert display string (A+, B-, etc.) to enum name (A_POS, B_NEG, etc.) */
    public static String parseBloodGroup(String s) {
        if (s == null) return "O_POS";
        // Already in enum format
        if (s.contains("_")) return s;
        return s.replace("+", "_POS").replace("-", "_NEG").replace(" ", "");
    }
}
