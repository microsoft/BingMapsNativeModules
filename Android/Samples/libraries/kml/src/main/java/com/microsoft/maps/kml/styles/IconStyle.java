// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.kml.styles;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.microsoft.maps.MapImage;

public class IconStyle {

  private MapImage mImage;

  @Nullable
  public MapImage getImage() {
    return mImage;
  }

  public void setImage(@NonNull MapImage image) {
    mImage = image;
  }
}
