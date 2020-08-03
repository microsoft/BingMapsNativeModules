// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.moduletoolstest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.microsoft.maps.Geopath;
import com.microsoft.maps.Geopoint;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapImage;
import com.microsoft.maps.MapPolygon;
import com.microsoft.maps.MapPolyline;
import com.microsoft.maps.MockMapElementCollection;
import com.microsoft.maps.moduletools.MapFactories;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import org.mockito.Mockito;

public class MockParserMapFactories implements MapFactories {

  @Override
  public MapElementLayer createMapElementLayer() {
    MapElementLayer layer = Mockito.mock(MapElementLayer.class);

    MockMapElementCollection mockElementCollection = new MockMapElementCollection(layer);

    Mockito.doAnswer(invocation -> mockElementCollection).when(layer).getElements();

    return layer;
  }

  @Override
  public MapIcon createMapIcon() {
    MapIcon icon = Mockito.mock(MapIcon.class);

    // location must be final for use within the 'doAnswer' clause
    // AtomicReference used as a wrapper class to capture changes to location
    final AtomicReference<Geopoint> location = new AtomicReference<>();
    Mockito.doAnswer(
            invocation -> {
              location.set(invocation.getArgument(0));
              return true;
            })
        .when(icon)
        .setLocation(Mockito.any(Geopoint.class));
    Mockito.doAnswer(invocation -> location.get()).when(icon).getLocation();

    final AtomicReference<String> title = new AtomicReference<>();
    Mockito.doAnswer(
            invocation -> {
              title.set(invocation.getArgument(0));
              return true;
            })
        .when(icon)
        .setTitle(Mockito.any(String.class));
    Mockito.doAnswer(invocation -> title.get()).when(icon).getTitle();

    final AtomicReference<MapImage> image = new AtomicReference<MapImage>();
    Mockito.doAnswer(
            invocation -> {
              image.set(invocation.getArgument(0));
              return true;
            })
        .when(icon)
        .setImage(Mockito.any(MapImage.class));
    Mockito.doAnswer(invocation -> image.get()).when(icon).getImage();

    return icon;
  }

  @Override
  public MapPolyline createMapPolyline() {
    MapPolyline polyline = Mockito.mock(MapPolyline.class);

    // paths must be final for use within the 'doAnswer' clause
    // AtomicReference used as a wrapper class to capture changes to paths
    final AtomicReference<Geopath> paths = new AtomicReference();
    Mockito.doAnswer(
            invocation -> {
              paths.set(invocation.getArgument(0));
              return true;
            })
        .when(polyline)
        .setPath(Mockito.any(Geopath.class));
    Mockito.doAnswer(invocation -> paths.get()).when(polyline).getPath();

    return polyline;
  }

  @Override
  public MapPolygon createMapPolygon() {
    MapPolygon polygon = Mockito.mock(MapPolygon.class);

    // paths must be final for use within the 'doAnswer' clause
    // AtomicReference used as a wrapper class to capture changes to paths
    final AtomicReference<ArrayList<Geopath>> paths = new AtomicReference(new ArrayList<Geopath>());
    Mockito.doAnswer(
            invocation -> {
              paths.set(invocation.getArgument(0));
              return true;
            })
        .when(polygon)
        .setPaths(Mockito.anyList());

    Mockito.doAnswer(invocation -> paths.get()).when(polygon).getPaths();

    return polygon;
  }

  public MapImage createMapImage(InputStream inputStream) {
    MapImage mapImage = Mockito.mock(MapImage.class);

    // image must be final for use within the 'doAnswer' clause
    // AtomicReference used as a wrapper class to capture changes to inputStream
    final AtomicReference<Bitmap> image = new AtomicReference<>();
    image.set(BitmapFactory.decodeStream(inputStream));
    Mockito.doAnswer(invocation -> image.get()).when(mapImage).getBitmap();

    return mapImage;
  }
}
