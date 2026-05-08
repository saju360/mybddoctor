package com.lifeplus.healthcare.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Hospital implements Serializable {
    @SerializedName("id")          public long id;
    @SerializedName("name")        public String name;
    @SerializedName("district")    public String district;
    @SerializedName("upazila")     public String upazila;
    @SerializedName("phone")       public String phone;
    @SerializedName("address")     public String address;
    @SerializedName("type")        public String type;
    @SerializedName("description") public String description; // extra notes
    @SerializedName("status")      public String status; // PENDING, APPROVED, REJECTED
    @SerializedName("adminNotes")  public String adminNotes;
    @SerializedName("createdAt")   public String createdAt;
}
