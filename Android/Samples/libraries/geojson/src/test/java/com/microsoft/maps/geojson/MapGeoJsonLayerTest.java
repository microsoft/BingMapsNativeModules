package com.microsoft.maps.geojson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.graphics.Color;
import com.microsoft.maps.MapPolygon;
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
}
