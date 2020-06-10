package com.microsoft.maps.geojson;

import static org.mockito.Mockito.doAnswer;

import android.graphics.Color;
import androidx.annotation.NonNull;
import com.microsoft.maps.MapElement;
import com.microsoft.maps.MapElementCollection;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapPolygon;
import com.microsoft.maps.MapPolyline;
import com.microsoft.maps.MockMapElementCollection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.mockito.Mockito;

class MockGeoJsonLayerMapFactories implements MapFactories {

  MapGeoJsonLayer createMapGeoJsonLayer() {
    MapGeoJsonLayer layer = Mockito.mock(MapGeoJsonLayer.class);
    MockMapElementCollection mockElementCollection = new MockMapElementCollection(layer);

    doAnswer(invocation -> mockElementCollection).when(layer).getElements();

    // fillColor must be final for use within the 'doAnswer' clause
    // AtomicReference used as a wrapper class to capture changes to fillColor
    final AtomicReference<Integer> fillColor = new AtomicReference();
    // set the default value
    fillColor.set(Color.BLUE);
    doAnswer(
            invocation -> {
              int color = invocation.getArgument(0);
              fillColor.set(color);

              for (MapElement element : mockElementCollection.getElements()) {
                if (element instanceof MapPolygon) {
                  ((MapPolygon) element).setFillColor(color);
                }
              }
              return true;
            })
        .when(layer)
        .setFillColor(Mockito.anyInt());

    doAnswer(invocation -> fillColor.get()).when(layer).getFillColor();

    final AtomicReference<Integer> strokeColor = new AtomicReference();
    // set the default value
    strokeColor.set(Color.BLUE);
    doAnswer(
            invocation -> {
              int color = invocation.getArgument(0);
              strokeColor.set(color);

              for (MapElement element : mockElementCollection.getElements()) {
                if (element instanceof MapPolygon) {
                  ((MapPolygon) element).setStrokeColor(color);
                } else if (element instanceof MapPolyline) {
                  ((MapPolyline) element).setStrokeColor(color);
                }
              }
              return true;
            })
        .when(layer)
        .setStrokeColor(Mockito.anyInt());

    doAnswer(invocation -> strokeColor.get()).when(layer).getStrokeColor();

    final AtomicReference<Boolean> isStrokeDashed = new AtomicReference();
    // set the default value
    isStrokeDashed.set(false);
    doAnswer(
            invocation -> {
              boolean arg = invocation.getArgument(0);
              if (isStrokeDashed.get() != arg) {
                isStrokeDashed.set(arg);
                for (MapElement element : mockElementCollection.getElements()) {
                  if (element instanceof MapPolygon) {
                    ((MapPolygon) element).setStrokeDashed(arg);
                  } else if (element instanceof MapPolyline) {
                    ((MapPolyline) element).setStrokeDashed(arg);
                  }
                }
              }
              return true;
            })
        .when(layer)
        .setStrokeDashed(Mockito.anyBoolean());

    doAnswer(invocation -> isStrokeDashed.get()).when(layer).getStrokeDashed();

    final AtomicReference<Integer> strokeWidth = new AtomicReference();
    // set the default value
    strokeWidth.set(1);
    doAnswer(
            invocation -> {
              int width = invocation.getArgument(0);
              if (strokeWidth.get() != width) {
                strokeWidth.set(width);
                for (MapElement element : mockElementCollection.getElements()) {
                  if (element instanceof MapPolygon) {
                    ((MapPolygon) element).setStrokeWidth(width);
                  } else if (element instanceof MapPolyline) {
                    ((MapPolyline) element).setStrokeWidth(width);
                  }
                }
              }
              return true;
            })
        .when(layer)
        .setStrokeWidth(Mockito.anyInt());

    doAnswer(invocation -> strokeWidth.get()).when(layer).getStrokeWidth();

    final AtomicReference<Boolean> polygonsVisible = new AtomicReference();
    // set the default value
    polygonsVisible.set(true);
    doAnswer(
            invocation -> {
              boolean arg = invocation.getArgument(0);
              if (polygonsVisible.get() != arg) {
                polygonsVisible.set(arg);
                for (MapElement element : mockElementCollection.getElements()) {
                  if (element instanceof MapPolygon) {
                    element.setVisible(arg);
                  }
                }
              }
              return true;
            })
        .when(layer)
        .setPolygonsVisible(Mockito.anyBoolean());

    doAnswer(invocation -> polygonsVisible.get()).when(layer).getPolygonsVisible();

    final AtomicReference<Boolean> polylinesVisible = new AtomicReference();
    // set the default value
    polylinesVisible.set(true);
    doAnswer(
            invocation -> {
              boolean arg = invocation.getArgument(0);
              if (polylinesVisible.get() != arg) {
                polylinesVisible.set(arg);
                for (MapElement element : mockElementCollection.getElements()) {
                  if (element instanceof MapPolyline) {
                    element.setVisible(arg);
                  }
                }
              }
              return true;
            })
        .when(layer)
        .setPolylinesVisible(Mockito.anyBoolean());

    doAnswer(invocation -> polylinesVisible.get()).when(layer).getPolylinesVisible();

    final AtomicReference<Boolean> iconsVisible = new AtomicReference();
    // set the default value
    iconsVisible.set(true);
    doAnswer(
            invocation -> {
              boolean arg = invocation.getArgument(0);
              if (iconsVisible.get() != arg) {
                iconsVisible.set(arg);
                for (MapElement element : mockElementCollection.getElements()) {
                  if (element instanceof MapIcon) {
                    element.setVisible(arg);
                  }
                }
              }
              return true;
            })
        .when(layer)
        .setIconsVisible(Mockito.anyBoolean());

    doAnswer(invocation -> iconsVisible.get()).when(layer).getIconsVisible();

    doAnswer(
            invocation -> {
              ArrayList<MapElement> elementsToRemove = new ArrayList<>();
              for (MapElement element : mockElementCollection.getElements()) {
                if (element instanceof MapPolygon) {
                  elementsToRemove.add(element);
                }
              }
              return removeAll(elementsToRemove, mockElementCollection);
            })
        .when(layer)
        .removePolygons();

    doAnswer(
            invocation -> {
              ArrayList<MapElement> elementsToRemove = new ArrayList<>();
              for (MapElement element : mockElementCollection.getElements()) {
                if (element instanceof MapPolyline) {
                  elementsToRemove.add(element);
                }
              }
              return removeAll(elementsToRemove, mockElementCollection);
            })
        .when(layer)
        .removePolylines();

    doAnswer(
            invocation -> {
              ArrayList<MapElement> elementsToRemove = new ArrayList<>();
              for (MapElement element : mockElementCollection.getElements()) {
                if (element instanceof MapIcon) {
                  elementsToRemove.add(element);
                }
              }
              return removeAll(elementsToRemove, mockElementCollection);
            })
        .when(layer)
        .removeIcons();

    return layer;
  }

