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

import android.content.Intent;
import android.support.annotation.NonNull;

import com.sefford.beauthentic.model.GCMBody;
import com.sefford.beauthentic.model.LoginPayload;

/**
 * Created by sefford on 23/03/16.
 */
public class LoginGCMNotificationService extends GcmNotificationService {

    private static final String TAG = "GCMLogin";

    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_PASSWORD = "extra_password";
    public static final String EXTRA_TYPE = "extra_type";
    public static final String EXTRA_AUTHTOKEN = "extra_authtoken";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public LoginGCMNotificationService() {
        super(TAG);
    }

    @NonNull
    @Override
    protected GCMBody getPayload(String token, Intent intent) {
        return new GCMBody(token, new LoginPayload(intent.getIntExtra(EXTRA_TYPE, 0),
                intent.getStringExtra(EXTRA_NAME),
                intent.getStringExtra(EXTRA_PASSWORD),
                intent.getStringExtra(EXTRA_AUTHTOKEN)));
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
