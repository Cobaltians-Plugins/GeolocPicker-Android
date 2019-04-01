package io.kristal.geolocpicker;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.cobaltians.cobalt.Cobalt;
import org.cobaltians.cobalt.fragments.CobaltFragment;
import org.cobaltians.cobalt.plugin.CobaltAbstractPlugin;
import org.cobaltians.cobalt.plugin.CobaltPluginWebContainer;
import org.json.JSONException;
import org.json.JSONObject;

import io.kristal.geolocpicker.utils.FormsUtils;

public class GeolocPicker extends CobaltAbstractPlugin {

    private static final String TAG = CobaltAbstractPlugin.class.getSimpleName();

    private CobaltFragment fragment;
    private Context context;
    private String mSelectLocationCallback;
    private int mSelectLocationRequest;

    /*******************************************************************************************************
     * MEMBERS
     *******************************************************************************************************/

    private static GeolocPicker sInstance;

    /**************************************************************************************
     * CONSTRUCTORS
     **************************************************************************************/

    public static CobaltAbstractPlugin getInstance(CobaltPluginWebContainer webContainer) {
        if (sInstance == null) {
            sInstance = new GeolocPicker();
        }
        return sInstance;
    }

    @Override
    public void onMessage(CobaltPluginWebContainer webContainer, JSONObject message) {
        try {
            fragment = webContainer.getFragment();
            context = webContainer.getActivity();
            String action = message.getString(Cobalt.kJSAction);
            JSONObject data = message.getJSONObject(Cobalt.kJSData);
            mSelectLocationCallback = message.getString(Cobalt.kJSCallback);
            mSelectLocationRequest = mSelectLocationCallback.hashCode();
            Intent intent = new Intent(context, MapActivity.class);

            if ("selectLocation".equals(action)) {
                if (data != null) {
                    String location = data.optString("location");
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
        catch(JSONException exception) {
            if (Cobalt.DEBUG) {
                Log.e(TAG, "onMessage: wrong format, possible issues: \n" +
                        "\t- missing 'action' field or not a string,\n" +
                        "\t- missing 'data' field or not a object,\n" +
                        "\t- missing 'data.actions' field or not an array,\n" +
                        "\t- missing 'callback' field or not a string.\n");
            }
            exception.printStackTrace();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == mSelectLocationRequest) {
            if (data != null) {
                LatLng coordinates = data.getParcelableExtra(MapActivity.EXTRA_COORDINATES);
                String address = data.getStringExtra(MapActivity.EXTRA_ADDRESS);
                if (coordinates != null) {
                    try {
                        JSONObject callbackData = new JSONObject();
                        callbackData.put("location", coordinates.latitude + "," + coordinates.longitude);
                        callbackData.put("address", address);
                        fragment.sendCallback(mSelectLocationCallback, callbackData);
                    } catch (JSONException exception) {
                        exception.printStackTrace();
                    }
                } else {
                    JSONObject callbackData = new JSONObject();
                    fragment.sendCallback(mSelectLocationCallback, callbackData);
                }
            }
        }
    }

}
