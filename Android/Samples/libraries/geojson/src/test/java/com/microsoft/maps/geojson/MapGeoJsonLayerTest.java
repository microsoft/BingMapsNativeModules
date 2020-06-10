package com.microsoft.maps.geojson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Color;
import com.microsoft.maps.MapElement;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapPolygon;
import com.microsoft.maps.MapPolyline;
import com.microsoft.maps.MockBingMapsLoader;
import com.microsoft.maps.MockMapElementCollection;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class MapGeoJsonLayerTest {

  private static final MockGeoJsonLayerMapFactories MOCK_MAP_FACTORIES =
      new MockGeoJsonLayerMapFactories();

  @Before
  public void setup() {
    MockBingMapsLoader.mockInitialize();
  }

  @Test
  public void testAddPolygon() {
    MapGeoJsonLayer layer = MOCK_MAP_FACTORIES.createMapGeoJsonLayer();
    layer.getElements().add(MOCK_MAP_FACTORIES.createMapPolygon());
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    MapPolygon polygon = (MapPolygon) elementCollection.getElements().get(0);
    assertNotNull(polygon);
  }

  @Test
  public void testAddPolyline() {
    MapGeoJsonLayer layer = MOCK_MAP_FACTORIES.createMapGeoJsonLayer();
    layer.getElements().add(MOCK_MAP_FACTORIES.createMapPolyline());
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    MapPolyline polyline = (MapPolyline) elementCollection.getElements().get(0);
    assertNotNull(polyline);
  }

  @Test
  public void testGetFillColorDefault() {
    MapGeoJsonLayer layer = MOCK_MAP_FACTORIES.createMapGeoJsonLayer();
    assertEquals(Color.BLUE, layer.getFillColor());
    layer.getElements().add(MOCK_MAP_FACTORIES.createMapPolygon());
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    MapPolygon polygon = (MapPolygon) elementCollection.getElements().get(0);
    assertNotNull(polygon);
    assertEquals(Color.BLUE, polygon.getFillColor());
  }

  @Test
  public void testSetFillColor() {
    MapGeoJsonLayer layer = MOCK_MAP_FACTORIES.createMapGeoJsonLayer();
    layer.getElements().add(MOCK_MAP_FACTORIES.createMapPolygon());
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    assertEquals(Color.BLUE, layer.getFillColor());
    layer.setFillColor(Color.GREEN);
    assertEquals(Color.GREEN, layer.getFillColor());
    MapPolygon polygon = (MapPolygon) elementCollection.getElements().get(0);
    assertNotNull(polygon);
    assertEquals(Color.GREEN, polygon.getFillColor());
  }

  @Test
  public void testSetFillColorMultiplePolygons() {
    MapGeoJsonLayer layer = MOCK_MAP_FACTORIES.createMapGeoJsonLayer();
    for (int i = 0; i < 5; i++) {
      layer.getElements().add(MOCK_MAP_FACTORIES.createMapPolygon());
    }
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(5, elementCollection.getElements().size());
    layer.setFillColor(Color.GREEN);
    assertEquals(Color.GREEN, layer.getFillColor());
    for (int i = 0; i < 5; i++) {
      MapPolygon polygon = (MapPolygon) elementCollection.getElements().get(0);
      assertNotNull(polygon);
      assertEquals(Color.GREEN, polygon.getFillColor());
    }
  }

  @Test
  public void testGetStrokeColorDefault() {
    MapGeoJsonLayer layer = MOCK_MAP_FACTORIES.createMapGeoJsonLayer();
    assertEquals(Color.BLUE, layer.getStrokeColor());
  }

  @Test
  public void testSetStrokeColor() {
    MapGeoJsonLayer layer = MOCK_MAP_FACTORIES.createMapGeoJsonLayer();
    layer.getElements().add(MOCK_MAP_FACTORIES.createMapPolygon());
    layer.getElements().add(MOCK_MAP_FACTORIES.createMapPolyline());
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(2, elementCollection.getElements().size());
    assertEquals(Color.BLUE, layer.getStrokeColor());
    layer.setStrokeColor(Color.GREEN);
    assertEquals(Color.GREEN, layer.getStrokeColor());

    MapPolygon polygon = (MapPolygon) elementCollection.getElements().get(0);
    assertNotNull(polygon);
    assertEquals(Color.GREEN, polygon.getStrokeColor());

    MapPolyline polyline = (MapPolyline) elementCollection.getElements().get(1);
    assertNotNull(polyline);
    assertEquals(Color.GREEN, polyline.getStrokeColor());
  }

  @Test
  public void testSetStrokeColorMultiplePolygonsPolylines() {
    MapGeoJsonLayer layer = MOCK_MAP_FACTORIES.createMapGeoJsonLayer();
    for (int i = 0; i < 2; i++) {
      layer.getElements().add(MOCK_MAP_FACTORIES.createMapPolygon());
      layer.getElements().add(MOCK_MAP_FACTORIES.createMapPolyline());
    }
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(4, elementCollection.getElements().size());
    layer.setStrokeColor(Color.GREEN);
    assertEquals(Color.GREEN, layer.getStrokeColor());
    for (int i = 0; i < 4; i += 2) {
      MapPolygon polygon = (MapPolygon) elementCollection.getElements().get(i);
      assertNotNull(polygon);
      assertEquals(Color.GREEN, polygon.getStrokeColor());

      MapPolyline polyline = (MapPolyline) elementCollection.getElements().get(i + 1);
      assertNotNull(polyline);
      assertEquals(Color.GREEN, polygon.getStrokeColor());
    }
  }

  @Test
  public void testGetStrokeDashedDefault() {
    MapGeoJsonLayer layer = MOCK_MAP_FACTORIES.createMapGeoJsonLayer();
    assertFalse(layer.getStrokeDashed());
  }

  @Test
  public void testSetStrokeDashedPolygonsPolylines() {
    MapGeoJsonLayer layer = MOCK_MAP_FACTORIES.createMapGeoJsonLayer();
    for (int i = 0; i < 2; i++) {
      layer.getElements().add(MOCK_MAP_FACTORIES.createMapPolygon());
      layer.getElements().add(MOCK_MAP_FACTORIES.createMapPolyline());
    }
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(4, elementCollection.getElements().size());
    layer.setStrokeDashed(true);
    assertTrue(layer.getStrokeDashed());
    for (int i = 0; i < 4; i += 2) {
      MapPolygon polygon = (MapPolygon) elementCollection.getElements().get(i);
      assertNotNull(polygon);
      assertTrue(polygon.isStrokeDashed());

      MapPolyline polyline = (MapPolyline) elementCollection.getElements().get(i + 1);
      assertNotNull(polyline);
      assertTrue(polyline.isStrokeDashed());
    }
  }

  @Test
  public void testGetStrokeWidthDefault() {
    MapGeoJsonLayer layer = MOCK_MAP_FACTORIES.createMapGeoJsonLayer();
    assertEquals(1, layer.getStrokeWidth());
  }

  @Test
  public void testSetStrokeWidthPolygonsPolylines() {
    MapGeoJsonLayer layer = MOCK_MAP_FACTORIES.createMapGeoJsonLayer();
    for (int i = 0; i < 2; i++) {
      layer.getElements().add(MOCK_MAP_FACTORIES.createMapPolygon());
      layer.getElements().add(MOCK_MAP_FACTORIES.createMapPolyline());
    }
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(4, elementCollection.getElements().size());
    assertEquals(1, layer.getStrokeWidth());
    layer.setStrokeWidth(3);
    assertEquals(3, layer.getStrokeWidth());
    for (int i = 0; i < 4; i += 2) {
      MapPolygon polygon = (MapPolygon) elementCollection.getElements().get(i);
      assertNotNull(polygon);
      assertEquals(3, polygon.getStrokeWidth());

      MapPolyline polyline = (MapPolyline) elementCollection.getElements().get(i + 1);
      assertNotNull(polyline);
      assertEquals(3, polyline.getStrokeWidth());
    }
  }

  @Test
  public void testGetPolygonsVisibleDefault() {
    MapGeoJsonLayer layer = MOCK_MAP_FACTORIES.createMapGeoJsonLayer();
    assertTrue(layer.getPolygonsVisible());
  }

  @Test
  public void testSetPolygonsInvisible() {
    MapGeoJsonLayer layer = populateNewLayer();
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(6, elementCollection.getElements().size());
    assertTrue(layer.getPolygonsVisible());
    layer.setPolygonsVisible(false);
    assertFalse(layer.getPolygonsVisible());
    for (int i = 0; i < 6; i += 3) {
      MapPolygon polygon = (MapPolygon) elementCollection.getElements().get(i);
      assertNotNull(polygon);
      assertFalse(polygon.isVisible());

      MapPolyline polyline = (MapPolyline) elementCollection.getElements().get(i + 1);
      assertNotNull(polyline);
      assertTrue(polyline.isVisible());

      MapIcon icon = (MapIcon) elementCollection.getElements().get(i + 2);
      assertNotNull(icon);
      assertTrue(icon.isVisible());
    }
  }

  @Test
  public void testGetPolylinesVisibleDefault() {
    MapGeoJsonLayer layer = MOCK_MAP_FACTORIES.createMapGeoJsonLayer();
    assertTrue(layer.getPolylinesVisible());
  }

  @Test
  public void testSetPolylinesInvisible() {
    MapGeoJsonLayer layer = populateNewLayer();
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(6, elementCollection.getElements().size());
    assertTrue(layer.getPolylinesVisible());
    layer.setPolylinesVisible(false);
    assertFalse(layer.getPolylinesVisible());
    for (int i = 0; i < 6; i += 3) {
      MapPolygon polygon = (MapPolygon) elementCollection.getElements().get(i);
      assertNotNull(polygon);
      assertTrue(polygon.isVisible());

      MapPolyline polyline = (MapPolyline) elementCollection.getElements().get(i + 1);
      assertNotNull(polyline);
      assertFalse(polyline.isVisible());

      MapIcon icon = (MapIcon) elementCollection.getElements().get(i + 2);
      assertNotNull(icon);
      assertTrue(icon.isVisible());
    }
  }

  @Test
  public void testGetIconsVisibleDefault() {
    MapGeoJsonLayer layer = MOCK_MAP_FACTORIES.createMapGeoJsonLayer();
    assertTrue(layer.getPolylinesVisible());
  }

  @Test
  public void testSetIconsInvisible() {
    MapGeoJsonLayer layer = populateNewLayer();
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(6, elementCollection.getElements().size());
    assertTrue(layer.getIconsVisible());
    layer.setIconsVisible(false);
    assertFalse(layer.getIconsVisible());
    for (int i = 0; i < 6; i += 3) {
      MapPolygon polygon = (MapPolygon) elementCollection.getElements().get(i);
      assertNotNull(polygon);
      assertTrue(polygon.isVisible());

      MapPolyline polyline = (MapPolyline) elementCollection.getElements().get(i + 1);
      assertNotNull(polyline);
      assertTrue(polyline.isVisible());

      MapIcon icon = (MapIcon) elementCollection.getElements().get(i + 2);
      assertNotNull(icon);
      assertFalse(icon.isVisible());
    }
  }

  @Test
  public void testRemovePolygons() {
    MapGeoJsonLayer layer = populateNewLayer();
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(6, elementCollection.getElements().size());
    List<MapElement> removed = layer.removePolygons();
    assertEquals(2, removed.size());
    for (int i = 0; i < removed.size(); i++) {
      MapElement element = removed.get(i);
      assertNotNull(element);
      assertTrue(element instanceof MapPolygon);
    }
    assertEquals(4, elementCollection.getElements().size());
    for (int i = 0; i < 4; i += 2) {
      MapElement element = elementCollection.getElements().get(i);
      assertNotNull(element);
      assertFalse(element instanceof MapPolygon);
    }
  }

  @Test
  public void testRemovePolylines() {
    MapGeoJsonLayer layer = populateNewLayer();
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(6, elementCollection.getElements().size());
    List<MapElement> removed = layer.removePolylines();
    assertEquals(2, removed.size());
    for (int i = 0; i < removed.size(); i++) {
      MapElement element = removed.get(i);
      assertNotNull(element);
      assertTrue(element instanceof MapPolyline);
    }
    assertEquals(4, elementCollection.getElements().size());
    for (int i = 0; i < 4; i += 2) {
      MapElement element = elementCollection.getElements().get(i);
      assertNotNull(element);
      assertFalse(element instanceof MapPolyline);
    }
  }

  @Test
  public void testRemoveIcons() {
    MapGeoJsonLayer layer = populateNewLayer();
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(6, elementCollection.getElements().size());
    List<MapElement> removed = layer.removeIcons();
    assertEquals(2, removed.size());
    for (int i = 0; i < removed.size(); i++) {
      MapElement element = removed.get(i);
      assertNotNull(element);
      assertTrue(element instanceof MapIcon);
    }
    assertEquals(4, elementCollection.getElements().size());
    for (int i = 0; i < 4; i += 2) {
      MapElement element = elementCollection.getElements().get(i);
      assertNotNull(element);
      assertFalse(element instanceof MapIcon);
    }
  }

  private static MapGeoJsonLayer populateNewLayer() {
    MapGeoJsonLayer layer = MOCK_MAP_FACTORIES.createMapGeoJsonLayer();
    for (int i = 0; i < 2; i++) {
      layer.getElements().add(MOCK_MAP_FACTORIES.createMapPolygon());
      layer.getElements().add(MOCK_MAP_FACTORIES.createMapPolyline());
      layer.getElements().add(MOCK_MAP_FACTORIES.createMapIcon());
    }
    return layer;
  }
}
