// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.geojson;

import com.microsoft.maps.Geoposition;

/** Class that extends Geoposition, implementing the toString() method. */
class GeopositionPrintable extends Geoposition {
  public GeopositionPrintable(double latitude, double longitude) {
    super(latitude, longitude);
  }

  public GeopositionPrintable(double latitude, double longitude, double altitude) {
    super(latitude, longitude, altitude);
  }

  @Override
  public String toString() {
    return "[" + getLongitude() + ", " + getLatitude() + ", " + getAltitude() + "]";
  }
}
