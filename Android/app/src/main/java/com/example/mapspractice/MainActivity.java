package com.example.mapspractice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapRenderMode;
import com.microsoft.maps.MapView;
import com.microsoft.maps.Geopoint;
import com.microsoft.maps.MapAnimationKind;
import com.microsoft.maps.MapScene;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

  private MapView mMapView;
  private static final Geopoint LAKE_WASHINGTON = new Geopoint(47.609466, -122.265185);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mMapView = new MapView(this, MapRenderMode.VECTOR);
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

    private MapElementLayer mParsedLayer;

    Parser() {}

    @Override
    protected void onPreExecute() {
      super.onPreExecute();

      Toast.makeText(getApplicationContext(), "Loading data...", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPostExecute(Object o) {
      super.onPostExecute(o);
      mMapView.getLayers().add(mParsedLayer);
    }

    @Override
    protected Void doInBackground(Object[] objects) {

      String geojson;
      InputStream is = null;
      try {
        is = getResources().openRawResource(R.raw.countries);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        // is.close();
        geojson = new String(buffer, "UTF-8");

        // profile parsing time
        long startTime = System.nanoTime();
        mParsedLayer = GeoJsonParser.parse(geojson);
        Log.d("time parsed:", "total: " + ((System.nanoTime() - startTime) / 1000000) + "mS\n");

      } catch (IOException | JSONException e) {
        e.printStackTrace();
      } finally {
          try {
              is.close();
          } catch (IOException e) {
              e.printStackTrace();
          }
      }

      return null;
    }
  }
}
