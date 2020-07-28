// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.modules;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.microsoft.maps.Geopoint;
import com.microsoft.maps.MapAnimationKind;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapRenderMode;
import com.microsoft.maps.MapScene;
import com.microsoft.maps.MapStyleSheets;
import com.microsoft.maps.MapView;
import com.microsoft.maps.geojson.GeoJsonParseException;
import com.microsoft.maps.geojson.GeoJsonParser;
import com.microsoft.maps.geojson.MapGeoJsonLayer;
import com.microsoft.maps.kml.KMLParseException;
import com.microsoft.maps.kml.KMLParser;
import java.io.InputStream;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private static final Geopoint LAKE_WASHINGTON = new Geopoint(47.609466, -122.265185);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mMapView = new MapView(this, MapRenderMode.VECTOR);
    mMapView.setMapStyleSheet(MapStyleSheets.aerial());
    mMapView.setCredentialsKey(BuildConfig.CREDENTIALS_KEY);
    ((FrameLayout) findViewById(R.id.map_view)).addView(mMapView);

    new Parser().execute();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mMapView.setScene(
        MapScene.createFromLocationAndZoomLevel(LAKE_WASHINGTON, 10), MapAnimationKind.NONE);
  }

  @Override
  protected void onResume() {
    super.onResume();
    mMapView.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mMapView.onPause();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mMapView.onSaveInstanceState(outState);
  }

  @Override
  protected void onStop() {
    super.onStop();
    mMapView.onStop();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mMapView.onDestroy();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mMapView.onLowMemory();
  }

  private class Parser extends AsyncTask {

    private MapGeoJsonLayer mGeoJsonLayer;
    private MapElementLayer mKmlLayer;

    Parser() {}

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      Toast.makeText(getApplicationContext(), "Loading data...", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPostExecute(Object o) {
      super.onPostExecute(o);

      if (mGeoJsonLayer != null) {
        mGeoJsonLayer.setStrokeWidth(4);
        mGeoJsonLayer.setStrokeColor(Color.YELLOW);
        mMapView.getLayers().add(mGeoJsonLayer);
      } else {
        Toast.makeText(
                getApplicationContext(),
                "An error occurred loading GeoJSON data.",
                Toast.LENGTH_LONG)
            .show();
      }
      if (mKmlLayer != null) {
        mMapView.getLayers().add(mKmlLayer);
      } else {
        Toast.makeText(
                getApplicationContext(), "An error occurred loading KML data.", Toast.LENGTH_LONG)
            .show();
      }
    }

    @Override
    protected Void doInBackground(Object[] objects) {
      parseGeoJson();
      parseKML();
      return null;
    }

    private void parseKML() {
      InputStream is = getResources().openRawResource(R.raw.geoxml);
      String kml = new Scanner(is).useDelimiter("\\A").next();
      try {
        mKmlLayer = KMLParser.parse(kml);
      } catch (KMLParseException e) {
        e.printStackTrace();
      }
    }

    private void parseGeoJson() {
      InputStream is = getResources().openRawResource(R.raw.geojson);
      String geojson = new Scanner(is).useDelimiter("\\A").next();
      try {
        mGeoJsonLayer = GeoJsonParser.parse(geojson);
      } catch (GeoJsonParseException e) {
        e.printStackTrace();
      }
    }
  }
}
