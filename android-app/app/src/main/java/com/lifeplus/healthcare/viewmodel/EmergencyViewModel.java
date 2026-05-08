package com.lifeplus.healthcare.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.lifeplus.healthcare.model.EmergencyRequest;
import com.lifeplus.healthcare.network.ApiClient;
import com.lifeplus.healthcare.util.ApiCallback;
import com.lifeplus.healthcare.util.Resource;

public class EmergencyViewModel extends AndroidViewModel {

    private final MutableLiveData<Resource<EmergencyRequest>> submitResult = new MutableLiveData<>();

    public EmergencyViewModel(@NonNull Application app) { super(app); }

    public LiveData<Resource<EmergencyRequest>> getSubmitResult() { return submitResult; }

    public void submit(String district, String upazila, String phone, String description) {
        EmergencyRequest req = new EmergencyRequest();
        req.district     = district;
        req.upazila      = upazila;
        req.contactPhone = phone;
        req.description  = description;
        submitResult.setValue(Resource.loading());
        ApiClient.get(getApplication())
                .createEmergencyRequest(req)
                .enqueue(new ApiCallback<>(submitResult));
    }
}
