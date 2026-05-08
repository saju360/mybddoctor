package com.lifeplus.healthcare.model;

import com.google.gson.annotations.SerializedName;

public class HealthRecord {
    @SerializedName("id")           public long id;
    @SerializedName("userId")       public long userId;
    @SerializedName("recordType")   public String recordType;
    @SerializedName("recordData")   public String recordData;
    @SerializedName("recordDate")   public String recordDate;
    @SerializedName("doctorName")   public String doctorName;
    @SerializedName("facilityName") public String facilityName;
    @SerializedName("createdAt")    public String createdAt;
}
