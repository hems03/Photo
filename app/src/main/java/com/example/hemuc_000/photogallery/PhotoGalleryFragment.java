package com.example.hemuc_000.photogallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.hemuc_000.photogallery.GalleryResponse.PhotosBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hemuc_000 on 7/14/2016.
 */
public class PhotoGalleryFragment extends VisibleFragment {
    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem>mItems= new ArrayList<>();
    private static final String TAG="PhotoGalleryFragment";
    private ThumbnailDownloader <PhotoHolder> mThumbnailDownloader;
    private  MenuItem mProgressItem;
    private static int mPage=0;
    public static PhotoGalleryFragment newInstance(){
        return new PhotoGalleryFragment();
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);


        Intent i =PollService.newIntent(getActivity());
        getActivity().startService(i);



        Handler responseHandler=new Handler();
        mThumbnailDownloader=new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setThumbnailDownloadListener(
                new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
                    @Override
                    public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail) {
                        Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                        target.bindDrawable(drawable);
                    }
                }
        );
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        updateItems();
        Log.i(TAG, "Background thread started");


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);
         mProgressItem=menu.findItem(R.id.menu_item_progress_bar);
        ProgressBar progressBar=(ProgressBar)mProgressItem.getActionView();
        MenuItem searchItem=menu.findItem(R.id.menu_item_search);
        final SearchView searchView=(SearchView)searchItem.getActionView();

        MenuItem pollItem=menu.findItem(R.id.menu_item_toggle_poll);
        if(PollService.isServiceAlarmOn(getActivity())){
            pollItem.setTitle(R.string.stop_polling);
        }else{
            pollItem.setTitle(R.string.start_polling);
        }
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "QueryTextSubmit" + query);

                QueryPreferences.setStoredQuery(getActivity(), query);
                searchView.clearFocus();

                updateItems();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "Query text changed: " + newText);

                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);
                updateItems();
                return true;
            case R.id.menu_item_toggle_poll:
                boolean startAlarm=!PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(),startAlarm);
                System.out.println("nadge it up");
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateItems(){
        String query=QueryPreferences.getStoredQuery(getActivity());
        //mProgressItem.setVisible(true);
        new FetchItemsTask(query,mPage).execute();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG,"Background Thread Destroyed");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    private class ScrollListener extends RecyclerView.OnScrollListener{
        private boolean canScrollDown=true;
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if(canScrollDown==false&&newState==0){
                mPage++;
                updateItems();
            }


        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if(!recyclerView.canScrollVertically(1)){
                canScrollDown=false;
                Log.i(TAG, "Reached the end");


            }
        }
    }
    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView mTitleImagevView;
        private GalleryItem mGalleryItem;

        @Override
        public void onClick(View v) {
            Intent i =new Intent(Intent.ACTION_VIEW,mGalleryItem.getPhotoPageUri());
            startActivity(i);
        }

        public PhotoHolder(View itemView){
            super(itemView);
            mTitleImagevView=(ImageView)itemView.findViewById(R.id.fragment_photo_gallery_image_view);
            itemView.setOnClickListener(this);
        }
        public void bindGalleryItem(GalleryItem galleryItem){
            mGalleryItem=galleryItem;
        }

        public void bindDrawable(Drawable drawable){
            mTitleImagevView.setImageDrawable(drawable);

        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{

        private List<GalleryItem>mGalleryItems;
        public PhotoAdapter(List<GalleryItem> galleryItems){
            mGalleryItems=galleryItems;
        }
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup, int i)  {
            LayoutInflater inflater=LayoutInflater.from(getActivity());
            View view =inflater.inflate(R.layout.gallery_item,viewGroup,false);
            return new PhotoHolder(view);

        }

        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int i) {
            GalleryItem galleryItem=mGalleryItems.get(i);
            Drawable placeHolder=getResources().getDrawable(R.drawable.nadge);
            photoHolder.bindDrawable(placeHolder);
            photoHolder.bindGalleryItem(galleryItem);
            mThumbnailDownloader.queueThumbnail(photoHolder,galleryItem.getURL());
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_photo_gallery,container,false);
        mPhotoRecyclerView=(RecyclerView)v.findViewById(R.id.fragment_photo_gallery_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),4));


        return v;

    }
    private void setupAdapter(){
        if(isAdded()){
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
            mPhotoRecyclerView.setOnScrollListener(new ScrollListener());

        }
    }
    private class FetchItemsTask extends AsyncTask<Void,Void,List<GalleryItem>> {
        private String mQuery;
        private int mPage;

        public FetchItemsTask(String query,int page){
            mQuery=query;
        }
        public FetchItemsTask(int page){
            mPage=page;
        }
        @Override
        protected List<GalleryItem> doInBackground(Void... params) {


            if(mQuery==null){
                return new FlickrFetchr(mPage).fetchRecentPhotos();
            }else{
                return new FlickrFetchr(mPage).searchPhotos(mQuery);
            }

        }


        protected void onPostExecute(List<GalleryItem> items) {
            mItems=items;
            mProgressItem.setVisible(false);
            setupAdapter();
        }
    }
}
