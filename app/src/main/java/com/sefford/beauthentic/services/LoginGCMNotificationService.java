package com.sefford.beauthentic.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.sefford.beauthentic.model.GCMBody;
import com.sefford.beauthentic.model.Payload;
import com.sefford.beauthentic.networking.GCMApi;
import com.sefford.beauthentic.utils.GCMUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by sefford on 23/03/16.
 */
public class LoginGCMNotificationService extends IntentService {

    private static final String TAG = "GCMLogin";

    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_PASSWORD = "extra_password";
    public static final String EXTRA_TYPE = "extra_type";
    public static final String EXTRA_AUTHTOKEN = "extra_authtoken";
    public static final String EXTRA_DEVICES = "extra_devices";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public LoginGCMNotificationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        final GCMApi api = intializeApi();
        final String currentDevice = GCMUtils.getGCMToken(this);

        for (String token : intent.getStringArrayListExtra(EXTRA_DEVICES)) {
            if (!token.equals(currentDevice)) {
                try {
                    final retrofit2.Response<Object> response = api.notifyLogin(new GCMBody(token, new Payload(intent.getIntExtra(EXTRA_TYPE, 0),
                            intent.getStringExtra(EXTRA_NAME),
                            intent.getStringExtra(EXTRA_PASSWORD),
                            intent.getStringExtra(EXTRA_AUTHTOKEN)))).execute();
                    Log.d(TAG, "To:" + token.substring(0, 10) + " Status:" + response.code() + " Message:" + response.message());
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }
    }

    @NonNull
    private GCMApi intializeApi() {
        final HttpLoggingInterceptor loggerInterceptor = new HttpLoggingInterceptor();
        loggerInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        final OkHttpClient.Builder builder =
                new OkHttpClient.Builder()
                        .addNetworkInterceptor(new Interceptor() {
                            @Override
                            public Response intercept(Chain chain) throws IOException {
                                final Request request = chain.request();
                                final Request newRequest = request.newBuilder()
                                        .addHeader("Authorization", "key=AIzaSyD5kZw9Bi0mRTBj3hWJ9zvZO1_iy1Xe-dI")
                                        .addHeader("Content-Type", "application/json")
                                        .build();
                                return chain.proceed(newRequest);
                            }
                        }).addNetworkInterceptor(loggerInterceptor);
        return new Retrofit.Builder()
                .baseUrl(GCMApi.GCM_ENDPOINT)
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(GCMApi.class);
    }
}
