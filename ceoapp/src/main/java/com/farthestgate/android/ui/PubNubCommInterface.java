package com.farthestgate.android.ui;

import com.pubnub.api.PubNubException;

/**
 * Created by Hanson on 28/07/2014.
 */
public interface PubNubCommInterface {
    void OnPubNubSendError(PubNubException e);
    void OnPubNubSendError(Exception e);
}
