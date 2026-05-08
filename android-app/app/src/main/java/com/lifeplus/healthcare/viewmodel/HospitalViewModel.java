package com.lifeplus.healthcare.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.lifeplus.healthcare.model.Hospital;
import com.lifeplus.healthcare.network.ApiClient;
import com.lifeplus.healthcare.util.ApiCallback;
import com.lifeplus.healthcare.util.Resource;
import java.util.List;

public class HospitalViewModel extends AndroidViewModel {
    private final MutableLiveData<Resource<List<Hospital>>> hospitals = new MutableLiveData<>();
    private final MutableLiveData<Resource<Hospital>>       saveResult   = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>>           deleteResult = new MutableLiveData<>();

    public HospitalViewModel(@NonNull Application app) { super(app); }

    public LiveData<Resource<List<Hospital>>> getHospitals()    { return hospitals; }
    public LiveData<Resource<Hospital>>       getSaveResult()   { return saveResult; }
    public LiveData<Resource<Void>>           getDeleteResult() { return deleteResult; }

    public void loadAll() {
        hospitals.setValue(Resource.loading());
        ApiClient.get(getApplication()).getHospitals().enqueue(new ApiCallback<>(hospitals));
    }

    /** loadMy() — for owner-only view, falls back to loadAll() since backend
     *  doesn't have a /hospitals/my endpoint. Filter client-side by userId. */
    public void loadMy() { loadAll(); }

    public void search(String district) {
        hospitals.setValue(Resource.loading());
        ApiClient.get(getApplication()).searchHospitals(district).enqueue(new ApiCallback<>(hospitals));
    }

    public void create(Hospital h) {
        saveResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).createHospital(h).enqueue(new ApiCallback<>(saveResult));
    }

    public void update(long id, Hospital h) {
        saveResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).updateHospital(id, h).enqueue(new ApiCallback<>(saveResult));
    }

    public void delete(long id) {
        deleteResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).deleteHospital(id).enqueue(new ApiCallback<>(deleteResult));
    }
}
