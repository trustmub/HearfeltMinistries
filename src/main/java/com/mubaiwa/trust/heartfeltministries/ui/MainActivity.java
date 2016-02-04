package com.mubaiwa.trust.heartfeltministries.ui;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mubaiwa.trust.heartfeltministries.model.CurrentEvent;
import com.mubaiwa.trust.heartfeltministries.R;
import com.mubaiwa.trust.heartfeltministries.model.Event;
import com.mubaiwa.trust.heartfeltministries.model.Unifier;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String EVENT_UNIFIER = "EVENT_UNIFIER";
    public static final String ZONE_UNIFIER = "ZONE_UNIFIER";
    private Unifier mUnifier;

    @Bind(R.id.dateValue) TextView mDateValue;
    @Bind(R.id.venueValue) TextView mVenueValue;
    @Bind(R.id.addressValue) TextView mAddressValue;
    @Bind(R.id.descriptionValue) TextView mDescriptionValue;
    @Bind(R.id.themeLabel)TextView mTheme;
    @Bind(R.id.startTimeValue) TextView mStartTimeValue;
    @Bind(R.id.endTimeValue) TextView mEndTimeValue;

    @Bind(R.id.progressBar) ProgressBar mProgressBar;
    @Bind(R.id.refreshImageView) ImageView mRefreshImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mProgressBar.setVisibility(View.INVISIBLE);

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRecentEvent();
            }
        });

        getRecentEvent();



    }

    private void getRecentEvent() {

        String apkKey = "56af6592e4b01190df4c644d";
        String mainUrl ="https://jsonblob.com/api/jsonBlob/" + apkKey;

        if(isNetworkAvailable()) {

            taggleRefresh();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(mainUrl)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            taggleRefresh();
                        }
                    });

                    alertUserAboutError();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            taggleRefresh();
                        }
                    });
                    try {
                        String jsonData = response.body().string();

                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            mUnifier = parseUnifierDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });


                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Exception Caught", e);
                    }

                }
            });
        }else {
            Toast.makeText(this, "Network is Not-Available", Toast.LENGTH_LONG).show();
        }
    }


    private void updateDisplay() {

        CurrentEvent currentEvent = mUnifier.getCurrentEvent();

        mTheme.setText(currentEvent.getTheme());
        mStartTimeValue.setText(currentEvent.getStartTime() + " HRS");
        mEndTimeValue.setText(currentEvent.getEndTime() + " HRS");
        mDateValue.setText("On " + currentEvent.getDate());
        mVenueValue.setText("At " + currentEvent.getVenue());
        mAddressValue.setText(currentEvent.getAddress());
        mDescriptionValue.setText(currentEvent.getDescription());

        //Drawable drawable = getResources().getDrawable(mCurrentWeather.getIconId());
        //mIconImageView.setImageDrawable(drawable);
    }

    private Unifier parseUnifierDetails(String jsonData) throws JSONException{
        Unifier unifier = new Unifier();

        unifier.setCurrentEvent(getCurrentDetails(jsonData));
        unifier.setEventUnified(getEventUnified(jsonData));
        //TODO create the getZoneUnified class after testing
        //unifier.setZoneUnified(getZoneUnified(jsonData));

        return unifier;
    }

    private Event[] getEventUnified(String jsonData) throws JSONException{
        JSONObject collection = new JSONObject(jsonData);
        //TODO will need the timezone when doing my events rotation
        //String timezone = forecast.getString("timezone");
        //JSONObject events = collection.getJSONObject("events");
        JSONArray events = collection.getJSONArray("events");

        Event[] theEvents = new Event[events.length()];

        for(int i = 0; i < events.length(); i++){
            JSONObject jsonEvent = events.getJSONObject(i);

            Event thisEvent = new Event();

            thisEvent.setName(jsonEvent.getString("name"));
            thisEvent.setDescription(jsonEvent.getString("description"));
            thisEvent.setAdmission(jsonEvent.getString("admission"));
            thisEvent.setRegistration(jsonEvent.getString("registration"));
            thisEvent.setStartTime(jsonEvent.getString("starttime"));
            thisEvent.setEndTime(jsonEvent.getString("endtime"));
            thisEvent.setVenue(jsonEvent.getString("venue"));
            thisEvent.setDate(jsonEvent.getString("date"));
            thisEvent.setIcon(jsonEvent.getString("icon"));

            theEvents[i] = thisEvent;
        }
        return theEvents;
    }


    private CurrentEvent getCurrentDetails(String jsonData) throws JSONException{
        JSONObject collection = new JSONObject(jsonData);
        //String timezone = collection.getString("timezone");
        //Log.i(TAG, "From JSON: " + timezone);

        JSONObject currently = collection.getJSONObject("currently");

        CurrentEvent currentEvent = new CurrentEvent();
        currentEvent.setTheme(currently.getString("theme"));
        currentEvent.setDescription(currently.getString("description"));
        currentEvent.setStartTime(currently.getString("starttime"));
        currentEvent.setEndTime(currently.getString("endtime"));
        currentEvent.setVenue(currently.getString("venue"));
        currentEvent.setAddress(currently.getString("address"));
        currentEvent.setDate(currently.getString("date"));
        currentEvent.setIconId(currently.getString("icon"));

        //Log.d(TAG, currentEvent.getFormattedTime());

        return currentEvent;
    }

    private void taggleRefresh() {
        if(mProgressBar.getVisibility() == View.INVISIBLE){
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        }else{
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfor = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if(networkInfor != null && networkInfor.isConnected()){
            isAvailable = true;
        }

        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error response");
    }
    //Todo disable the buttons if the app is offline
    //todo disable the button while the app is downloading the jsonData

    @OnClick(R.id.eventsButton)
    public void startEventsActivity(View view){
        Intent intent = new Intent(this, EventsActivity.class);
        intent.putExtra(EVENT_UNIFIER, mUnifier.getEventUnified());
        startActivity(intent);
    }

    @OnClick(R.id.zonesButton)
    public void startZonesActivity(View view){
        Intent intent = new Intent(this, ZonesActivity.class);
        intent.putExtra(ZONE_UNIFIER, mUnifier.getZoneUnified());
        startActivity(intent);
    }


}
