// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.geojson;

import static org.mockito.Mockito.doAnswer;

import com.microsoft.maps.Geopath;
import com.microsoft.maps.Geopoint;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapPolygon;
import com.microsoft.maps.MapPolyline;
import com.microsoft.maps.MockMapElementCollection;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import org.mockito.Mockito;

class MockParserMapFactories implements MapFactories {

  @Override
  public MapElementLayer createMapElementLayer() {
    MapElementLayer layer = Mockito.mock(MapElementLayer.class);

    MockMapElementCollection mockElementCollection = new MockMapElementCollection(layer);

    doAnswer(invocation -> mockElementCollection).when(layer).getElements();

    return layer;
  }

  @Override
  public MapIcon createMapIcon() {
    MapIcon icon = Mockito.mock(MapIcon.class);

    // location must be final for use within the 'doAnswer' clause
    // AtomicReference used as a wrapper class to capture changes to location
    final AtomicReference<Geopoint> location = new AtomicReference<>();
    doAnswer(
            invocation -> {
              location.set(invocation.getArgument(0));
              return true;
            })
        .when(icon)
        .setLocation(Mockito.any(Geopoint.class));
    doAnswer(invocation -> location.get()).when(icon).getLocation();

    return icon;
  }

  @Override
  public MapPolyline createMapPolyline() {
    MapPolyline polyline = Mockito.mock(MapPolyline.class);

    // paths must be final for use within the 'doAnswer' clause
    // AtomicReference used as a wrapper class to capture changes to paths
    final AtomicReference<Geopath> paths = new AtomicReference();
    doAnswer(
            invocation -> {
              paths.set(invocation.getArgument(0));
              return true;
            })
        .when(polyline)
        .setPath(Mockito.any(Geopath.class));
    doAnswer(invocation -> paths.get()).when(polyline).getPath();

    return polyline;
  }

  @Override
  public MapPolygon createMapPolygon() {
    MapPolygon polygon = Mockito.mock(MapPolygon.class);

    // paths must be final for use within the 'doAnswer' clause
    // AtomicReference used as a wrapper class to capture changes to paths
    final AtomicReference<ArrayList<Geopath>> paths = new AtomicReference(new ArrayList<Geopath>());
    doAnswer(
            invocation -> {
              paths.set(invocation.getArgument(0));
              return true;
            })
        .when(polygon)
        .setPaths(Mockito.anyList());

    doAnswer(invocation -> paths.get()).when(polygon).getPaths();

    return polygon;
  }
}
