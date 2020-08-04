// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.kml.styles;

public class PolyStyle {

  private boolean mShouldFill = true;
  private boolean mShouldOutline = true;
  private int mFillColor = 0xffffffff;

  public PolyStyle() {}

  public void setShouldFill(boolean shouldFill) {
    mShouldFill = shouldFill;
  }

  public boolean shouldFill() {
    return mShouldFill;
  }

  public void setShouldOutline(boolean shouldOutline) {
    mShouldOutline = shouldOutline;
  }

  public boolean shouldOutline() {
    return mShouldOutline;
  }

  public void setFillColor(int fillColor) {
    mFillColor = fillColor;
  }

  public int getFillColor() {
    return mFillColor;
  }

  public int getTransparent() {
    return 0x00ffffff;
  }
}
