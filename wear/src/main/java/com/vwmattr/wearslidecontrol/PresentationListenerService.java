package com.vwmattr.wearslidecontrol;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Matty on 7/20/14.
 */
public class PresentationListenerService extends WearableListenerService {

    private static final String TAG = PresentationListenerService.class.getSimpleName();

    private static final String EXTRA_INDEX = "pageIndex";
    private static final int ID_PRESENTATION = 999;
    private static final byte CONTROL_FWD_MSG = 0;
    private static final byte CONTROL_PREV_MSG = 1;

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    //Notification for Data items
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        
        for (DataEvent event : events) {
            Uri uri = event.getDataItem().getUri();
            if ("/state".equals(uri.getPath())) {
               if (event.getType() == DataEvent.TYPE_CHANGED) {
                   handleStateChanged(event);
               }
            }
        }
    }

    private void handleStateChanged(DataEvent event) {
        DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
        int currentIndex = dataMap.getInt(EXTRA_INDEX, -1);

        postNotification(currentIndex);

    }

    private void postNotification(int currentIndex) {
        Intent intent = new Intent(this, PresentationListenerService.class).setAction("advance");
        PendingIntent advanceIntent = PendingIntent.getService(getApplicationContext(),
                101, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("Page #" + Integer.toString(currentIndex))
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_launcher);
        NotificationCompat.WearableExtender extender = new NotificationCompat.WearableExtender();
        extender.addAction(new NotificationCompat.Action(android.R.drawable.ic_media_next,
                "advance", advanceIntent))
                .setContentAction(0)
                .setHintHideIcon(true);
        builder.extend(extender);
        manager.notify(ID_PRESENTATION, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ("advance".equals(intent.getAction())) {
            sendControlMessage(CONTROL_FWD_MSG);
        }
        return START_NOT_STICKY;
    }

    private void sendControlMessage(final byte command) {

        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                NodeApi.GetConnectedNodesResult nodes =
                        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();

                byte[] message = new byte[1];
                message[0] = command;
                for (Node node : nodes.getNodes()) {
                    Wearable.MessageApi.sendMessage(mGoogleApiClient,
                            node.getId(), "/control", message);
                    Log.i(TAG, "Sent Control message: "
                            + Byte.toString(command) + " to node: " + node.getId());
                }
                return null;
            }
        }.execute();
    }

}