package com.microsoft.maps.geojson;

import static org.mockito.Mockito.doAnswer;

import android.graphics.Color;
import com.microsoft.maps.MapElement;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapPolygon;
import com.microsoft.maps.MapPolyline;
import com.microsoft.maps.MockMapElementCollection;
import java.util.concurrent.atomic.AtomicReference;
import org.mockito.Mockito;

class MockGeoJsonLayerMapFactories implements MapFactories {

  MapGeoJsonLayer createMapGeoJsonLayer() {
    MapGeoJsonLayer layer = Mockito.mock(MapGeoJsonLayer.class);
    MockMapElementCollection mockElementCollection = new MockMapElementCollection(layer);

    doAnswer(invocation -> mockElementCollection).when(layer).getElements();

    // fillColor must be final for use within the 'doAnswer' clause
    // AtomicReference used as a wrapper class to capture changes to fillColor
    final AtomicReference<Integer> fillColor = new AtomicReference();
    // set the default value
    fillColor.set(Color.BLUE);
    doAnswer(
            invocation -> {
              int color = invocation.getArgument(0);
              fillColor.set(color);

              for (MapElement element : mockElementCollection.getElements()) {
                if (element instanceof MapPolygon) {
                  ((MapPolygon) element).setFillColor(color);
                }
              }
              return true;
            })
        .when(layer)
        .setFillColor(Mockito.anyInt());

    doAnswer(invocation -> fillColor.get()).when(layer).getFillColor();

    final AtomicReference<Integer> strokeColor = new AtomicReference();
    // set the default value
    strokeColor.set(Color.BLUE);
    doAnswer(
            invocation -> {
              int color = invocation.getArgument(0);
              strokeColor.set(color);

              for (MapElement element : mockElementCollection.getElements()) {
                if (element instanceof MapPolygon) {
                  ((MapPolygon) element).setStrokeColor(color);
                } else if (element instanceof MapPolyline) {
                  ((MapPolyline) element).setStrokeColor(color);
                }
              }
              return true;
            })
        .when(layer)
        .setStrokeColor(Mockito.anyInt());

    doAnswer(invocation -> strokeColor.get()).when(layer).getStrokeColor();

    final AtomicReference<Boolean> isStrokeDashed = new AtomicReference();
    // set the default value
    isStrokeDashed.set(false);
    doAnswer(
            invocation -> {
              boolean arg = invocation.getArgument(0);
              if (isStrokeDashed.get() != arg) {
                isStrokeDashed.set(arg);
                for (MapElement element : mockElementCollection.getElements()) {
                  if (element instanceof MapPolygon) {
                    ((MapPolygon) element).setStrokeDashed(arg);
                  } else if (element instanceof MapPolyline) {
                    ((MapPolyline) element).setStrokeDashed(arg);
                  }
                }
              }
              return true;
            })
        .when(layer)
        .setStrokeDashed(Mockito.anyBoolean());

    doAnswer(invocation -> isStrokeDashed.get()).when(layer).getStrokeDashed();
    return layer;
  }

  /* Not used, implemented for interface */
  @Override
  public MapElementLayer createMapElementLayer() {
    return null;
  }

  @Override
  public MapIcon createMapIcon() {
    return null;
  }

  @Override
  public MapPolyline createMapPolyline() {
    MapPolyline polyine = Mockito.mock(MapPolyline.class);

    final AtomicReference<Integer> strokeColor = new AtomicReference();
    // set the default value
    strokeColor.set(Color.BLUE);
    doAnswer(
            invocation -> {
              strokeColor.set(invocation.getArgument(0));
              return true;
            })
        .when(polyine)
        .setStrokeColor(Mockito.anyInt());

    doAnswer(invocation -> strokeColor.get()).when(polyine).getStrokeColor();

    final AtomicReference<Boolean> isStrokeDashed = new AtomicReference();
    doAnswer(
            invocation -> {
              isStrokeDashed.set(invocation.getArgument(0));
              return true;
            })
        .when(polyine)
        .setStrokeDashed(Mockito.anyBoolean());

    doAnswer(invocation -> isStrokeDashed.get()).when(polyine).isStrokeDashed();

    return polyine;
  }

  @Override
  public MapPolygon createMapPolygon() {
    MapPolygon polygon = Mockito.mock(MapPolygon.class);

    final AtomicReference<Integer> fillColor = new AtomicReference();
    // set the default value
    fillColor.set(Color.BLUE);
    doAnswer(
            invocation -> {
              fillColor.set(invocation.getArgument(0));
              return true;
            })
        .when(polygon)
        .setFillColor(Mockito.anyInt());

    doAnswer(invocation -> fillColor.get()).when(polygon).getFillColor();

    final AtomicReference<Integer> strokeColor = new AtomicReference();
    // set the default value
    strokeColor.set(Color.BLUE);
    doAnswer(
            invocation -> {
              strokeColor.set(invocation.getArgument(0));
              return true;
            })
        .when(polygon)
        .setStrokeColor(Mockito.anyInt());

    doAnswer(invocation -> strokeColor.get()).when(polygon).getStrokeColor();

    final AtomicReference<Boolean> isStrokeDashed = new AtomicReference();
    doAnswer(
            invocation -> {
              isStrokeDashed.set(invocation.getArgument(0));
              return true;
            })
        .when(polygon)
        .setStrokeDashed(Mockito.anyBoolean());

    doAnswer(invocation -> isStrokeDashed.get()).when(polygon).isStrokeDashed();

    return polygon;
  }
}
