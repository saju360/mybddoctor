package com.lifeplus.healthcare.model;

import com.google.gson.annotations.SerializedName;

public class AuthModels {

    public static class LoginRequest {
        @SerializedName("phone")   public final String phone;
        @SerializedName("password") public final String password;
        public LoginRequest(String phone, String password) {
            this.phone = phone; this.password = password;
        }
    }

    public static class RegisterRequest {
        @SerializedName("fullName")           public final String fullName;
        @SerializedName("phone")              public final String phone;
        @SerializedName("email")              public final String email;
        @SerializedName("password")           public final String password;
        @SerializedName("preferredLanguage")  public final String preferredLanguage;
        public RegisterRequest(String fullName, String phone, String email,
                               String password, String preferredLanguage) {
            this.fullName = fullName; this.phone = phone; this.email = email;
            this.password = password; this.preferredLanguage = preferredLanguage;
        }
    }

    public static class AuthResponse {
        @SerializedName("accessToken")  public String accessToken;
        @SerializedName("refreshToken") public String refreshToken;
        @SerializedName("tokenType")    public String tokenType;
        @SerializedName("userId")       public long userId;
        @SerializedName("role")         public String role;
    }

    public static class RefreshRequest {
        @SerializedName("refreshToken") public final String refreshToken;
        public RefreshRequest(String refreshToken) { this.refreshToken = refreshToken; }
    }
}
