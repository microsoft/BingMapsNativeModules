package com.microsoft.maps.geojson;

import android.graphics.Color;
import com.microsoft.maps.MapElement;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapPolygon;
import com.microsoft.maps.MapPolyline;

/**
 * Class returned by GeoJsonParser. Optionally, the overall style of the shapes in the layer can be
 * set (the same style will be applied to all applicable shapes). Visibility of shapes can be
 * filtered by type.
 *
 * <p>Created by Elizabeth Bartusiak (t-elbart) on 2020-06-08
 */
public class MapGeoJsonLayer extends MapElementLayer {

  private int mFillColor = Color.BLUE;
  private int mStrokeColor = Color.BLUE;
  private boolean mIsStrokeDashed;
  private int mStrokeWidth = 1;

  private boolean mArePolygonsVisible;
  private boolean mArePolylinesVisible;
  private boolean mAreIconsVisible;

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

  /**
   * Gets the ARGB format color used to outline polygons and draw polylines.
   *
   * @return ARGB format color
   */
  public int getStrokeColor() {
    return mStrokeColor;
  }

  /**
   * Sets the ARGB format color used to outline polygons and draw polylines.
   *
   * @param strokeColor the ARGB format color
   */
  public void setStrokeColor(int strokeColor) {
    if (strokeColor != mStrokeColor) {
      mStrokeColor = strokeColor;
      for (MapElement element : getElements()) {
        if (element instanceof MapPolygon) {
          ((MapPolygon) element).setStrokeColor(strokeColor);
        } else if (element instanceof MapPolyline) {
          ((MapPolyline) element).setStrokeColor(strokeColor);
        }
      }
    }
  }

  /**
   * Tells whether the lines are dashed in polylines and polygons.
   *
   * @return boolean value of dashed or not
   */
  public boolean getStrokeDashed() {
    return mIsStrokeDashed;
  }

  /**
   * Sets whether the lines are dashed or not in polygons and polylines.
   *
   * @param isStrokeDashed whether the lines should be dashed or not
   */
  public void setStrokeDashed(boolean isStrokeDashed) {
    if (mIsStrokeDashed != isStrokeDashed) {
      mIsStrokeDashed = isStrokeDashed;
      for (MapElement element : getElements()) {
        if (element instanceof MapPolygon) {
          ((MapPolygon) element).setStrokeDashed(isStrokeDashed);
        } else if (element instanceof MapPolyline) {
          ((MapPolyline) element).setStrokeDashed(isStrokeDashed);
        }
      }
    }
  }

  /**
   * Gets the line width of polylines and the outline of polygons.
   *
   * @return int value of line width
   */
  public int getStrokeWidth() {
    return mStrokeWidth;
  }

  /**
   * Sets the line width for polylines and the outline of polygons.
   *
   * @param strokeWidth int value of line width
   */
  public void setStrokeWidth(int strokeWidth) {
    if (mStrokeWidth != strokeWidth) {
      mStrokeWidth = strokeWidth;
      for (MapElement element : getElements()) {
        if (element instanceof MapPolygon) {
          ((MapPolygon) element).setStrokeWidth(strokeWidth);
        } else if (element instanceof MapPolyline) {
          ((MapPolyline) element).setStrokeWidth(strokeWidth);
        }
      }
    }
  }

  /**
   * Tells whether polygons are visible or not.
   *
   * @return boolean true if polygons visible, false otherwise
   */
  public boolean getPolygonsVisible() {
    return mArePolygonsVisible;
  }

  /**
   * Sets whether polygons are visible or not.
   *
   * @param visible boolean value for whether polygons are visible or not
   */
  public void setPolygonsVisible(boolean visible) {
    if (mArePolygonsVisible != visible) {
      mArePolygonsVisible = visible;
      for (MapElement element : getElements()) {
        if (element instanceof MapPolygon) {
          element.setVisible(visible);
        }
      }
    }
  }

  /**
   * Tells whether polylines are visible or not.
   *
   * @return boolean true if polylines visible, false otherwise
   */
  public boolean getPolylinesVisible() {
    return mArePolylinesVisible;
  }

  /**
   * Sets whether polylines are visible or not.
   *
   * @param visible boolean value for whether polylines are visible or not
   */
  public void setPolylinesVisible(boolean visible) {
    if (mArePolylinesVisible != visible) {
      mArePolylinesVisible = visible;
      for (MapElement element : getElements()) {
        if (element instanceof MapPolyline) {
          element.setVisible(visible);
        }
      }
    }
  }

  /**
   * Tells whether icons are visible or not.
   *
   * @return boolean true if icons visible, false otherwise
   */
  public boolean getIconsVisible() {
    return mArePolylinesVisible;
  }

  /**
   * Sets whether icons are visible or not.
   *
   * @param visible boolean value for whether icons are visible or not
   */
  public void setIconsVisible(boolean visible) {
    if (mAreIconsVisible != visible) {
      mAreIconsVisible = visible;
      for (MapElement element : getElements()) {
        if (element instanceof MapIcon) {
          element.setVisible(visible);
        }
      }
    }
  }
}
