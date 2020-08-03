// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.moduletools;

import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapImage;
import com.microsoft.maps.MapPolygon;
import com.microsoft.maps.MapPolyline;
import java.io.IOException;
import java.io.InputStream;

public interface MapFactories {
  MapElementLayer createMapElementLayer();

  MapIcon createMapIcon();

  MapPolyline createMapPolyline();

  MapPolygon createMapPolygon();

  MapImage createMapImage(InputStream inputStream) throws IOException;
}
