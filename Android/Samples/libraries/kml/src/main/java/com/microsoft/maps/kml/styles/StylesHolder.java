// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.kml.styles;

import androidx.annotation.NonNull;

/** This class wraps all the styles that can be used with the Style element under one id. */
public class StylesHolder {

  private final IconStyle mIconStyle = new IconStyle();
  private final LineStyle mLineStyle = new LineStyle();
  private final PolyStyle mPolyStyle = new PolyStyle();

  public StylesHolder() {}

  @NonNull
  public IconStyle getIconStyle() {
    return mIconStyle;
  }

  @NonNull
  public LineStyle getLineStyle() {
    return mLineStyle;
  }

  @NonNull
  public PolyStyle getPolyStyle() {
    return mPolyStyle;
  }
}
