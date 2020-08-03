// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.kml.styles;

import androidx.annotation.NonNull;

/** This class wraps all the styles that can be used with the Style element under one id. */
public class StylesHolder {

  private IconStyle mIconStyle = new IconStyle();
  private LineStyle mLineStyle = new LineStyle();

  public StylesHolder() {}

  @NonNull
  public IconStyle getIconStyle() {
    return mIconStyle;
  }

  @NonNull
  public LineStyle getLineStyle() {
    return mLineStyle;
  }
}
