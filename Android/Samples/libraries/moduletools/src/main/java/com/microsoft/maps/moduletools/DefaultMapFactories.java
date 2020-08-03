// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.moduletools;

import androidx.annotation.NonNull;

import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapImage;
import com.microsoft.maps.MapPolygon;
import com.microsoft.maps.MapPolyline;
import java.io.IOException;
import java.io.InputStream;

public class DefaultMapFactories implements MapFactories {

  @Override
  public MapElementLayer createMapElementLayer() {
    return new MapElementLayer();
  }

  @Override
  public MapIcon createMapIcon() {
    return new MapIcon();
  }

  @Override
  public MapPolyline createMapPolyline() {
    return new MapPolyline();
  }

  @Override
  public MapPolygon createMapPolygon() {
    return new MapPolygon();
  }

  @Override
  public MapImage createMapImage(@NonNull InputStream inputStream) throws IOException {
    return new MapImage(inputStream);
  }
}
