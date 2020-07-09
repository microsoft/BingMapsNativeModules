// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.geojson;

import com.microsoft.maps.AltitudeReferenceSystem;

/** Wrapper for AltitudeReferenceSystem to make it an object. */
class AltitudeReferenceSystemWrapper {

  private AltitudeReferenceSystem mAltitudeReferenceSystem;

  AltitudeReferenceSystemWrapper(AltitudeReferenceSystem altitudeReferenceSystem) {
    mAltitudeReferenceSystem = altitudeReferenceSystem;
  }

  AltitudeReferenceSystem getAltitudeReferenceSystem() {
    return mAltitudeReferenceSystem;
  }

  void setAltitudeReferenceSystem(AltitudeReferenceSystem altitudeReferenceSystem) {
    mAltitudeReferenceSystem = altitudeReferenceSystem;
  }
}
