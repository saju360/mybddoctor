package com.lifeplus.healthcare.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.lifeplus.healthcare.model.BloodBank;
import com.lifeplus.healthcare.network.ApiClient;
import com.lifeplus.healthcare.util.ApiCallback;
import com.lifeplus.healthcare.util.Resource;
import java.util.List;

public class BloodBankViewModel extends AndroidViewModel {
    private final MutableLiveData<Resource<List<BloodBank>>> banks        = new MutableLiveData<>();
    private final MutableLiveData<Resource<BloodBank>>       saveResult   = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>>            deleteResult = new MutableLiveData<>();

    public BloodBankViewModel(@NonNull Application app) { super(app); }
    public LiveData<Resource<List<BloodBank>>> getBanks()       { return banks; }
    public LiveData<Resource<BloodBank>>       getSaveResult()  { return saveResult; }
    public LiveData<Resource<Void>>            getDeleteResult(){ return deleteResult; }

    public void loadAll() {
        banks.setValue(Resource.loading());
        ApiClient.get(getApplication()).getBloodBanks().enqueue(new ApiCallback<>(banks));
    }

    /** No /blood-banks/my endpoint — falls back to loadAll() */
    public void loadMy() { loadAll(); }

    public void search(String district) {
        banks.setValue(Resource.loading());
        ApiClient.get(getApplication()).searchBloodBanks(district).enqueue(new ApiCallback<>(banks));
    }

    public void create(BloodBank b) {
        saveResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).createBloodBank(b).enqueue(new ApiCallback<>(saveResult));
    }

    public void update(long id, BloodBank b) {
        saveResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).updateBloodBank(id, b).enqueue(new ApiCallback<>(saveResult));
    }

    public void delete(long id) {
        deleteResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).deleteBloodBank(id).enqueue(new ApiCallback<>(deleteResult));
    }
}
