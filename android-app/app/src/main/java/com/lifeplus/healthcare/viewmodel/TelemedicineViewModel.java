package com.lifeplus.healthcare.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.lifeplus.healthcare.model.TelemedicineSession;
import com.lifeplus.healthcare.network.ApiClient;
import com.lifeplus.healthcare.util.ApiCallback;
import com.lifeplus.healthcare.util.Resource;
import java.util.List;

public class TelemedicineViewModel extends AndroidViewModel {
    private final MutableLiveData<Resource<List<TelemedicineSession>>> sessions = new MutableLiveData<>();
    private final MutableLiveData<Resource<TelemedicineSession>> bookResult     = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> cancelResult                  = new MutableLiveData<>();

    public TelemedicineViewModel(@NonNull Application app) { super(app); }

    public LiveData<Resource<List<TelemedicineSession>>> getSessions()    { return sessions; }
    public LiveData<Resource<TelemedicineSession>>       getBookResult()  { return bookResult; }
    public LiveData<Resource<Void>>                      getCancelResult(){ return cancelResult; }

    public void loadMy() {
        sessions.setValue(Resource.loading());
        ApiClient.get(getApplication()).myTelemedicine().enqueue(new ApiCallback<>(sessions));
    }

    public void book(long doctorId, String scheduledAt, String notes) {
        TelemedicineSession s = new TelemedicineSession();
        s.doctorId    = doctorId;
        s.scheduledAt = scheduledAt;
        s.notes       = notes;
        bookResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).createTelemedicine(s).enqueue(new ApiCallback<>(bookResult));
    }

    public void cancel(long id) {
        cancelResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).cancelTelemedicine(id).enqueue(new ApiCallback<>(cancelResult));
    }
}
