// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.moduletoolstest;

import static org.junit.Assert.assertEquals;

import androidx.annotation.NonNull;
import com.microsoft.maps.Geoposition;

public class TestHelpers {

  /** Used for asserting correctness of coordinates of a Geoposition. */
  public static void assertPositionEquals(
      @NonNull double[] expectedPoints, @NonNull Geoposition position) {
    assertEquals(expectedPoints[0], position.getLongitude(), 0);
    assertEquals(expectedPoints[1], position.getLatitude(), 0);
    if (expectedPoints.length > 2) {
      assertEquals(expectedPoints[2], position.getAltitude(), 0);
    } else {
      assertEquals(0.0, position.getAltitude(), 0);
    }
  }
}
