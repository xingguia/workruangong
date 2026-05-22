package com.example.myapplication.api;

import android.content.Context;
import android.content.SharedPreferences;

public class ApiClient {

    private static final String BASE_URL = "http://10.0.2.2:8000/api/v1";
    private static final String PREF_NAME = "fitness_app_prefs";
    private static final String KEY_TOKEN = "auth_token";

    private static ApiClient instance;
    private SharedPreferences prefs;

    private ApiClient(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }

    public String getBaseUrl() {
        return BASE_URL;
    }

    public String getAuthToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void setAuthToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public void clearAuthToken() {
        prefs.edit().remove(KEY_TOKEN).apply();
    }

    public boolean isLoggedIn() {
        return getAuthToken() != null;
    }
}
