package com.example.hemuc_000.photogallery;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.List;

/**
 * Created by hemuc_000 on 7/20/2016.
 */
public class PollService extends IntentService {
    private static final String TAG="PollService";
    private static final int POLL_INTERVAL=1000*60; //60  seconds
    public static final String ACTION_SHOW_NOTIFICATION="com.bignerdranch.android.photogallery.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE=
        "com.example.hemuc_000.photogallery.PRIVATE";
    public static final String REQUEST_CODE="REQUEST_CODE";
    public static final String NOTIFICATION="NOTIFICATION";
    public static Intent newIntent(Context context){
        return new Intent(context,PollService.class);

    }
    public PollService(){
        super(TAG);
    }
    public static boolean isServiceAlarmOn(Context context){
        Intent i =PollService.newIntent(context);
        PendingIntent pendingIntent=PendingIntent.getService(context,0,i,PendingIntent.FLAG_NO_CREATE);
        return pendingIntent!=null;
    }
    public static void setServiceAlarm(Context c, boolean isOn){
        Intent i =PollService.newIntent(c);
        PendingIntent pendingIntent=PendingIntent.getService(c, 0, i, 0);

        AlarmManager alarmManager=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);

        if(isOn){
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),POLL_INTERVAL,pendingIntent);
        }else{
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
        QueryPreferences.setAlarmOn(c,isOn);
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        if(!isNetworkAvailableAndConnected()){
            return;
        }
        List<GalleryItem> items;
        String query=QueryPreferences.getStoredQuery(this);
        String lastResultID=QueryPreferences.getLastResultID(this);

        if(query==null){
            items= new FlickrFetchr().fetchRecentPhotos();
        }else{
            items=new FlickrFetchr().searchPhotos(query);
        }
        if(items.size()==0){
            return;
        }
        String resultID=items.get(0).toString();
        if(resultID.equals(lastResultID)){
            Log.i(TAG, "Got an old result: " + resultID);
        }else{
            Log.i(TAG,"Got a new result: "+resultID);

            Resources resources =getResources();
            Intent i =PhotoGalleryActivity.newIntent(this);
            PendingIntent pendingIntent=PendingIntent.getActivity(this, 0, i, 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(resources.getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resources.getString(R.string.new_pictures_title))
                    .setContentText(resources.getString(R.string.new_pictures_text))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();
            showBackgroundNotification(0,notification);
        }
        QueryPreferences.setLastResultID(this,resultID);
        Log.i(TAG, "Received an Intent: " + intent);
    }
    private void showBackgroundNotification(int requestCode,Notification notification){
        Intent intent=new Intent(ACTION_SHOW_NOTIFICATION);
        intent.putExtra(REQUEST_CODE,requestCode);
        intent.putExtra(NOTIFICATION,notification);
        sendOrderedBroadcast(intent,PERM_PRIVATE,null,null, Activity.RESULT_OK,null,null);
    }

    private boolean isNetworkAvailableAndConnected(){
        ConnectivityManager connectivityManager=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable=connectivityManager.getActiveNetworkInfo()!=null;
        boolean isNetworkConnected=isNetworkAvailable&&connectivityManager.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }
}
