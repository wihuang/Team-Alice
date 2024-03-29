package cse110.TeamNom.projectnom;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;

/*
 * Class that handles GPS activity by using the services
 * of another application.
 */
public class GPSFragment extends Service implements LocationListener{
	private final Context mContext;

	//flag for GPS Status
	boolean isGPSEnabled = false;
	//flag for network status
	boolean isNetworkEnabled = false;
	//flag for connection status
	boolean canGetLocation = false;
	// Location variables
	Location location;
	double latitude;
	double longitude;

	//The minimum distance to change updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; //10 meters

	//The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

	//Declaring a Location Manager
	protected LocationManager locationManager;
	// Constructor of getting the location of users.
	public GPSFragment(Context context) 
	{
		this.mContext = context;
		getLocation();
	}
	
	/*
	 * Method that returns the location of the user by using the GPS service.
	 */
	public Location getLocation()
	{
		try
		{
			locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

			//getting GPS status
			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

			//getting network status
			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled)
			{
				// no network provider is enabled
			}
			else
			{
				this.canGetLocation = true;

				//First get location from Network Provider
				if (isNetworkEnabled)
				{
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

					if (locationManager != null)
					{
						location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						updateGPSCoordinates();
					}
				}

				//if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled)
				{
					if (location == null)
					{
						locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

						if (locationManager != null)
						{
							location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							updateGPSCoordinates();
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return location;
	}
	
	/*
	 * Method that updates the longitude and latitude 
	 * of the user.
	 */
	public void updateGPSCoordinates()
	{
		if (location != null)
		{
			// Sets the longitude and latitude of the user.
			latitude = location.getLatitude();
			longitude = location.getLongitude();
		}
	}

	/**
	 * Stop using GPS listener
	 * Calling this function will stop using GPS in your app
	 */

	public void stopUsingGPS()
	{
		if (locationManager != null)
		{
			locationManager.removeUpdates(GPSFragment.this);
		}
	}

	/**
	 * Function to get latitude
	 */
	public double getLatitude()
	{
		if (location != null)
		{
			latitude = location.getLatitude();
		}

		return latitude;
	}

	/**
	 * Function to get longitude
	 */
	public double getLongitude()
	{
		if (location != null)
		{
			longitude = location.getLongitude();
		}

		return longitude;
	}

	/**
	 * Function to check GPS/wifi enabled
	 */
	public boolean canGetLocation()
	{
		return this.canGetLocation;
	}

	
	 /* Function to show settings alert dialog */
	 
	public void showSettingsAlert()
	{
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

		//Setting Dialog Title
		alertDialog.setTitle(R.string.GPSAlertDialogTitle);

		//Setting Dialog Message
		alertDialog.setMessage(R.string.GPSAlertDialogMessage);

		//On Pressing Setting button
		alertDialog.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() 
		{	
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				mContext.startActivity(intent);
			}
		});

		//On pressing cancel button
		alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() 
		{	
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				dialog.cancel();
			}
		});

		alertDialog.show();
	}
	 
	/**
	 * Get list of address by latitude and longitude
	 * @return null or List<Address>
	 */
	public List<Address> getGeocoderAddress(Context context)
	{
		if (location != null)
		{
			Geocoder geocoder = new Geocoder(context, Locale.ENGLISH);
			try 
			{
				List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
				return addresses;
			} 
			catch (IOException e) 
			{
			}
		}

		return null;
	}

	/**
	 * Try to get AddressLine
	 * @return null or addressLine
	 */
	public String getAddressLine(Context context)
	{
		List<Address> addresses = getGeocoderAddress(context);
		if (addresses != null && addresses.size() > 0)
		{
			Address address = addresses.get(0);
			String addressLine = address.getAddressLine(0);

			return addressLine;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Try to get Locality
	 * @return null or locality
	 */
	public String getLocality(Context context)
	{
		List<Address> addresses = getGeocoderAddress(context);
		if (addresses != null && addresses.size() > 0)
		{
			Address address = addresses.get(0);
			String locality = address.getLocality();

			return locality;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Try to get Postal Code
	 * @return null or postalCode
	 */
	public String getPostalCode(Context context)
	{
		List<Address> addresses = getGeocoderAddress(context);
		if (addresses != null && addresses.size() > 0)
		{
			Address address = addresses.get(0);
			String postalCode = address.getPostalCode();

			return postalCode;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Try to get CountryName
	 * @return null or postalCode
	 */
	public String getCountryName(Context context)
	{
		List<Address> addresses = getGeocoderAddress(context);
		if (addresses != null && addresses.size() > 0)
		{
			Address address = addresses.get(0);
			String countryName = address.getCountryName();

			return countryName;
		}
		else
		{
			return null;
		}
	}

	// Method stubs of unimplemented method for GPS.
	@Override
	public void onLocationChanged(Location location) 
	{	
	}

	@Override
	public void onProviderDisabled(String provider) 
	{	
	}

	@Override
	public void onProviderEnabled(String provider) 
	{	
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) 
	{	
	}

	@Override
	public IBinder onBind(Intent intent) 
	{
		return null;
	}
}
