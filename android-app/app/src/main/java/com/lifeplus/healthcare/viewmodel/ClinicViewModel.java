package com.lifeplus.healthcare.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.lifeplus.healthcare.model.Clinic;
import com.lifeplus.healthcare.network.ApiClient;
import com.lifeplus.healthcare.util.ApiCallback;
import com.lifeplus.healthcare.util.Resource;
import java.util.List;

public class ClinicViewModel extends AndroidViewModel {
    private final MutableLiveData<Resource<List<Clinic>>> clinics      = new MutableLiveData<>();
    private final MutableLiveData<Resource<Clinic>>       saveResult   = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>>         deleteResult = new MutableLiveData<>();

    public ClinicViewModel(@NonNull Application app) { super(app); }
    public LiveData<Resource<List<Clinic>>> getClinics()     { return clinics; }
    public LiveData<Resource<Clinic>>       getSaveResult()  { return saveResult; }
    public LiveData<Resource<Void>>         getDeleteResult(){ return deleteResult; }

    public void loadAll() {
        clinics.setValue(Resource.loading());
        ApiClient.get(getApplication()).getClinics().enqueue(new ApiCallback<>(clinics));
    }

    /** No /clinics/my endpoint — falls back to loadAll() */
    public void loadMy() { loadAll(); }

    public void search(String district) {
        clinics.setValue(Resource.loading());
        ApiClient.get(getApplication()).searchClinics(district).enqueue(new ApiCallback<>(clinics));
    }

    public void create(Clinic c) {
        saveResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).createClinic(c).enqueue(new ApiCallback<>(saveResult));
    }

    public void update(long id, Clinic c) {
        saveResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).updateClinic(id, c).enqueue(new ApiCallback<>(saveResult));
    }

    public void delete(long id) {
        deleteResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).deleteClinic(id).enqueue(new ApiCallback<>(deleteResult));
    }
}
