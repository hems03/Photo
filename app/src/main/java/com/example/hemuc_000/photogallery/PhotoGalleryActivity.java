package com.example.hemuc_000.photogallery;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class PhotoGalleryActivity extends SimpleFragmentActivity{
    public static Intent newIntent(Context c){
        return new Intent(c,PhotoGalleryActivity.class);
    }
    @Override
    protected Fragment createFragment() {
        return new PhotoGalleryFragment();
    }
}
