package com.sefford.beauthentic.model;

import com.google.gson.annotations.SerializedName;
import com.sefford.beauthentic.services.NotifySyncService;

/**
 * Payload to execute the Sync request
 *
 * @author Saúl Díaz González <sefford@gmail.com>
 */
public class SyncPayload {

    @SerializedName(NotifySyncService.EXTRA_SYNC)
    final String sync;

    public SyncPayload() {
        sync = "sync_plz";
    }
}
