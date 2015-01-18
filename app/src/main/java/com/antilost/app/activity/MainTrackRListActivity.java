package com.antilost.app.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.antilost.app.R;
import com.antilost.app.adapter.TrackRListAdapter;

public class MainTrackRListActivity extends Activity implements View.OnClickListener{

    private TrackRListAdapter mListViewAdapter;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_track_rlist);
        mListView = (ListView) findViewById(R.id.listview);
        mListViewAdapter = new TrackRListAdapter(getLayoutInflater());
        mListView.setAdapter(mListViewAdapter);
        findViewById(R.id.btnUserProfile).setOnClickListener(this);
        findViewById(R.id.btnLocation).setOnClickListener(this);
        findViewById(R.id.btnAdd).setOnClickListener(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_track_rlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAdd:
                addNewTrackR();
                break;
            case R.id.btnUserProfile:
                showUserProfile();
                break;
            case R.id.btnLocation:
                showLocations();
                break;
        }
    }

    private void showLocations() {

    }

    private void showUserProfile() {

    }

    private void addNewTrackR() {
        Intent i = new Intent(this, StartBindActivity.class);
        startActivity(i);
    }
}
