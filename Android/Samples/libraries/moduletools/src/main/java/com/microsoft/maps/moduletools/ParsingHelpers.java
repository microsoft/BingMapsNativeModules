// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.moduletools;

import android.util.Log;
import androidx.annotation.NonNull;
import com.microsoft.maps.AltitudeReferenceSystem;
import com.microsoft.maps.Geoposition;
import java.util.ArrayList;

public class ParsingHelpers {

  public static void setAltitudesToZeroIfAtSurface(
      @NonNull ArrayList<Geoposition> positions, AltitudeReferenceSystem altitudeReferenceSystem) {
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
}
