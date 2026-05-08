package com.lifeplus.healthcare.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.lifeplus.healthcare.model.Pharmacy;
import com.lifeplus.healthcare.network.ApiClient;
import com.lifeplus.healthcare.util.ApiCallback;
import com.lifeplus.healthcare.util.Resource;
import java.util.List;

public class PharmacyViewModel extends AndroidViewModel {
    private final MutableLiveData<Resource<List<Pharmacy>>> pharmacies  = new MutableLiveData<>();
    private final MutableLiveData<Resource<Pharmacy>>       saveResult  = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>>           deleteResult= new MutableLiveData<>();

    public PharmacyViewModel(@NonNull Application app) { super(app); }
    public LiveData<Resource<List<Pharmacy>>> getPharmacies()  { return pharmacies; }
    public LiveData<Resource<Pharmacy>>       getSaveResult()  { return saveResult; }
    public LiveData<Resource<Void>>           getDeleteResult(){ return deleteResult; }

    public void loadAll() {
        pharmacies.setValue(Resource.loading());
        ApiClient.get(getApplication()).getPharmacies().enqueue(new ApiCallback<>(pharmacies));
    }

    /** No /pharmacies/my endpoint — falls back to loadAll() */
    public void loadMy() { loadAll(); }

    public void search(String district, boolean open24h) {
        pharmacies.setValue(Resource.loading());
        ApiClient.get(getApplication()).searchPharmacies(district, open24h).enqueue(new ApiCallback<>(pharmacies));
    }

    public void create(Pharmacy p) {
        saveResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).createPharmacy(p).enqueue(new ApiCallback<>(saveResult));
    }

    public void update(long id, Pharmacy p) {
        saveResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).updatePharmacy(id, p).enqueue(new ApiCallback<>(saveResult));
    }

    public void delete(long id) {
        deleteResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).deletePharmacy(id).enqueue(new ApiCallback<>(deleteResult));
    }
}
