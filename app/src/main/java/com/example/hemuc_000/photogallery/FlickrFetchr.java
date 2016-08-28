package com.example.hemuc_000.photogallery;


import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hemuc_000 on 7/14/2016.
 */
public class FlickrFetchr {
    private static final String TAG="FlickrFetcher";
    private static final String API_KEY="e22b3d77570ca74f9ae24499638c5a8c";
    private static final String FETCH_RECENTS_METHOD="flickr.photos.getRecent";
    private static final String SEARCH_METHOD="flickr.photos.search";
    private static int mPage;
    private static final Uri ENDPOINT=Uri
            .parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key",API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("page", Integer.toString(mPage))
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras","url_s")
            .build();


    List<GalleryItem>items = new ArrayList<>();
    GalleryResponse mGalleryResponse;

    public FlickrFetchr(int page){
        mPage=page;
    }
    public FlickrFetchr(){

    }

    public byte[] getUrlBytes (String urlSpec) throws IOException{
        URL url  =new URL(urlSpec);
        HttpURLConnection connection= (HttpURLConnection)url.openConnection();

        try{
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            InputStream inputStream=connection.getInputStream();

            if(connection.getResponseCode()!=HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage()+": with "+urlSpec);
            }

            int bytesRead=0;
            byte[] buffer=new byte[1024];
            while((bytesRead= inputStream.read(buffer))>0){
                out.write(buffer,0,bytesRead);
            };
            out.close();
            return out.toByteArray();

        }finally {
            connection.disconnect();
        }

    }

    public String getUrlString(String urlSpec) throws IOException{
        return new String(getUrlBytes(urlSpec));
    }
    public List<GalleryItem> downloadGalleryItems(String url){
        try{

            String jsonString =getUrlString(url);
            JSONObject jsonBody= new JSONObject(jsonString);
            parseItems(items,jsonBody);

            Log.i(TAG,"Received JSON: "+jsonString);
        }catch (IOException ioe){
            Log.e(TAG,"Failed to fetch");
        }catch (JSONException je){

        }
        return items;
    }
    private String buildURL(String method, String query){
        Uri.Builder uriBuilder=ENDPOINT.buildUpon()
                .appendQueryParameter("method",method);
        if (method.equals(SEARCH_METHOD)){
            uriBuilder.appendQueryParameter("text",query);

        }
        return uriBuilder.build().toString();
    }

    public List <GalleryItem> fetchRecentPhotos(){
        String url =buildURL(FETCH_RECENTS_METHOD,null);
        return downloadGalleryItems(url);
    }
    public List<GalleryItem> searchPhotos(String query){
        String url=buildURL(SEARCH_METHOD,query);
        return downloadGalleryItems(url);
    }

    private void parseItems(List<GalleryItem> items,JSONObject jsonBody)throws IOException,JSONException{
        JSONObject photosJSONObject=jsonBody.getJSONObject("photos");
        JSONArray photoJSONArray=photosJSONObject.getJSONArray("photo");

            for (int i = 0; i < photoJSONArray.length(); i++) {
                JSONObject photoJsonObject = photoJSONArray.getJSONObject(i);

                GalleryItem item = new GalleryItem();
                item.setID(photoJsonObject.getString("id"));
                item.setCaption(photoJsonObject.getString("title"));
                item.setOwner(photoJsonObject.getString("owner"));

                if (!photoJsonObject.has("url_s")) {
                    continue;
                }
                item.setURL(photoJsonObject.getString("url_s"));
                items.add(item);
            }



    }


}
