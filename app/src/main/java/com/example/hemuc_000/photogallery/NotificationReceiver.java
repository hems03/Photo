package com.example.hemuc_000.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

/**
 * Created by hemuc_000 on 7/21/2016.
 */
public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG="NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"received result: "+getResultCode());
        if(getResultCode()!= Activity.RESULT_OK){
            return;
        }
        int requestCode=intent.getIntExtra(PollService.REQUEST_CODE,0);
        Notification notification=(Notification)
                intent.getParcelableExtra(PollService.NOTIFICATION);

        NotificationManagerCompat notificationManagerCompat=NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(requestCode,notification);
    }
}
