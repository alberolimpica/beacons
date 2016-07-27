package com.example.r00143659.beacondeployment;

import android.os.Bundle;
import android.os.RemoteException;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BeaconActivity extends AppCompatActivity implements BeaconConsumer, RangeNotifier {

    private BeaconManager mBeaconManager;
    static List<BeaconItem> beacons = new ArrayList<>();
    private ImageView mapView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon);

        //Android webService rest json
        //change the app's policies to avoid network exceptions
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //Service address
        String URL = "";

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    //This method gets called when the activity appears
    public  void onResume (){
        super.onResume();
        mBeaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        //setBeaconLayout method will tell the Android Beacon Library how to decode an Eddystone UID frame
        mBeaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("s:0-1=feaa,m:2-2=00,p:3-3:-41,i:4-13,i:14-19"));
        mBeaconManager.bind(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id == R.id.action_settings){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBeaconServiceConnect(){
        //To tell the libraryy that we want to see all beacons:
        Region region = new Region("all-beacons-region", null, null, null);
        try{
            //to start looking for beacons that match this region definition
            mBeaconManager.startRangingBeaconsInRegion(region);
        }catch(RemoteException e){
            e.printStackTrace();
        }
        //This class will receive callbacks everytime a beacon is seen
        mBeaconManager.setRangeNotifier(this);
    }
    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {

        for (Beacon beacon: beacons) {
            // You can tell if a beacon is an Eddystone beacon because it will have a serviceUuid of
            // 0xfeaa, and a beaconTypeCode of x00. (For the Eddystone-TLM frame, the beaconTypeCode will be 0x20 and for Eddystone-URL the beaconType code will be 0x10).
            if (beacon.getServiceUuid() == 0xfeaa && beacon.getBeaconTypeCode() == 0x00) {
                // This is a Eddystone-UID frame
                Identifier namespaceId = beacon.getId1();
                Identifier instanceId = beacon.getId2();

//                Log.d("Finding Beacons", "I see a beacon transmitting namespace id: " + namespaceId +
//                        " and instance id: " + instanceId +
//                        " approximately " + beacon.getDistance() + " meters away.");
                final String Id = String.valueOf(instanceId);
                final String namespace = String.valueOf(namespaceId);
                final double distance =  beacon.getDistance();
              //  storeBeacons(new BeaconItem(Id, namespace, distance));// Only the original thread that created a view hierarchy can touch its views.
                runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d("BeaconActivity", "Este beacon");
                        storeBeacons(new BeaconItem(Id, namespace, distance));// Only the original thread that created a view hierarchy can touch its views.


                        ((TextView)BeaconActivity.this.findViewById(R.id.message)).setText("Hello world, and welcome to Eddystone!");
                    }
                });
            }
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        mBeaconManager.unbind(this);
    }

    public void storeBeacons(BeaconItem beacon){
        List<String> beaconInfo = new ArrayList<String>();
        if(beacons.size() >= 3){
            return;
        }

        boolean equal = false;
        for (BeaconItem aux : beacons) {
            if(beacon.getId() == aux.getId()){
                aux.setDistance(beacon.getDistance());
                equal = true;

            }
        }

        if(!equal){
            beacons.add(beacon);
        }

        for (BeaconItem aux : beacons) {
            String name = aux.getNamespace();
            String dist = String.valueOf(aux.getDistance());
            String id = aux.getId();
            beaconInfo.add("I see a beacon transmitting namespace id: " + name +
                    " and instance id: " + id +
                    " approximately " + dist + " meters away.");
        }




        ListView listView = (ListView) findViewById(R.id.listView1);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, beaconInfo);
        listView.setAdapter(adapter);
    }

}



