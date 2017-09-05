package com.example.edgar.rutasencoder;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.model.Direction;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, DirectionCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private final float DEFAULT_ZOOM = 12;
    private final LatLng PUEBLA = new LatLng(19.0266703, -98.2127671);
    ProgressBar progressBar;
    int countMarkers=0;
    int lol=0;
    List<Marker> marcadores = new ArrayList<>();
    List<LatLng> finalList = new ArrayList<>();
    ArrayList<LatLng> resetList = new ArrayList<>();
    ArrayList<Polyline> polylines = new ArrayList<>();
    Polyline polyline;
    Button botonCalcula;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setVisibility(GONE);

        botonCalcula = (Button) findViewById(R.id.boton_genera);
        botonCalcula.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                progressBar.setVisibility(VISIBLE);

                if(marcadores.size()>=lol){
                    Log.d("XX-Drawing", "from " + marcadores.get(lol).getTitle() + " to " + marcadores.get(lol+1).getTitle());
                    calcularRuta(marcadores.get(lol).getPosition(), marcadores.get(lol + 1).getPosition());

                }


                lol++;
            }
        });

        Button botonDibuja = (Button) findViewById(R.id.boton_dibuja);
        botonDibuja.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dibujarRuta();
            }
        });
    }

    public void resetMap(View v){
        Log.d("XX-RESET","True and removing "+lol);
        polyline.remove();
        //finalList.clear();
        polylines.remove(lol-1);
        lol--;

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(PUEBLA, DEFAULT_ZOOM));

        boolean success = googleMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.style_json)));
        if (!success)
            Toast.makeText(this, "Error cargando el estilo", Toast.LENGTH_LONG).show();

        mMap.setOnMapLongClickListener(MapsActivity.this);

        initMarker();
    }


    public void getMarkers(){
        if(marcadores.size()>1) {
            int i;
            for (i = 0; i < (marcadores.size()-1); i++) {
                Log.d("XX-RutaToFrom", marcadores.get(i).getId() + " to " + marcadores.get(i + 1).getId());
                calcularRuta(marcadores.get(i).getPosition(), marcadores.get(i + 1).getPosition());
            }
        }
    }

    public void calcularRuta(LatLng origin, LatLng destination){
        String serverKey = "AIzaSyB3ja-ZrTG2p09J86ZXfIGdM6vKy7HB378";
        GoogleDirection.withServerKey(serverKey)
                .from(origin)
                .to(destination)
                .execute(this);
    }


    public void dibujarRuta(){
        lala();
        Log.d("XX-FinalPolylineSize", ""+finalList.size());
        List<LatLng> finalPolyline = PolyUtil.simplify(finalList,2);
        Log.d("XX-PolylineSizeSimplify", ""+finalPolyline.size());
        polyline = mMap.addPolyline(new PolylineOptions().clickable(true));
        polyline.setPoints(finalPolyline);
        String polylineEncoded = PolyUtil.encode(finalPolyline);
        Log.d("XX-FinalPolyline",polylineEncoded);
        polyline.setStartCap(new CustomCap(
                BitmapDescriptorFactory.fromResource(R.drawable.start_point), 20));
        polyline.setEndCap(new CustomCap(
                BitmapDescriptorFactory.fromResource(R.drawable.end_point), 20));
    }

    public void lala(){
        List<LatLng> newList;
        Log.d("polilineasSize",""+polylines.size());
        for (int i = 0;  i < polylines.size(); i++) {
            Log.d("polilineasSize","IndexPoly"+i);
            newList = polylines.get(i).getPoints();
            for (int y = 1;  y < newList.size(); y++) {
                finalList.add(newList.get(y));
            }
        }
    }


    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        if(direction.isOK()) {
            final ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            polyline = mMap.addPolyline(new PolylineOptions().clickable(true).color(Color.BLUE).width(3));
            polyline.setPoints(directionPositionList);
            polylines.add(polyline);
            progressBar.setVisibility(GONE);
        }

    }

    @Override
    public void onDirectionFailure(Throwable t) {
        Toast.makeText(this, "Error al construir la ruta", Toast.LENGTH_LONG).show();
    }

    Marker newMarker;
    @Override
    public void onMapLongClick(LatLng point) {
        newMarker = mMap.addMarker(new MarkerOptions()
                .position(point)
                .title("m"+countMarkers)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .draggable(true));
        marcadores.add(newMarker);
        countMarkers++;
    }

    public void initMarker(){
        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {}
            @Override
            public void onMarkerDrag(Marker marker) {}

            @Override
            public void onMarkerDragEnd(Marker marker) {}
        });
    }

}