  /* Not used, implemented for interface */
  @Override
  public MapElementLayer createMapElementLayer() {
    return null;
  }

  @Override
  public MapIcon createMapIcon() {
    MapIcon icon = Mockito.mock(MapIcon.class);

    final AtomicReference<Boolean> isVisible = new AtomicReference();
    isVisible.set(true);
    doAnswer(
            invocation -> {
              isVisible.set(invocation.getArgument(0));
              return true;
            })
        .when(icon)
        .setVisible(Mockito.anyBoolean());

    doAnswer(invocation -> isVisible.get()).when(icon).isVisible();

    return icon;
  }

  @Override
  public MapPolyline createMapPolyline() {
    MapPolyline polyine = Mockito.mock(MapPolyline.class);

    final AtomicReference<Integer> strokeColor = new AtomicReference();
    // set the default value
    strokeColor.set(Color.BLUE);
    doAnswer(
            invocation -> {
              strokeColor.set(invocation.getArgument(0));
              return true;
            })
        .when(polyine)
        .setStrokeColor(Mockito.anyInt());

    doAnswer(invocation -> strokeColor.get()).when(polyine).getStrokeColor();

    final AtomicReference<Boolean> isStrokeDashed = new AtomicReference();
    doAnswer(
            invocation -> {
              isStrokeDashed.set(invocation.getArgument(0));
              return true;
            })
        .when(polyine)
        .setStrokeDashed(Mockito.anyBoolean());

    doAnswer(invocation -> isStrokeDashed.get()).when(polyine).isStrokeDashed();

    final AtomicReference<Integer> strokeWidth = new AtomicReference();
    doAnswer(
            invocation -> {
              strokeWidth.set(invocation.getArgument(0));
              return true;
            })
        .when(polyine)
        .setStrokeWidth(Mockito.anyInt());

    doAnswer(invocation -> strokeWidth.get()).when(polyine).getStrokeWidth();

    final AtomicReference<Boolean> isVisible = new AtomicReference();
    isVisible.set(true);
    doAnswer(
            invocation -> {
              isVisible.set(invocation.getArgument(0));
              return true;
            })
        .when(polyine)
        .setVisible(Mockito.anyBoolean());

    doAnswer(invocation -> isVisible.get()).when(polyine).isVisible();

    return polyine;
  }

