package com.microsoft.maps.moduletools;

import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapPolygon;
import com.microsoft.maps.MapPolyline;

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
}
