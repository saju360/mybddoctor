package com.lifeplus.healthcare.model;

import com.google.gson.annotations.SerializedName;

/** Matches the GlobalExceptionHandler error response shape from the backend. */
public class ApiError {
    @SerializedName("timestamp") public String timestamp;
    @SerializedName("status")    public int status;
    @SerializedName("error")     public String error;
    @SerializedName("message")   public String message;

    public String displayMessage() {
        return (message != null && !message.isEmpty()) ? message : error;
    }
}
