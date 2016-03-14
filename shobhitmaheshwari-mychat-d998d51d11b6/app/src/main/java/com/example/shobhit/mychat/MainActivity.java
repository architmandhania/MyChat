package com.example.shobhit.mychat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

	public static String LOG_TAG = "MyChat ";
    public static final String myUserIdKey = "myUserId";
    public static final String myNickname = "myNickname";
	public static final String myRestaurant = "myRestaurant";
	private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    private static boolean precise_location_found = false;
    public static Location myLocation;

	String addressline = "";
	String city = "";
	String state = "";
	String country = "";
	String postalCode = "";
	String knownName = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		saveOwnid();
	}

	@Override
	public void onResume(){

        requestLocationUpdate();

		//Enable/disable chatButton based on location information
		manageChatButton();

		super.onResume();
	}

	public void getaddress(double latitude, double longitude) {
		Geocoder geocoder;
		List<Address> addresses;
		geocoder = new Geocoder(this, Locale.getDefault());
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 1);
			addressline = addresses.get(0).getAddressLine(0);
			city = addresses.get(0).getLocality();
			state = addresses.get(0).getAdminArea();
			country = addresses.get(0).getCountryName();
			postalCode = addresses.get(0).getPostalCode();
			knownName = addresses.get(0).getFeatureName();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void getLocation(View v) {
		double lat = myLocation.getLatitude();
		double longi = myLocation.getLongitude();
		getaddress(lat, longi);

		TextView tmp = (TextView) findViewById(R.id.v_address);
		tmp.setText("Address: " + addressline);
		tmp.setVisibility(View.VISIBLE);

		tmp = (TextView) findViewById(R.id.v_city);
		tmp.setText("City: " + city);
		tmp.setVisibility(View.VISIBLE);

		tmp = (TextView) findViewById(R.id.v_state);
		tmp.setText("State: " + state);
		tmp.setVisibility(View.VISIBLE);

		tmp = (TextView) findViewById(R.id.v_country);
		tmp.setText("Country: " + country);
		tmp.setVisibility(View.VISIBLE);

		tmp = (TextView) findViewById(R.id.v_postal_code);
		tmp.setText("Zipcode: " + postalCode);
		tmp.setVisibility(View.VISIBLE);

		tmp = (TextView) findViewById(R.id.v_name);
		tmp.setText("Name: " + knownName);
		tmp.setVisibility(View.VISIBLE);
	}

	/*
	Request location update. This must be called in onResume if the user has allowed location sharing
	 */
	private void requestLocationUpdate(){

		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //initialize myLocation to last known location by NETWORK_PROVIDER
        myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (locationManager == null) {
            Log.i(LOG_TAG, "No Location Manager.");
            return;
        }

        List<String> providers = locationManager.getProviders(true);
        if (providers.size() == 0) {
            Log.i(LOG_TAG, "No Location Providers found.");
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
					PackageManager.PERMISSION_GRANTED) {
               Log.i(LOG_TAG, "Manifest gave location permission. Requesting location update");
               for (int i = 0; i < providers.size(); i++)
                 locationManager.requestLocationUpdates(providers.get(i), 9000, 0, locationListener);
        } else {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
							MY_PERMISSIONS_REQUEST_FINE_LOCATION);

            // MY_PERMISSIONS_REQUEST_FINE_LOCATION is an app-defined int constant.
            // The callback method gets the result of the request
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		switch (requestCode) {
			case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.i(LOG_TAG, "Location permission granted. Yay!.");

                    List<String> providers = locationManager.getProviders(true);
                    if (providers.size() == 0) {
                        Log.i(LOG_TAG, "No Location Providers found.");
                        return;
                    }

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED) {
                        Log.i(LOG_TAG, "Requesting location update (after location permission is granted).");
                        for (int i = 0; i < providers.size(); i++)
                            locationManager.requestLocationUpdates(providers.get(i), 9000, 0, locationListener);
					} else {
						throw new RuntimeException("permission not granted, still callback fired");
					}

				} else {
					// permission denied, boo! Disable the
					// functionality that depends on this permission.
				}
				return;
			}

			// other 'case' lines to check for other
			// permissions this app might request in future
		}
	}

	/*
	Remove location update. This must be called in onPause if the user has allowed location sharing
	 */
	private void removeLocationUpdate() {
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		if (locationManager != null) {
			if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
					PackageManager.PERMISSION_GRANTED) {

				locationManager.removeUpdates(locationListener);
				Log.i(LOG_TAG, "removing location update");
			}
		}
	}

	@Override
	public void onPause(){
        removeLocationUpdate();//if the user has allowed location sharing we must disable location updates now
		super.onPause();
	}

	/**
	 * Listens to the location, and gets the most precise recent location.
	 * Copied from Prof. Luca class code
	 */
	LocationListener locationListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {

            Log.i(LOG_TAG, "location changed called");
            myLocation = location;
            dumpLocation();

            //if found the location accuracy within 50m, then enable the chat button
			precise_location_found = ((myLocation != null) && (myLocation.getAccuracy() <= 50.0));
            manageChatButton();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {}

		@Override
		public void onProviderEnabled(String provider) {}

		@Override
		public void onProviderDisabled(String provider) {}
	};

    /*
	Set the button enable/disable
	 */
    private void manageChatButton(){

        //TODO: should be false, but making it true since location service is not working in emulation
        boolean setButtonState = true;

        if (precise_location_found) {
            //check if the user has entered any nickname
            String nickname = ((EditText) findViewById(R.id.editText)).getText().toString();
            if (nickname.length() > 0) {
                setButtonState = true;
            }
        }
        //enable Start Chat button
        Button chatButton = (Button) findViewById(R.id.startChat);
        chatButton.setEnabled(setButtonState);
    }

    //generate unique id, and save it in preferences as myUserId
	private void saveOwnid() {

		//Create a random string using UUID, and take only the first 5 characters.
        //No need to check if the random string is less than 5 because UUID string always is.
        String user_id = (UUID.randomUUID().toString()).substring(0, 5);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(myUserIdKey, user_id);
        editor.commit();
        Log.i(LOG_TAG, "Saved in Preferences " + myUserIdKey + " as: " + user_id);
	}

    private void saveNickname() {
        String nickname = ((EditText) findViewById(R.id.editText)).getText().toString();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(myNickname, nickname);
        editor.commit();
        Log.i(LOG_TAG, "Saved in Preferences " + myNickname + " as: " + nickname);
    }

	private void saveRestaurant() {
		String restaurant = ((EditText) findViewById(R.id.editName)).getText().toString();
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(myRestaurant, restaurant);
		editor.commit();
	}

	public void checkIn(View v) {
        saveNickname();
		saveRestaurant();
		Intent intent = new Intent(this, ChatActivity.class);
		startActivity(intent);
	}

	public void restartApp(View v) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		//finish();
	}

    private void dumpLocation() {
        if (myLocation != null) {
            Log.i(LOG_TAG, "Latitude = " + Double.toString(myLocation.getLatitude()));
            Log.i(LOG_TAG, "Longitude = " + Double.toString(myLocation.getLongitude()));
            Log.i(LOG_TAG, "Accuracy = " + Float.toString(myLocation.getAccuracy()));
        } else {
            Log.i(LOG_TAG, "Location is null");
        }
    }
}

