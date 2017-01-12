package com.example.android.sp;
//All imports
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ruturaj on 1/8/17.
 */
public class ParkWhizSpots {

    //Variable Declarations
    private double latitude, longitude;
    private GoogleMap searchmap;
    private Context context;
    private String TAG = "debugger";
    private Map PWSpotnames = new HashMap();
    private Map PWSpotprices = new HashMap();

    public ParkWhizSpots() {
    }  //empty constructor

    public ParkWhizSpots(double latitude, double longitude, GoogleMap searchmap, Context context) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.searchmap = searchmap;
        this.context = context;
    }

    public void getParkWhizspots(){
        new ParkWhizFeedTask().execute();   //execute the ParkWhiz spot finder
    }

    public Map getPWSpotnames(){
        return PWSpotnames;                 //return spotnames map
    }

    private class ParkWhizFeedTask extends AsyncTask<Void, Void, String> {

        private Exception exception;

        protected void onPreExecute() {

        }

        protected String doInBackground(Void... urls) {


            try {
                //make the URL
                URL url = new URL("https://api.parkwhiz.com/search/?lat="+Double.toString(latitude)+"&lng="+Double.toString(longitude)+getTimeStamp()+"&key="+context.getResources().getString(R.string.ParkWhiz_API_key));
                //URL url = new URL("https://api.parkwhiz.com/search/?lat=" + Double.toString(40.7590) + "&lng=" + Double.toString(-73.9845) + getTimeStamp() + "&key=c38210d1f5fda38362d86859997ef847");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        // returns unix timestamps of now and 3hours from now
        private String getTimeStamp() {


            String start = Long.toString(System.currentTimeMillis() / 1000L);
            Calendar now = Calendar.getInstance();
            Calendar tmp = (Calendar) now.clone();
            tmp.add(Calendar.HOUR_OF_DAY, 3);
            String end = Long.toString(tmp.getTimeInMillis() / 1000L);

            return ("&start=" + start + "&end=" + end);
        }



        protected void onPostExecute(String response) {
            if (response == null) {
                response = "THERE WAS AN ERROR";
            }

            try { //get the parking lot names and their prices
                JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
                JSONArray array = object.getJSONArray("parking_listings");
                if (array == null) {
                    Log.d(TAG, "nothing here");
                }
                for (int i = 0; i < array.length(); i++) {
                    Log.d(TAG, "parking names " + array.getJSONObject(i).getDouble("lat"));
                    Double lat = array.getJSONObject(i).getDouble("lat");
                    Double lng = array.getJSONObject(i).getDouble("lng");
                    LatLng pwspotplace = new LatLng(lat,lng);
                    String pwspotname  = array.getJSONObject(i).getString("location_name");
                    Double pwspotprice = array.getJSONObject(i).getDouble("price");
                    PWSpotnames.put(pwspotplace,pwspotname);   //map them to their locations
                    PWSpotprices.put(pwspotplace,pwspotprice);

                    IconGenerator iconFactory = new IconGenerator(context); //generate the custom marker showing price
                    iconFactory.setStyle(IconGenerator.STYLE_PURPLE);
                    iconFactory.setTextAppearance(R.style.iconGenText);
                    MarkerOptions markerOptions = new MarkerOptions().
                            icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon("$"+Double.toString(pwspotprice)))).
                            position(pwspotplace).
                            anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
                    searchmap.addMarker(markerOptions);     //add it to the map
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
