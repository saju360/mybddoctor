package com.lifeplus.healthcare.util;

import androidx.lifecycle.MutableLiveData;
import com.google.gson.Gson;
import com.lifeplus.healthcare.model.ApiError;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Generic Retrofit Callback that posts Resource states to a MutableLiveData.
 * Handles both typed responses and Void (DELETE) responses.
 */
public class ApiCallback<T> implements Callback<T> {

    private final MutableLiveData<Resource<T>> liveData;
    private static final Gson gson = new Gson();

    public ApiCallback(MutableLiveData<Resource<T>> liveData) {
        this.liveData = liveData;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            // For Void (204 No Content) responses, body is null — that's fine
            liveData.postValue(Resource.success(response.body()));
        } else {
            liveData.postValue(Resource.error(parseError(response)));
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        String msg = t.getMessage();
        if (msg != null && msg.contains("Unable to resolve host")) {
            liveData.postValue(Resource.error("Cannot connect to server. Check your network."));
        } else {
            liveData.postValue(Resource.error("Network error: " + (msg != null ? msg : "Unknown")));
        }
    }

    private String parseError(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String raw = response.errorBody().string();
                ApiError err = gson.fromJson(raw, ApiError.class);
                if (err != null && err.message != null && !err.message.isEmpty()) {
                    return err.message;
                }
            }
        } catch (Exception ignored) {}
        return "Error " + response.code() + ": " + response.message();
    }
}
