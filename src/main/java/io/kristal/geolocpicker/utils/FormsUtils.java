package io.kristal.geolocpicker.utils;

import android.location.Address;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by sebastien on 06/04/2018.
 */

public final class FormsUtils {

    private static final String TAG = FormsUtils.class.getSimpleName();

    public static final @Nullable
    LatLng parseCoordinates(@NonNull String string) {
        String [] coordinates = string.split(",");
        if (coordinates.length == 2) {
            try {
                double latitude = Double.valueOf(coordinates[0]);
                double longitude = Double.valueOf(coordinates[1]);
                return new LatLng(latitude, longitude);
            }
            catch (NumberFormatException exception) {
                Log.e(TAG, "parseCoordinates: wrong coordinate format " + string + ".");
                exception.printStackTrace();
            }
        }
        else {
            Log.e(TAG, "parseCoordinates: wrong coordinate format " + string + ".");
        }

        return null;
    }

    public static final String formatAddress(@NonNull Address address){
        String newAddress = new String();

        String temp = address.getSubThoroughfare();
        if(temp != null && !temp.equals("Unnamed Road"))
            newAddress += temp + " ";
        temp = address.getThoroughfare();
        if(temp != null && !temp.equals("Unnamed Road"))
            newAddress += temp + ",\n";
        temp = address.getPostalCode();
        if(temp != null)
            newAddress += temp + " ";
        temp = address.getLocality();
        if(temp != null)
            newAddress += temp;

        return newAddress;
    }
}
