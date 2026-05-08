package com.lifeplus.healthcare.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.lifeplus.healthcare.model.BloodRequest;
import com.lifeplus.healthcare.network.ApiClient;
import com.lifeplus.healthcare.util.ApiCallback;
import com.lifeplus.healthcare.util.Resource;
import java.util.List;

public class BloodRequestViewModel extends AndroidViewModel {

    private final MutableLiveData<Resource<List<BloodRequest>>> requests = new MutableLiveData<>();
    private final MutableLiveData<Resource<BloodRequest>> submitResult   = new MutableLiveData<>();

    public BloodRequestViewModel(@NonNull Application app) { super(app); }

    public LiveData<Resource<List<BloodRequest>>> getRequests()     { return requests; }
    public LiveData<Resource<BloodRequest>>       getSubmitResult() { return submitResult; }

    public void loadAll() {
        requests.setValue(Resource.loading());
        ApiClient.get(getApplication()).getBloodRequests().enqueue(new ApiCallback<>(requests));
    }

    public void loadMy() {
        requests.setValue(Resource.loading());
        ApiClient.get(getApplication()).myBloodRequests().enqueue(new ApiCallback<>(requests));
    }

    public void search(String bloodGroup, String district) {
        requests.setValue(Resource.loading());
        ApiClient.get(getApplication())
                .searchBloodRequests(bloodGroup, district)
                .enqueue(new ApiCallback<>(requests));
    }

    public void submit(BloodRequest req) {
        submitResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).createBloodRequest(req).enqueue(new ApiCallback<>(submitResult));
    }
}
