package com.lifeplus.healthcare.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.lifeplus.healthcare.model.DiagnosticCenter;
import com.lifeplus.healthcare.network.ApiClient;
import com.lifeplus.healthcare.util.ApiCallback;
import com.lifeplus.healthcare.util.Resource;
import java.util.List;

public class DiagnosticViewModel extends AndroidViewModel {
    private final MutableLiveData<Resource<List<DiagnosticCenter>>> centers     = new MutableLiveData<>();
    private final MutableLiveData<Resource<DiagnosticCenter>>       saveResult  = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>>                   deleteResult= new MutableLiveData<>();

    public DiagnosticViewModel(@NonNull Application app) { super(app); }
    public LiveData<Resource<List<DiagnosticCenter>>> getCenters()     { return centers; }
    public LiveData<Resource<DiagnosticCenter>>       getSaveResult()  { return saveResult; }
    public LiveData<Resource<Void>>                   getDeleteResult(){ return deleteResult; }

    public void loadAll() {
        centers.setValue(Resource.loading());
        ApiClient.get(getApplication()).getDiagnostics().enqueue(new ApiCallback<>(centers));
    }

    /** No /diagnostics/my endpoint — falls back to loadAll() */
    public void loadMy() { loadAll(); }

    public void search(String district, String test) {
        centers.setValue(Resource.loading());
        ApiClient.get(getApplication()).searchDiagnostics(district, test).enqueue(new ApiCallback<>(centers));
    }

    public void create(DiagnosticCenter d) {
        saveResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).createDiagnostic(d).enqueue(new ApiCallback<>(saveResult));
    }

    public void update(long id, DiagnosticCenter d) {
        saveResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).updateDiagnostic(id, d).enqueue(new ApiCallback<>(saveResult));
    }

    public void delete(long id) {
        deleteResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).deleteDiagnostic(id).enqueue(new ApiCallback<>(deleteResult));
    }
}
