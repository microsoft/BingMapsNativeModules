// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.kml;

import org.junit.Test;

public class KMLParserTest {

  /**
   * Tests the public method to catch null. Note: parse(null) will not call internalParse with null.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testNullKMLThrowsException() throws KMLParseException {
    KMLParser.parse(null);
  }
}