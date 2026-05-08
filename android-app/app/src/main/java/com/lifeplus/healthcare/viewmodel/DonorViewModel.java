package com.lifeplus.healthcare.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.lifeplus.healthcare.model.Donor;
import com.lifeplus.healthcare.network.ApiClient;
import com.lifeplus.healthcare.util.ApiCallback;
import com.lifeplus.healthcare.util.Resource;
import java.util.List;

public class DonorViewModel extends AndroidViewModel {
    private final MutableLiveData<Resource<List<Donor>>> donors       = new MutableLiveData<>();
    private final MutableLiveData<Resource<Donor>>       saveResult   = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>>        deleteResult = new MutableLiveData<>();

    public DonorViewModel(@NonNull Application app) { super(app); }

    public LiveData<Resource<List<Donor>>> getDonors()      { return donors; }
    public LiveData<Resource<Donor>>       getSaveResult()  { return saveResult; }
    public LiveData<Resource<Void>>        getDeleteResult(){ return deleteResult; }

    public void loadAll() {
        donors.setValue(Resource.loading());
        ApiClient.get(getApplication()).getDonors().enqueue(new ApiCallback<>(donors));
    }

    public void loadMy() {
        donors.setValue(Resource.loading());
        ApiClient.get(getApplication()).myDonors().enqueue(new ApiCallback<>(donors));
    }

    public void search(String bloodGroup, String district) {
        donors.setValue(Resource.loading());
        ApiClient.get(getApplication()).searchDonors(bloodGroup, district).enqueue(new ApiCallback<>(donors));
    }

    public void register(Donor donor) {
        saveResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).createDonor(donor).enqueue(new ApiCallback<>(saveResult));
    }

    public void update(long id, Donor donor) {
        saveResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).updateDonor(id, donor).enqueue(new ApiCallback<>(saveResult));
    }

    public void delete(long id) {
        deleteResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).deleteDonor(id).enqueue(new ApiCallback<>(deleteResult));
    }
}
