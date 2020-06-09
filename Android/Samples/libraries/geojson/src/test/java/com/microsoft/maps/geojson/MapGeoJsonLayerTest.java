package com.microsoft.maps.geojson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Color;
import com.microsoft.maps.MapPolygon;
import com.microsoft.maps.MapPolyline;
import com.microsoft.maps.MockBingMapsLoader;
import com.microsoft.maps.MockMapElementCollection;
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
    MapPolygon poly = MOCK_MAP_FACTORIES.createMapPolygon();
    layer.getElements().add(poly);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    MapPolygon polygon = (MapPolygon) elementCollection.getElements().get(0);
    assertNotNull(polygon);
  }

  @Test
  public void testAddPolyline() {
    MapGeoJsonLayer layer = MOCK_MAP_FACTORIES.createMapGeoJsonLayer();
    MapPolyline poly = MOCK_MAP_FACTORIES.createMapPolyline();
    layer.getElements().add(poly);
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
    MapPolygon poly = MOCK_MAP_FACTORIES.createMapPolygon();
    layer.getElements().add(poly);
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
    MapPolygon poly = MOCK_MAP_FACTORIES.createMapPolygon();
    layer.getElements().add(poly);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
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
      MapPolygon poly = MOCK_MAP_FACTORIES.createMapPolygon();
      layer.getElements().add(poly);
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
    MapPolygon polygonAdd = MOCK_MAP_FACTORIES.createMapPolygon();
    layer.getElements().add(polygonAdd);
    MapPolyline polylineAdd = MOCK_MAP_FACTORIES.createMapPolyline();
    layer.getElements().add(polylineAdd);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(2, elementCollection.getElements().size());
    layer.setStrokeColor(Color.GREEN);
    assertEquals(Color.GREEN, layer.getStrokeColor());

    MapPolygon polygonReceived = (MapPolygon) elementCollection.getElements().get(0);
    assertNotNull(polygonReceived);
    assertEquals(Color.GREEN, polygonReceived.getStrokeColor());

    MapPolyline polylineReceived = (MapPolyline) elementCollection.getElements().get(1);
    assertNotNull(polylineReceived);
    assertEquals(Color.GREEN, polylineReceived.getStrokeColor());
  }

  @Test
  public void testSetStrokeColorMultiplePolygonsPolylines() {
    MapGeoJsonLayer layer = MOCK_MAP_FACTORIES.createMapGeoJsonLayer();
    for (int i = 0; i < 2; i++) {
      MapPolygon polygonAdd = MOCK_MAP_FACTORIES.createMapPolygon();
      layer.getElements().add(polygonAdd);
      MapPolyline polylineAdd = MOCK_MAP_FACTORIES.createMapPolyline();
      layer.getElements().add(polylineAdd);
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
      MapPolygon polygonAdd = MOCK_MAP_FACTORIES.createMapPolygon();
      layer.getElements().add(polygonAdd);
      MapPolyline polylineAdd = MOCK_MAP_FACTORIES.createMapPolyline();
      layer.getElements().add(polylineAdd);
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
}
