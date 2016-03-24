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
import com.sefford.beauthentic.model.SyncPayload;

/**
 * Service that notifies GCM that the clients would need to sync
 *
 * @author Saúl Díaz González <sefford@gmail.com>
 */
public class NotifySyncService extends GcmNotificationService {

    private static final String TAG = "NotifySyncService";

    public static final String EXTRA_SYNC = "extra_sync";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public NotifySyncService() {
        super(TAG);
    }

    @NonNull
    @Override
    protected GCMBody getPayload(String token, Intent intent) {
        return new GCMBody(token, new SyncPayload());
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
