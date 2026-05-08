package com.lifeplus.healthcare.model;

import com.google.gson.annotations.SerializedName;

public class DashboardSlide {
    @SerializedName("id")           public long id;
    @SerializedName("title")        public String title;
    @SerializedName("subtitle")     public String subtitle;
    @SerializedName("imageUrl")     public String imageUrl;
    @SerializedName("actionUrl")    public String actionUrl;
    @SerializedName("displayOrder") public int displayOrder;
    @SerializedName("active")       public boolean active;
}
