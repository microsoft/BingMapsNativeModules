// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.moduletools;

import com.microsoft.maps.AltitudeReferenceSystem;

/** Wrapper for AltitudeReferenceSystem to make it a mutable object. */
public class AltitudeReferenceSystemWrapper {

  private AltitudeReferenceSystem mAltitudeReferenceSystem;

  public AltitudeReferenceSystemWrapper(AltitudeReferenceSystem altitudeReferenceSystem) {
    mAltitudeReferenceSystem = altitudeReferenceSystem;
  }

  public AltitudeReferenceSystem getAltitudeReferenceSystem() {
    return mAltitudeReferenceSystem;
  }

  public void setAltitudeReferenceSystem(AltitudeReferenceSystem altitudeReferenceSystem) {
    mAltitudeReferenceSystem = altitudeReferenceSystem;
  }
}
