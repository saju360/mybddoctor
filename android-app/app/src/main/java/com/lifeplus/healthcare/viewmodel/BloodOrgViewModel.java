package com.lifeplus.healthcare.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.lifeplus.healthcare.model.BloodOrganization;
import com.lifeplus.healthcare.network.ApiClient;
import com.lifeplus.healthcare.util.ApiCallback;
import com.lifeplus.healthcare.util.Resource;
import java.util.List;

public class BloodOrgViewModel extends AndroidViewModel {
    private final MutableLiveData<Resource<List<BloodOrganization>>> orgs        = new MutableLiveData<>();
    private final MutableLiveData<Resource<BloodOrganization>>       saveResult  = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>>                    deleteResult= new MutableLiveData<>();

    public BloodOrgViewModel(@NonNull Application app) { super(app); }
    public LiveData<Resource<List<BloodOrganization>>> getOrgs()        { return orgs; }
    public LiveData<Resource<BloodOrganization>>       getSaveResult()  { return saveResult; }
    public LiveData<Resource<Void>>                    getDeleteResult(){ return deleteResult; }

    public void loadAll() {
        orgs.setValue(Resource.loading());
        ApiClient.get(getApplication()).getBloodOrgs().enqueue(new ApiCallback<>(orgs));
    }

    /** No /blood-organizations/my endpoint — falls back to loadAll() */
    public void loadMy() { loadAll(); }

    public void search(String district) {
        orgs.setValue(Resource.loading());
        ApiClient.get(getApplication()).searchBloodOrgs(district).enqueue(new ApiCallback<>(orgs));
    }

    public void create(BloodOrganization org) {
        saveResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).createBloodOrg(org).enqueue(new ApiCallback<>(saveResult));
    }

    public void update(long id, BloodOrganization org) {
        saveResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).updateBloodOrg(id, org).enqueue(new ApiCallback<>(saveResult));
    }

    public void delete(long id) {
        deleteResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).deleteBloodOrg(id).enqueue(new ApiCallback<>(deleteResult));
    }
}