  @Override
  public MapPolygon createMapPolygon() {
    MapPolygon polygon = Mockito.mock(MapPolygon.class);

    final AtomicReference<Integer> fillColor = new AtomicReference();
    // set the default value
    fillColor.set(Color.BLUE);
    doAnswer(
            invocation -> {
              fillColor.set(invocation.getArgument(0));
              return true;
            })
        .when(polygon)
        .setFillColor(Mockito.anyInt());

    doAnswer(invocation -> fillColor.get()).when(polygon).getFillColor();

    final AtomicReference<Integer> strokeColor = new AtomicReference();
    // set the default value
    strokeColor.set(Color.BLUE);
    doAnswer(
            invocation -> {
              strokeColor.set(invocation.getArgument(0));
              return true;
            })
        .when(polygon)
        .setStrokeColor(Mockito.anyInt());

    doAnswer(invocation -> strokeColor.get()).when(polygon).getStrokeColor();

    final AtomicReference<Boolean> isStrokeDashed = new AtomicReference();
    doAnswer(
            invocation -> {
              isStrokeDashed.set(invocation.getArgument(0));
              return true;
            })
        .when(polygon)
        .setStrokeDashed(Mockito.anyBoolean());

    doAnswer(invocation -> isStrokeDashed.get()).when(polygon).isStrokeDashed();

    final AtomicReference<Integer> strokeWidth = new AtomicReference();
    doAnswer(
            invocation -> {
              strokeWidth.set(invocation.getArgument(0));
              return true;
            })
        .when(polygon)
        .setStrokeWidth(Mockito.anyInt());

    doAnswer(invocation -> strokeWidth.get()).when(polygon).getStrokeWidth();

    final AtomicReference<Boolean> isVisible = new AtomicReference();
    isVisible.set(true);
    doAnswer(
            invocation -> {
              isVisible.set(invocation.getArgument(0));
              return true;
            })
        .when(polygon)
        .setVisible(Mockito.anyBoolean());

    doAnswer(invocation -> isVisible.get()).when(polygon).isVisible();

    return polygon;
  }

  @NonNull
  private List<MapElement> removeAll(
      @NonNull ArrayList<MapElement> elementsToRemove, @NonNull MapElementCollection elements) {
    ArrayList<MapElement> removedList = new ArrayList<>();
    for (int i = 0; i < elementsToRemove.size(); i++) {
      MapElement element = elementsToRemove.get(i);
      removedList.add(element);
      elements.remove(element);
    }
    return removedList;
  }
}
