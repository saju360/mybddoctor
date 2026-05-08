package com.lifeplus.healthcare.model;

import com.google.gson.annotations.SerializedName;

public class MedicineReminder {
    @SerializedName("id")           public long id;
    @SerializedName("userId")       public long userId;
    @SerializedName("medicineName") public String medicineName;
    @SerializedName("reminderTime") public String reminderTime;
    @SerializedName("dosage")       public String dosage;
    @SerializedName("frequency")    public String frequency;
    @SerializedName("active")       public boolean active;
}
