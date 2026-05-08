package com.lifeplus.healthcare.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.lifeplus.healthcare.model.Appointment;
import com.lifeplus.healthcare.network.ApiClient;
import com.lifeplus.healthcare.util.ApiCallback;
import com.lifeplus.healthcare.util.Resource;
import java.util.List;

public class AppointmentViewModel extends AndroidViewModel {

    private final MutableLiveData<Resource<List<Appointment>>> appointments = new MutableLiveData<>();
    private final MutableLiveData<Resource<Appointment>> bookResult         = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> cancelResult              = new MutableLiveData<>();

    public AppointmentViewModel(@NonNull Application app) { super(app); }

    public LiveData<Resource<List<Appointment>>> getAppointments() { return appointments; }
    public LiveData<Resource<Appointment>>       getBookResult()   { return bookResult; }
    public LiveData<Resource<Void>>              getCancelResult() { return cancelResult; }

    public void loadMy() {
        appointments.setValue(Resource.loading());
        ApiClient.get(getApplication()).myAppointments().enqueue(new ApiCallback<>(appointments));
    }

    public void book(long doctorId, String date, String timeSlot, String notes) {
        Appointment appt = new Appointment();
        appt.doctorId        = doctorId;
        appt.appointmentDate = date;
        appt.timeSlot        = timeSlot;
        appt.notes           = notes;
        bookResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).bookAppointment(appt).enqueue(new ApiCallback<>(bookResult));
    }

    public void cancel(long id) {
        cancelResult.setValue(Resource.loading());
        ApiClient.get(getApplication()).cancelAppointment(id).enqueue(new ApiCallback<>(cancelResult));
    }
}
