// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.geojson;

import com.microsoft.maps.AltitudeReferenceSystem;
import com.microsoft.maps.Geopath;
import com.microsoft.maps.Geoposition;
import java.util.List;

class GeopathIndexed extends Geopath {

  private List<Geoposition> mPositions;

  public GeopathIndexed(List<Geoposition> positions) {
    super(positions);
    mPositions = positions;
  }

  GeopathIndexed(List<Geoposition> positions, AltitudeReferenceSystem altitudeReference) {
    super(positions, altitudeReference);
    mPositions = positions;
  }

  Geoposition get(int index) {
    return mPositions.get(index);
  }
}
