package com.abourahal.michael.uottawa.teamup;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


/**
 * Created by hocke on 2017-07-16.
 */

public class FirstFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    MapView mMapView;
    View myView;
    Location lo;
    private GoogleApiClient gc;
    private FusedLocationProviderApi locationProvider = LocationServices.FusedLocationApi;
    private LocationRequest lr;
    private double latitude;
    private double longitude;
    private double selectedLatitude;
    private double selectedLongitude;
    private float zoom =14;
    private float bearing = 0;
    private int count = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.first_layout, container, false);
        gc = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        gc.connect();

        lr = new LocationRequest();
        lr.setInterval(60*1000);
        lr.setFastestInterval(15*1000);
        lr.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setVisibility(View.VISIBLE);
        fab.setImageResource(R.mipmap.ic_add_white_24dp);
        //fab.set
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Adding Event", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                //NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);

                if(selectedLongitude!=0&&selectedLatitude!=0) {
                    CreateEventFragment cr = new CreateEventFragment();
                    Bundle b = new Bundle();
                    b.putDouble("latitude", selectedLatitude);
                    b.putDouble("longitude", selectedLongitude);
                    cr.setArguments(b);
                    FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.content_frame, cr).commit();
                }
                else{
                    Toast.makeText(getActivity(), "Please select a location on the map", Toast.LENGTH_LONG).show();
                }

            }
        });
        //writeToFile("",getActivity());

        return myView;


    }
    private void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("config.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMapView = (MapView) myView.findViewById(R.id.mapView2);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        MapsInitializer.initialize(getActivity());



        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);


            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
                @Override
                public void onMapClick(LatLng latLng) {
                    mMap.clear();
                    loadFile();

                    Marker m = mMap.addMarker(new MarkerOptions().position(latLng).title("Event here?"));
                    m.showInfoWindow();
                    selectedLatitude = latLng.latitude;
                    selectedLongitude = latLng.longitude;
                }
            });
        } else {

        Toast.makeText(getActivity(), "permission denied", Toast.LENGTH_LONG).show();
    }
    loadFile();

    }

    private void loadFile()
    {
        //writeToFile("",getActivity());
        String allActivity = readFromFile(getActivity());
        String[] allMarker = allActivity.split("\\-\\^\\-");
        for(int i=0;i<allMarker.length;++i)
        {
            final String[] allFileItems = (allMarker[i]).split("\\|\\^\\|");
            if(!allFileItems[0].equals("")) {
                double lat = Double.parseDouble(allFileItems[0]);
                double lon = Double.parseDouble(allFileItems[1]);
                final String info =  "Max Participant: "+allFileItems[3] + "\n"+"Date: "+allFileItems[4] + "\n"+"Start Time: "+allFileItems[5] + "\n"+"End Time: "+allFileItems[6] + "\n"+"Description:  "+allFileItems[7];
                Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title(allFileItems[2]).snippet(info));

                if(allFileItems[8].equalsIgnoreCase("Hockey"))
                {
                    m.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.hockey));
                }
                else if(allFileItems[8].equalsIgnoreCase("Soccer"))
                {
                    m.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.soccerballvariant));
                }
                else if(allFileItems[8].equalsIgnoreCase("Football"))
                {
                    m.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.americanfootball));
                }
                else if(allFileItems[8].equalsIgnoreCase("Baseball"))
                {
                    m.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.baseball));
                }
                else
                {
                    m.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.basketball));
                }

                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                    @Override
                    public View getInfoWindow(Marker arg0) {

                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker m) {

                        Context mContext = getActivity();

                        LinearLayout info = new LinearLayout(mContext);
                        info.setOrientation(LinearLayout.VERTICAL);

                        TextView title = new TextView(mContext);
                        title.setTextColor(Color.BLACK);
                        title.setGravity(Gravity.CENTER);
                        title.setTypeface(null, Typeface.BOLD);
                        title.setText(m.getTitle());
                        title.setSingleLine(false);

                        info.addView(title);

                        if (m.getSnippet() != null) {
                            TextView snippet = new TextView(mContext);
                            snippet.setTextColor(Color.GRAY);
                            snippet.setText(m.getSnippet());
                            snippet.setSingleLine(false);

                            info.addView(snippet);
                            String file = readFromFile(getActivity());
                            String[] lines = file.split("\\-\\^\\-");


                            Button b = new Button(getActivity());

                            b.setText("Join");
                            b.setBackgroundColor(Color.TRANSPARENT);
                            //b.setBackgroundColor(R.color.colorPrimary);

                            info.addView(b);
                        }


                        return info;
                    }
                });
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        requestLocationUpdate();


    }
    private void requestLocationUpdate()
    {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(gc, lr, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        if(count ==0)
        {
            zoom= 14;
            bearing = 0;
            CameraPosition lib = CameraPosition.builder().target(new LatLng(latitude, longitude)).zoom(zoom).bearing(bearing).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(lib));

        }


        count++;
    }

}

