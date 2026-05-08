package com.lifeplus.healthcare.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Ambulance implements Serializable {
    @SerializedName("id")            public long id;
    @SerializedName("providerName")  public String providerName;
    @SerializedName("district")      public String district;
    @SerializedName("phone")         public String phone;
    @SerializedName("vehicleNumber") public String vehicleNumber;
    @SerializedName("available")     public boolean available;
}
