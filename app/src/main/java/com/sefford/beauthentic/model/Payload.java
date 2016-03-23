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
package com.sefford.beauthentic.model;

import com.google.gson.annotations.SerializedName;
import com.sefford.beauthentic.services.LoginGCMNotificationService;

/**
 * Created by sefford on 23/03/16.
 */
public class Payload {

    @SerializedName(LoginGCMNotificationService.EXTRA_TYPE)
    final int type;
    @SerializedName(LoginGCMNotificationService.EXTRA_NAME)
    final String name;
    @SerializedName(LoginGCMNotificationService.EXTRA_PASSWORD)
    final String password;
    @SerializedName(LoginGCMNotificationService.EXTRA_AUTHTOKEN)
    final String authtoken;


    public Payload(int type, String name, String password, String authtoken) {
        this.type = type;
        this.name = name;
        this.password = password;
        this.authtoken = authtoken;
    }
}
