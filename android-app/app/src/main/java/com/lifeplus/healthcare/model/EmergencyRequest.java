package com.lifeplus.healthcare.model;

import com.google.gson.annotations.SerializedName;

public class EmergencyRequest {
    @SerializedName("id")           public long id;
    @SerializedName("userId")       public Long userId;
    @SerializedName("district")     public String district;
    @SerializedName("upazila")      public String upazila;
    @SerializedName("contactPhone") public String contactPhone;
    @SerializedName("description")  public String description;
    @SerializedName("status")       public String status;
    @SerializedName("createdAt")    public String createdAt;
}
