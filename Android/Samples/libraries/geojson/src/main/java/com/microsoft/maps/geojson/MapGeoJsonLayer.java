package com.microsoft.maps.geojson;

import android.graphics.Color;
import com.microsoft.maps.MapElement;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapPolygon;

/**
 * Class returned by GeoJsonParser. Optionally, the overall style of the shapes in the layer can be
 * set (the same style will be applied to all applicable shapes). Visibility of shapes can be
 * filtered by type.
 *
 * <p>Created by Elizabeth Bartusiak (t-elbart) on 2020-06-08
 */
public class MapGeoJsonLayer extends MapElementLayer {

  private int mFillColor = Color.BLUE;

  public MapGeoJsonLayer() {
    super();
  }

  /**
   * Gets the ARGB format color to fill polygons.
   *
   * @return ARGB color to fill polygons.
   */
  public int getFillColor() {
    return mFillColor;
  }

  /**
   * Sets the ARGB fill color of polygons.
   *
   * @param fillColor the ARGB color to fill polygons
   */
  public void setFillColor(int fillColor) {
    if (fillColor != mFillColor) {
      mFillColor = fillColor;
      for (MapElement element : getElements()) {
        if (element instanceof MapPolygon) {
          ((MapPolygon) element).setFillColor(fillColor);
        }
      }
    }
  }
}
