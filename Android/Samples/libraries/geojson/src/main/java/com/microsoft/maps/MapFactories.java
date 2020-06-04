package com.microsoft.maps;

import androidx.annotation.VisibleForTesting;

@VisibleForTesting
interface MapFactories {
  MapElementLayer createMapElementLayer();

  MapIcon createMapIcon();

  MapPolyline createMapPolyline();

  MapPolygon createMapPolygon();
}
