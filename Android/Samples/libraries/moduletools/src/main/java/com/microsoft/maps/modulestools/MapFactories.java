// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.modulestools;

import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapPolygon;
import com.microsoft.maps.MapPolyline;

public interface MapFactories {
  MapElementLayer createMapElementLayer();

  MapIcon createMapIcon();

  MapPolyline createMapPolyline();

  MapPolygon createMapPolygon();
}
