
package dtindustries.go5c;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class main_activity extends ListActivity implements View.OnClickListener {
    // Progress Dialog
    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> eventsList;

    // url to get all events list
    private static final String url_all_events = "http://10.0.2.2/5CGO/GetAllEvents.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_EVENTS = "events";
    private static final String TAG_EID = "eid";
    private static final String TAG_NAME = "title";
    private static final String TAG_DATEOF = "dateof";
    private static final String TAG_LOCATION = "location";
    //  private static final String TAG_HOST = "host";

    private Button createEvent, profile;

    // events JSONArray
    JSONArray events = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        createEvent= (Button) findViewById(R.id.createEventbt);
        createEvent.setOnClickListener(this);
        profile = (Button) findViewById(R.id.ProfileButton);
        profile.setOnClickListener(this);

        // Hashmap for ListView
        eventsList = new ArrayList<HashMap<String, String>>();


        // Loading products in Background Thread
        new LoadAllEvents().execute();

        // Get listview
        ListView lv = getListView();

        // on selecting single event
        // launching event Screen
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String eid = ((TextView) view.findViewById(R.id.eid)).getText()
                        .toString();

                // Starting new intent
                //*************GO TO EVENT PAGEEEEE*****************************
                Intent in = new Intent(getApplicationContext(),
                        login.class);
                // sending pid to next activity
                in.putExtra(TAG_EID, eid);

                // starting new activity and expecting some response back
                startActivityForResult(in, 100);
            }
        });

    }



    // Response from event Activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if result code 100
        if (resultCode == 100) {
            // if result code 100 is received
            // means user edited/deleted event
            // reload this screen again
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

    }

    /**
     * Background Async Task to Load all product by making HTTP Request
     * */
    class LoadAllEvents extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(main_activity.this);
            pDialog.setMessage("Loading events. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * getting All events from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_events, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("All Events: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // events found
                    // Getting Array of Events
                    events = json.getJSONArray(TAG_EVENTS);

                    // looping through All Events
                    for (int i = 0; i < events.length(); i++) {
                        JSONObject c = events.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString(TAG_EID);
                        String title = c.getString(TAG_NAME);
                        String dateof = c.getString(TAG_DATEOF);
                        String location = c.getString(TAG_LOCATION);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_EID, id);
                        map.put(TAG_NAME, title);
                        map.put(TAG_DATEOF, dateof);
                        map.put(TAG_LOCATION, location);

                        // adding HashList to ArrayList
                        eventsList.add(map);
                    }
                } else {
                    // no events found
                    // ***************Launch Add New product Activity*******************
                    Intent i = new Intent(getApplicationContext(),
                            login.class);
                    // Closing all previous activities
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    ListAdapter adapter = new SimpleAdapter(
                            main_activity.this, eventsList,
                            R.layout.events_info, new String[] { TAG_EID,
                            TAG_NAME, TAG_DATEOF, TAG_LOCATION},
                            new int[] { R.id.eid, R.id.title, R.id.dateof, R.id.location});
                    // updating listview
                    setListAdapter(adapter);
                }
            });

        }

    }

    @Override public void onClick(View v) {
        // TODO Auto-generated method
        switch (v.getId()) {
            case R.id.createEventbt:
                startActivity(new Intent(main_activity.this, CreateEvent.class));
                break;
            case R.id.ProfileButton:
                startActivity(new Intent(main_activity.this, profile.class));
                break;


        }
    }
}