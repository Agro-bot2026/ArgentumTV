package com.labolilla.argentumtv;

import android.content.Context;

import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;

import java.util.List;

public class CastOptionsProvider implements OptionsProvider {

    @Override
    public CastOptions getCastOptions(Context context) {
        return new CastOptions.Builder()
                .setReceiverApplicationId("3B323644")
                .setStopReceiverApplicationWhenEndingSession(true)
                .setResumeSavedSession(true)
                .setEnableReconnectionService(true)
                .setLaunchOptions(null)
                .setCastMediaOptions(null)
                .build();
    }

    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context context) {
        return null;
    }
}
