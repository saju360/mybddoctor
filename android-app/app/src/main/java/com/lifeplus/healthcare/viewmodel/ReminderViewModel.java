package com.lifeplus.healthcare.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.lifeplus.healthcare.model.MedicineReminder;
import com.lifeplus.healthcare.network.ApiClient;
import com.lifeplus.healthcare.util.ApiCallback;
import com.lifeplus.healthcare.util.Resource;
import java.util.List;

public class ReminderViewModel extends AndroidViewModel {

    private final MutableLiveData<Resource<List<MedicineReminder>>> reminders    = new MutableLiveData<>();
    private final MutableLiveData<Resource<MedicineReminder>>       saveResult   = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>>                   deleteResult = new MutableLiveData<>();

    public ReminderViewModel(@NonNull Application app) { super(app); }

    public LiveData<Resource<List<MedicineReminder>>> getReminders()   { return reminders; }
    public LiveData<Resource<MedicineReminder>>       getSaveResult()  { return saveResult; }
    public LiveData<Resource<Void>>                   getDeleteResult(){ return deleteResult; }

    public void loadAll() {
        reminders.setValue(Resource.loading());
        ApiClient.get(getApplication()).myReminders().enqueue(new ApiCallback<>(reminders));
    }

    public void loadActive() {
        reminders.setValue(Resource.loading());
        ApiClient.get(getApplication()).myActiveReminders().enqueue(new ApiCallback<>(reminders));
    }

    public void add(String name, String time, String dosage, String frequency) {
        MedicineReminder r = new MedicineReminder();
        r.medicineName = name;
        r.reminderTime = time;
        r.dosage       = dosage;
        r.frequency    = frequency;
        r.active       = true;
        saveResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).createReminder(r).enqueue(new ApiCallback<>(saveResult));
    }

    public void update(long id, MedicineReminder r) {
        saveResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).updateReminder(id, r).enqueue(new ApiCallback<>(saveResult));
    }

    public void toggleActive(long id, MedicineReminder existing) {
        existing.active = !existing.active;
        update(id, existing);
    }

    public void delete(long id) {
        deleteResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).deleteReminder(id).enqueue(new ApiCallback<>(deleteResult));
    }
}
