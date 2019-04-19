package io.kristal.geolocpicker;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.cobaltians.cobalt.Cobalt;
import org.cobaltians.cobalt.fragments.CobaltFragment;
import org.cobaltians.cobalt.plugin.CobaltAbstractPlugin;
import org.cobaltians.cobalt.plugin.CobaltPluginWebContainer;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import io.kristal.geolocpicker.utils.FormsUtils;

public class GeolocPicker extends CobaltAbstractPlugin {

    private static final String TAG = CobaltAbstractPlugin.class.getSimpleName();

    private CobaltFragment fragment;
    private Context context;
    private int mSelectLocationRequest;

    /*******************************************************************************************************
     * MEMBERS
     *******************************************************************************************************/

    private static GeolocPicker sInstance;

    /**************************************************************************************
     * CONSTRUCTORS
     **************************************************************************************/

    public static GeolocPicker getInstance()
    {
        if (sInstance == null)
        {
            sInstance = new GeolocPicker();
        }
        return sInstance;
    }
    
    @Override
    public void onMessage(@NonNull CobaltPluginWebContainer webContainer, @NonNull String action,
            @Nullable JSONObject data, @Nullable String callbackChannel)
    {
        // TODO: check nullability for webContainer.getFragment/getActivity
        fragment = webContainer.getFragment();
        context = webContainer.getActivity();
        Intent intent = new Intent(context, MapActivity.class);

        if ("selectLocation".equals(action)) {
            if (data != null) {
                String location = data.optString("location");

                mSelectLocationRequest = new Random().nextInt(254);
                if (location != null) {
                    LatLng coordinates = FormsUtils.parseCoordinates(location);
                    if (coordinates != null) {
                        intent.putExtra(MapActivity.EXTRA_COORDINATES, coordinates);
                    }
                }
                String address = data.optString("address");
                if (address != null) {
                    intent.putExtra(MapActivity.EXTRA_ADDRESS, address);
                }
            }
            fragment.startActivityForResult(intent, mSelectLocationRequest);
        }
        else if (Cobalt.DEBUG) {
            Log.w(TAG, "onMessage: action '" + action + "' not recognized");
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == mSelectLocationRequest) {
            if (data != null) {
                LatLng coordinates = data.getParcelableExtra(MapActivity.EXTRA_COORDINATES);
                String address = data.getStringExtra(MapActivity.EXTRA_ADDRESS);
                try {
                    if (coordinates != null) {
                            JSONObject callbackData = new JSONObject();
                            callbackData.put("location", coordinates.latitude + "," + coordinates.longitude);
                            callbackData.put("address", address);
                            // TODO: use PubSub on callbackChannel
                            //fragment.sendPlugin(mPluginName, callbackData);
                        } else {
                        JSONObject callbackData = new JSONObject();
                        // TODO: use PubSub on callbackChannel
                        //fragment.sendPlugin(mPluginName, callbackData);
                    }
                } catch (JSONException exception) {
                    exception.printStackTrace();
                }
            }
        }
    }
}
