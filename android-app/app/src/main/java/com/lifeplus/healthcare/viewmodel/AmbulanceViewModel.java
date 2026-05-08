package com.lifeplus.healthcare.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.lifeplus.healthcare.model.Ambulance;
import com.lifeplus.healthcare.network.ApiClient;
import com.lifeplus.healthcare.util.ApiCallback;
import com.lifeplus.healthcare.util.Resource;
import java.util.List;

public class AmbulanceViewModel extends AndroidViewModel {
    private final MutableLiveData<Resource<List<Ambulance>>> ambulances = new MutableLiveData<>();
    private final MutableLiveData<Resource<Ambulance>> saveResult       = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>>      deleteResult     = new MutableLiveData<>();

    public AmbulanceViewModel(@NonNull Application app) { super(app); }
    public LiveData<Resource<List<Ambulance>>> getAmbulances()  { return ambulances; }
    public LiveData<Resource<Ambulance>>       getSaveResult()  { return saveResult; }
    public LiveData<Resource<Void>>            getDeleteResult(){ return deleteResult; }

    public void loadAll() {
        ambulances.setValue(Resource.loading());
        ApiClient.get(getApplication()).getAmbulances().enqueue(new ApiCallback<>(ambulances));
    }

    /** No /ambulances/my endpoint — falls back to loadAll() */
    public void loadMy() { loadAll(); }

    public void search(String district, boolean availableOnly) {
        ambulances.setValue(Resource.loading());
        ApiClient.get(getApplication()).searchAmbulances(district, availableOnly).enqueue(new ApiCallback<>(ambulances));
    }

    public void create(Ambulance a) {
        saveResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).createAmbulance(a).enqueue(new ApiCallback<>(saveResult));
    }

    public void update(long id, Ambulance a) {
        saveResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).updateAmbulance(id, a).enqueue(new ApiCallback<>(saveResult));
    }

    public void delete(long id) {
        deleteResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).deleteAmbulance(id).enqueue(new ApiCallback<>(deleteResult));
    }
}
