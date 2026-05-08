package com.lifeplus.healthcare.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.lifeplus.healthcare.model.AuthModels;
import com.lifeplus.healthcare.network.ApiClient;
import com.lifeplus.healthcare.util.ApiCallback;
import com.lifeplus.healthcare.util.Resource;
import com.lifeplus.healthcare.util.SessionManager;
import retrofit2.Call;
import retrofit2.Response;

public class AuthViewModel extends AndroidViewModel {

    private final MutableLiveData<Resource<AuthModels.AuthResponse>> loginResult    = new MutableLiveData<>();
    private final MutableLiveData<Resource<AuthModels.AuthResponse>> registerResult = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application app) { super(app); }

    public LiveData<Resource<AuthModels.AuthResponse>> getLoginResult()    { return loginResult; }
    public LiveData<Resource<AuthModels.AuthResponse>> getRegisterResult() { return registerResult; }

    public void login(String phone, String password) {
        loginResult.setValue(Resource.loading());
        Application app = getApplication();
        ApiClient.get(app)
                .login(new AuthModels.LoginRequest(phone, password))
                .enqueue(new ApiCallback<AuthModels.AuthResponse>(loginResult) {
                    @Override
                    public void onResponse(Call<AuthModels.AuthResponse> call,
                                           Response<AuthModels.AuthResponse> response) {
                        super.onResponse(call, response);
                        // Save session on successful login
                        if (response.isSuccessful() && response.body() != null) {
                            AuthModels.AuthResponse body = response.body();
                            SessionManager.get(app).saveSession(
                                    body.accessToken,
                                    body.refreshToken,
                                    body.userId,
                                    body.role != null ? body.role : "USER"
                            );
                        }
                    }
                });
    }

    public void register(String fullName, String phone, String email,
                         String password, String lang) {
        registerResult.setValue(Resource.loading());
        ApiClient.get(getApplication())
                .register(new AuthModels.RegisterRequest(fullName, phone, email, password, lang))
                .enqueue(new ApiCallback<>(registerResult));
    }

    public void logout() {
        SessionManager.get(getApplication()).clearSession();
        ApiClient.reset();
    }
}
