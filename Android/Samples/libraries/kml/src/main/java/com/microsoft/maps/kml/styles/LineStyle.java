// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.kml.styles;

public class LineStyle {

  private int mWidth = 1;
  private int mStrokeColor = 0xffffffff;

  public LineStyle() {}

  public void setWidth(int width) {
    mWidth = width;
  }

  public int getWidth() {
    return mWidth;
  }

  public void setStrokeColor(int strokeColor) {
    mStrokeColor = strokeColor;
  }

  public int getStrokeColor() {
    return mStrokeColor;
  }
}
