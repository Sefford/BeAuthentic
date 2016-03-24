/*
 * Copyright (C) 2016 Saúl Díaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sefford.beauthentic.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.sefford.beauthentic.model.GCMBody;
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
 * Service that launches requests to GCM Server
 *
 * @author Saúl Díaz González <sefford@gmail.com>
 */
public abstract class GcmNotificationService extends IntentService {
    public static final String EXTRA_DEVICES = "extra_devices";

    public GcmNotificationService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        final GCMApi api = intializeApi();
        final String currentDevice = GCMUtils.getGCMToken(this);

        for (String token : intent.getStringArrayListExtra(EXTRA_DEVICES)) {
            if (!token.equals(currentDevice)) {
                try {
                    final retrofit2.Response<Object> response = api.notifyLogin(getPayload(token, intent)).execute();
                    Log.d(getTag(), "To:" + token.substring(0, 10) + " Status:" + response.code() + " Message:" + response.message());
                } catch (IOException e) {
                    Log.e(getTag(), e.getMessage(), e);
                }
            }
        }
    }

    @NonNull
    protected abstract GCMBody getPayload(String token, Intent intent);

    protected abstract String getTag();

    @NonNull
    GCMApi intializeApi() {
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
