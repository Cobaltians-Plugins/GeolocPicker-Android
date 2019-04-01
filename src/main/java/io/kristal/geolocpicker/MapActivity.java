package io.kristal.geolocpicker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static io.kristal.geolocpicker.utils.FormsUtils.formatAddress;


/**
 * Created by vincent Rifa on 23/03/2019.
 */

public final class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener {

    private static final String TAG = MapActivity.class.getSimpleName();

    public static final String EXTRA_COORDINATES = "io.kristal.forms.activities.MapActivity.EXTRA_COORDINATES";
    public static final String EXTRA_ADDRESS = "io.kristal.forms.activities.MapActivity.EXTRA_ADDRESS";

    private LatLng mPinCoordinates;
    private String mAddress;
    private GoogleMap mMap;
    private Marker mMarker;
    private final LatLng DEFAULT_LATLNG = new LatLng(46.3432433, 2.5907915); // Center on France (coordinates: Montlucon)
    private TextView instructions;
    private TextView address;
    private ImageButton clear_button;
    private int shortAnimationDuration;


    /***********************************************************************************************
     *
     * LIFECYCLE
     *
     **********************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        mPinCoordinates = getIntent().getParcelableExtra(EXTRA_COORDINATES);
        mAddress = getIntent().getStringExtra(EXTRA_ADDRESS);

        Fragment mapFragment = getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null
                && mapFragment instanceof SupportMapFragment) {
            ((SupportMapFragment) mapFragment).getMapAsync(this);
        }
        else {
            Log.e(TAG, "onCreate: map fragment not found.");
        }

        this.setTitle(R.string.map_title);

        instructions = findViewById(R.id.instructions);
        address = findViewById(R.id.address);
        clear_button = findViewById(R.id.clear_button);
        shortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

    }

    /***********************************************************************************************
     *
     * MENU
     *
     **********************************************************************************************/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_map, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_ok) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_COORDINATES, mPinCoordinates);
            intent.putExtra(EXTRA_ADDRESS, mAddress);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    /***********************************************************************************************
     *
     * MAP
     *
     **********************************************************************************************/

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mPinCoordinates != null) {
            // Set Marker and address
            mMarker = mMap.addMarker(new MarkerOptions().position(mPinCoordinates));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mPinCoordinates, 15));
            address.setText(mAddress);
            // Hide instructions
            instructions.setVisibility(View.GONE);

        }
        else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LATLNG, 5));
            // Hide Address
            address.setVisibility(View.GONE);
            clear_button.setVisibility(View.GONE);
        }

        mMap.setOnMapLongClickListener(this);
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (mPinCoordinates == null) {
            invalidateOptionsMenu();
        }
        mPinCoordinates = latLng;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            Log.e(TAG, "onMapLongClick: Error on getting addresses: " + e.toString());
        }

        if(addresses == null || addresses.isEmpty()) {
            mAddress = getString(R.string.unknown_address);
        }
        else{
            mAddress = addresses.get(0).getAddressLine(0);
        }
        Log.d(TAG, "Address get from Google Maps : " + mAddress);
        mAddress = formatAddress(addresses.get(0));
        if(mAddress.isEmpty())
            mAddress = getString(R.string.unknown_address);
        Log.d(TAG, "Address formatted : " + mAddress);

        if (mMarker == null) {
            mMarker = mMap.addMarker(new MarkerOptions().position(mPinCoordinates));
        }
        else {
            mMarker.setPosition(latLng);
        }

        mMarker.setVisible(true);
        refreshInfo();
    }

    public void onClear(View v) {
        // Clear coordinates & address
        mPinCoordinates = null;
        mAddress = null;
        mMarker.setVisible(false);

        refreshInfo();
    }

    public void refreshInfo(){
        //If no location selected
        if(mPinCoordinates == null){
            //Hide address
            //Display instructions
            // TODO Check (API 21 for CardView)
            instructions.setAlpha(0f);
            instructions.setVisibility(View.VISIBLE);
            instructions.animate()
                    .alpha(1f)
                    .setDuration(shortAnimationDuration)
                    .setListener(null);
            address.animate()
                    .alpha(0f)
                    .setDuration(shortAnimationDuration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            address.setVisibility(View.GONE);
                        }
                    });
            clear_button.animate()
                    .alpha(0f)
                    .setDuration(shortAnimationDuration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            address.setVisibility(View.GONE);
                        }
                    });
        }
        //If location selected
        else {
            //Hide instructions
            instructions.animate()
                    .alpha(0f)
                    .setDuration(shortAnimationDuration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            instructions.setVisibility(View.GONE);
                        }
                    });

            //Display address
            address.setText(mAddress);
            address.setAlpha(0f);
            address.setVisibility(View.VISIBLE);
            address.animate()
                    .alpha(1f)
                    .setDuration(shortAnimationDuration)
                    .setListener(null);
            //Display address
            clear_button.setAlpha(0f);
            clear_button.setVisibility(View.VISIBLE);
            clear_button.animate()
                    .alpha(1f)
                    .setDuration(shortAnimationDuration)
                    .setListener(null);
        }
    }

}
