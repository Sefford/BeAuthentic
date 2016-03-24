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
package com.sefford.beauthentic.utils;

/**
 * Constants package
 */
public class Constants {

    public static final String FIREBASE_URL = "https://torrid-inferno-7798.firebaseio.com/";
    public static final String FIREBASE_USER_URL = FIREBASE_URL + "users/";

    public static final String SENT_TOKEN_TO_SERVER = "token_sent";

    public static final String REGISTRATION_COMPLETED_ACTION = "com.sefford.beauthentic.REGISTRATION_COMPLETED";

    private Constants() {
    }

}
