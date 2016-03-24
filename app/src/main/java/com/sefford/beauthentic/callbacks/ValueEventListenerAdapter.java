package com.sefford.beauthentic.callbacks;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

/**
 * ValueEventListenr adapter
 *
 * @author Saúl Díaz González <sefford@gmail.com>
 */
public class ValueEventListenerAdapter implements ValueEventListener {
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onCancelled(FirebaseError firebaseError) {

    }
}
