// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.kml;

import androidx.annotation.NonNull;

import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapPolygon;
import com.microsoft.maps.MapPolyline;
import com.microsoft.maps.modulestools.MapFactories;

/**
 * Class that parses KML and returns a new MapElementLayer containing all the shapes outlined in
 * the GeoJSON.
 *
 * <p>Created by Elizabeth Bartusiak (t-elbart) on 07/21/2020
 */

public class KMLParser {

  private MapElementLayer mLayer;

  private static final MapFactories DEFAULT_MAP_FACTORIES =
      new MapFactories() {
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
      };

  private KMLParser() {}

  @NonNull
  public static MapElementLayer parse(@NonNull String kml) throws KMLParseException {
    if (kml == null) {
      throw new IllegalArgumentException("Input String cannot be null.");
    }

    KMLParser instance = new KMLParser();
    try {
      return instance.internalParse(kml, DEFAULT_MAP_FACTORIES);
    } catch (Exception e) {
      throw new KMLParseException(e.getMessage());
    }
  }

  MapElementLayer internalParse(@NonNull String kml, @NonNull MapFactories factory) {
    return null;
  }

}
