package com.lifeplus.healthcare.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.lifeplus.healthcare.model.HealthRecord;
import com.lifeplus.healthcare.network.ApiClient;
import com.lifeplus.healthcare.util.ApiCallback;
import com.lifeplus.healthcare.util.Resource;
import java.util.List;

public class HealthRecordViewModel extends AndroidViewModel {

    private final MutableLiveData<Resource<List<HealthRecord>>> records      = new MutableLiveData<>();
    private final MutableLiveData<Resource<HealthRecord>>       saveResult   = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>>               deleteResult = new MutableLiveData<>();

    public HealthRecordViewModel(@NonNull Application app) { super(app); }

    public LiveData<Resource<List<HealthRecord>>> getRecords()    { return records; }
    public LiveData<Resource<HealthRecord>>       getSaveResult() { return saveResult; }
    public LiveData<Resource<Void>>               getDeleteResult(){ return deleteResult; }

    public void loadMy() {
        records.setValue(Resource.loading());
        ApiClient.get(getApplication()).myHealthRecords().enqueue(new ApiCallback<>(records));
    }

    public void add(String type, String data, String date, String doctor, String facility) {
        HealthRecord r = new HealthRecord();
        r.recordType   = type;
        r.recordData   = data;
        r.recordDate   = date;
        r.doctorName   = doctor;
        r.facilityName = facility;
        saveResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).createHealthRecord(r).enqueue(new ApiCallback<>(saveResult));
    }

    public void update(long id, HealthRecord r) {
        saveResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).updateHealthRecord(id, r).enqueue(new ApiCallback<>(saveResult));
    }

    public void delete(long id) {
        deleteResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).deleteHealthRecord(id).enqueue(new ApiCallback<>(deleteResult));
    }
}
