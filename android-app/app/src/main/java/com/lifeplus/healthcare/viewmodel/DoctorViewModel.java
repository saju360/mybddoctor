package com.lifeplus.healthcare.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.lifeplus.healthcare.model.Doctor;
import com.lifeplus.healthcare.network.ApiClient;
import com.lifeplus.healthcare.util.ApiCallback;
import com.lifeplus.healthcare.util.Resource;
import java.util.List;

public class DoctorViewModel extends AndroidViewModel {

    private final MutableLiveData<Resource<List<Doctor>>> doctors     = new MutableLiveData<>();
    private final MutableLiveData<Resource<Doctor>>       saveResult  = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>>         deleteResult= new MutableLiveData<>();

    public DoctorViewModel(@NonNull Application app) { super(app); }

    public LiveData<Resource<List<Doctor>>> getDoctors()     { return doctors; }
    public LiveData<Resource<Doctor>>       getSaveResult()  { return saveResult; }
    public LiveData<Resource<Void>>         getDeleteResult(){ return deleteResult; }

    public void loadAll() {
        doctors.setValue(Resource.loading());
        ApiClient.get(getApplication()).getDoctors().enqueue(new ApiCallback<>(doctors));
    }

    /** No /doctors/my endpoint — falls back to loadAll() */
    public void loadMy() { loadAll(); }

    public void search(String specialty, Boolean telemedicine) {
        doctors.setValue(Resource.loading());
        ApiClient.get(getApplication()).searchDoctors(specialty, telemedicine).enqueue(new ApiCallback<>(doctors));
    }

    public void create(Doctor d) {
        saveResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).createDoctor(d).enqueue(new ApiCallback<>(saveResult));
    }

    public void update(long id, Doctor d) {
        saveResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).updateDoctor(id, d).enqueue(new ApiCallback<>(saveResult));
    }

    public void delete(long id) {
        deleteResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).deleteDoctor(id).enqueue(new ApiCallback<>(deleteResult));
    }
}
