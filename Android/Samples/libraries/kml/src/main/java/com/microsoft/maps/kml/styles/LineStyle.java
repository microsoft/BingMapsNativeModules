// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.kml.styles;

public class LineStyle {

  private boolean mUseWidth = false;
  private int mWidth = 1;
  private boolean mUseStrokeColor = false;
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

  public boolean useWidth() {
    return mUseWidth;
  }

  public void setUseWidth(boolean useWidth) {
    mUseWidth = useWidth;
  }

  public boolean useStrokeColor() {
    return mUseStrokeColor;
  }

  public void setUseStrokeColor(boolean useStrokeColor) {
    mUseStrokeColor = useStrokeColor;
  }
}
