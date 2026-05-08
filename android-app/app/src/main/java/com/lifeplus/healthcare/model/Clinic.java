package com.lifeplus.healthcare.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Clinic implements Serializable {
    @SerializedName("id")       public long id;
    @SerializedName("name")     public String name;
    @SerializedName("district") public String district;
    @SerializedName("upazila")  public String upazila;
    @SerializedName("phone")    public String phone;
    @SerializedName("address")  public String address;
    @SerializedName("specialties") public String specialties;
    @SerializedName("status")      public String status;
    @SerializedName("adminNotes")  public String adminNotes;
}
