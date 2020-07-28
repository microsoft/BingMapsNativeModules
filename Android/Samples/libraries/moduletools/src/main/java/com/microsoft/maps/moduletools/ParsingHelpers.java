// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.moduletools;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.microsoft.maps.AltitudeReferenceSystem;
import com.microsoft.maps.Geoposition;
import java.util.ArrayList;

public class ParsingHelpers {

  public static void setAltitudesToZeroIfAtSurface(
      @NonNull ArrayList<Geoposition> positions,
      @NonNull AltitudeReferenceSystem altitudeReferenceSystem) {
    if (altitudeReferenceSystem == AltitudeReferenceSystem.SURFACE) {
      for (Geoposition position : positions) {
        position.setAltitude(0);
      }
    }
  }

  public static void logAltitudeWarning() {
    Log.w(
        "Altitude",
        "Unless all positions in a Geometry Object contain an altitude coordinate, all altitudes will be set to 0 at surface level for that Geometry Object.");
  }

  @Nullable
  public static String verifyPolygonRing(@NonNull ArrayList<Geoposition> positions) {
    if (positions.size() < 4) {
      StringBuilder positionsStringBuilder = new StringBuilder();
      for (int i = 0; i < positions.size() - 1; i++) {
        Geoposition position = positions.get(i);
        positionsStringBuilder
            .append(
                "["
                    + position.getLatitude()
                    + ", "
                    + position.getLongitude()
                    + ", "
                    + position.getAltitude()
                    + "]")
            .append(", ");
      }
      positionsStringBuilder.append(positions.get(positions.size() - 1));
      return "Polygon ring must have at least 4 positions, and the first and last position must "
          + "be the same. Instead saw Geopositions: ["
          + positionsStringBuilder
          + "].";
    }

    Geoposition firstPosition = positions.get(0);
    Geoposition lastPosition = positions.get(positions.size() - 1);
    if (firstPosition.getLongitude() != lastPosition.getLongitude()
        || firstPosition.getLatitude() != lastPosition.getLatitude()
        || firstPosition.getAltitude() != lastPosition.getAltitude()) {
      return "First and last coordinate pair of each polygon ring must be the same. "
          + " Instead saw Geopositions: first: "
          + "["
          + firstPosition.getLatitude()
          + ", "
          + firstPosition.getLongitude()
          + ", "
          + firstPosition.getAltitude()
          + "]"
          + " last: "
          + "["
          + lastPosition.getLatitude()
          + ", "
          + lastPosition.getLongitude()
          + ", "
          + lastPosition.getAltitude()
          + "]";
    }
    return null;
  }
}
