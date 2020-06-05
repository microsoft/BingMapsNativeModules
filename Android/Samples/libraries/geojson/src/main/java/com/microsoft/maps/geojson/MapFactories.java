package com.microsoft.maps.geojson;

import androidx.annotation.VisibleForTesting;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapPolygon;
import com.microsoft.maps.MapPolyline;

@VisibleForTesting
interface MapFactories {
  MapElementLayer createMapElementLayer();

  MapIcon createMapIcon();

  MapPolyline createMapPolyline();

  MapPolygon createMapPolygon();
}
