package com.lifeplus.healthcare.model;

import com.google.gson.annotations.SerializedName;

public class WalkthroughSlide {
    @SerializedName("id")           public long id;
    @SerializedName("title")        public String title;
    @SerializedName("subtitle")     public String subtitle;
    @SerializedName("iconName")     public String iconName;
    @SerializedName("accentColor")  public String accentColor;
    @SerializedName("displayOrder") public int displayOrder;
    @SerializedName("active")       public boolean active;
}
