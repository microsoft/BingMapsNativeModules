package com.microsoft.maps.geojson;

import static org.mockito.Mockito.doAnswer;

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
    fillColor.set(0xff0000ff);
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
    return null;
  }

  @Override
  public MapPolygon createMapPolygon() {
    MapPolygon polygon = Mockito.mock(MapPolygon.class);

    // fillColor must be final for use within the 'doAnswer' clause
    // AtomicReference used as a wrapper class to capture changes to fillColor
    final AtomicReference<Integer> fillColor = new AtomicReference();
    // set the default value
    fillColor.set(0xff0000ff);
    doAnswer(
            invocation -> {
              fillColor.set(invocation.getArgument(0));
              return true;
            })
        .when(polygon)
        .setFillColor(Mockito.anyInt());

    doAnswer(invocation -> fillColor.get()).when(polygon).getFillColor();
    return polygon;
  }
}
