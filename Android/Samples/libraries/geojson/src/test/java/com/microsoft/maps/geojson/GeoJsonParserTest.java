package com.microsoft.maps.geojson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import androidx.annotation.NonNull;
import com.microsoft.maps.Geopath;
import com.microsoft.maps.Geoposition;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapPolygon;
import com.microsoft.maps.MapPolyline;
import com.microsoft.maps.MockBingMapsLoader;
import com.microsoft.maps.MockMapElementCollection;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

/** Unit tests to check the GeoJSONParser class. */
public class GeoJsonParserTest {

  private static final MapFactories MOCK_MAP_FACTORIES = new MockMapFactories();

  @Before
  public void setup() {
    MockBingMapsLoader.mockInitialize();
  }

  @Test
  public void testParseOneRingPolygon() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n"
            + "    \"type\": \"Polygon\", \n"
            + "    \"coordinates\": [\n"
            + "        [[30, 10], [40, 40], [20, 40], [10, 20], [30, 10]]\n"
            + "    ]\n"
            + "}";

    MapElementLayer layer = new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    MapPolygon polygon = (MapPolygon) elementCollection.getElements().get(0);
    assertNotNull(polygon);
    assertEquals(1, polygon.getPaths().size());
    double[][] expectedPoints = {{30, 10}, {40, 40}, {20, 40}, {10, 20}, {30, 10}};
    int index = 0;
    for (Geoposition position : polygon.getPaths().get(0)) {
      checkPosition(expectedPoints[index], position);
      index++;
    }
  }

  @Test
  public void testParseMultiRingPolygon() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n"
            + "    \"type\": \"Polygon\", \n"
            + "    \"coordinates\": [\n"
            + "        [[35, 10], [45, 45], [15, 40], [10, 20], [35, 10]], \n"
            + "        [[20, 30], [35, 35], [30, 20], [20, 30]]\n"
            + "    ]\n"
            + "}";

    MapElementLayer layer = new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    MapPolygon polygon = (MapPolygon) elementCollection.getElements().get(0);
    assertNotNull(polygon);
    assertEquals(2, polygon.getPaths().size());
    double[][][] expectedPoints = {
      {{35, 10}, {45, 45}, {15, 40}, {10, 20}, {35, 10}},
      {{20, 30}, {35, 35}, {30, 20}, {20, 30}}
    };
    for (int ring = 0; ring < polygon.getPaths().size(); ring++) {
      Geopath path = polygon.getPaths().get(ring);
      int index = 0;
      for (Geoposition position : path) {
        checkPosition(expectedPoints[ring][index], position);
        index++;
      }
    }
  }

  @Test
  public void testParsePolyline() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n"
            + "    \"type\": \"LineString\", \n"
            + "    \"coordinates\": [\n"
            + "        [30, 10, 3], [10, 30, 9], [40, 40]\n"
            + "    ]\n"
            + "}";

    MapElementLayer layer = new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    MapPolyline polyline = (MapPolyline) elementCollection.getElements().get(0);
    assertNotNull(polyline);
    assertEquals(3, polyline.getPath().size());
    double[][] expectedPoints = {{30, 10, 3}, {10, 30, 9}, {40, 40, 0}};
    int index = 0;
    for (Geoposition position : polyline.getPath()) {
      checkPosition(expectedPoints[index], position);
      index++;
    }
  }

  @Test
  public void testParsePoint() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n" + "    \"type\": \"Point\", \n" + "    \"coordinates\": [30, 10]\n" + "}";

    MapElementLayer layer = new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    MapIcon icon = (MapIcon) elementCollection.getElements().get(0);
    assertNotNull(icon);
    double[] expectedPoints = {30, 10, 0};
    checkPosition(expectedPoints, icon.getLocation().getPosition());
  }

  @Test
  public void testParseMultiPolygon() throws JSONException, GeoJsonParseException {
    String geojson =
        "{\n"
            + "    \"type\": \"MultiPolygon\", \n"
            + "    \"coordinates\": [\n"
            + "        [\n"
            + "            [[30, 20], [45, 40], [10, 40], [30, 20]]\n"
            + "        ], \n"
            + "        [\n"
            + "            [[15, 5], [40, 10], [10, 20], [5, 10], [15, 5]]\n"
            + "        ]\n"
            + "    ]\n"
            + "}";

    MapElementLayer layer = new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(2, elementCollection.getElements().size());
    double[][] expectedPoints = {
      {30, 20}, {45, 40}, {10, 40}, {30, 20}, {15, 5}, {40, 10}, {10, 20}, {5, 10}, {15, 5}
    };
    int index = 0;
    for (int i = 0; i < elementCollection.getElements().size(); i++) {
      MapPolygon polygon = (MapPolygon) elementCollection.getElements().get(i);
      assertNotNull(polygon);
      assertEquals(1, polygon.getPaths().size());

      for (Geoposition position : polygon.getPaths().get(0)) {
        checkPosition(expectedPoints[index], position);
        index++;
      }
    }
  }

  @Test
  public void testParseMultiPolyline() throws JSONException, GeoJsonParseException {
    String geojson =
        "{\n"
            + "    \"type\": \"MultiLineString\", \n"
            + "    \"coordinates\": [\n"
            + "        [[10, 10], [20, 20], [10, 40]], \n"
            + "        [[40, 40], [30, 30], [40, 20]]\n"
            + "    ]\n"
            + "}";

    MapElementLayer layer = new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(2, elementCollection.getElements().size());
    double[][] expectedPoints = {{10, 10}, {20, 20}, {10, 40}, {40, 40}, {30, 30}, {40, 20}};
    int index = 0;
    for (int i = 0; i < elementCollection.getElements().size(); i++) {
      MapPolyline polyline = (MapPolyline) elementCollection.getElements().get(i);
      assertNotNull(polyline);
      assertEquals(3, polyline.getPath().size());

      for (Geoposition position : polyline.getPath()) {
        checkPosition(expectedPoints[index], position);
        index++;
      }
    }
  }

  @Test
  public void testParseMultiPoint() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n"
            + "    \"type\": \"MultiPoint\", \n"
            + "    \"coordinates\": [\n"
            + "        [10, 40], [40, 30], [20, 20], [30, 10]\n"
            + "    ]\n"
            + "}";

    MapElementLayer layer = new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(4, elementCollection.getElements().size());
    double[][] expectedPoints = {{10, 40}, {40, 30}, {20, 20}, {30, 10}};
    for (int i = 0; i < elementCollection.getElements().size(); i++) {
      MapIcon icon = (MapIcon) elementCollection.getElements().get(i);
      assertNotNull(icon);
      checkPosition(expectedPoints[i], icon.getLocation().getPosition());
    }
  }

  @Test
  public void testParseGeometryCollection() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n"
            + "    \"type\": \"GeometryCollection\",\n"
            + "    \"geometries\": [\n"
            + "        {\n"
            + "            \"type\": \"Point\",\n"
            + "            \"coordinates\": [40, 10]\n"
            + "        },\n"
            + "        {\n"
            + "            \"type\": \"LineString\",\n"
            + "            \"coordinates\": [\n"
            + "                [10, 20]\n"
            + "            ]\n"
            + "        },\n"
            + "    ]\n"
            + "}";

    MapElementLayer layer = new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(2, elementCollection.getElements().size());

    MapIcon icon = (MapIcon) elementCollection.getElements().get(0);
    assertNotNull(icon);

    double[] expectedPoints = {40, 10};
    checkPosition(expectedPoints, icon.getLocation().getPosition());

    MapPolyline line = (MapPolyline) elementCollection.getElements().get(1);
    assertNotNull(line);
    expectedPoints = new double[] {10, 20};
    for (Geoposition position : line.getPath()) {
      checkPosition(expectedPoints, position);
    }
  }

  @Test
  public void testParseFeature() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n"
            + "      \"type\": \"Feature\",\n"
            + "      \"geometry\": {\n"
            + "        \"type\": \"Point\",\n"
            + "        \"coordinates\": [102.0, 0.5]\n"
            + "      },\n"
            + "      \"properties\": {\n"
            + "        \"prop0\": \"value0\"\n"
            + "      }\n"
            + "    }";

    MapElementLayer layer = new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());

    MapIcon icon = (MapIcon) elementCollection.getElements().get(0);
    assertNotNull(icon);

    double[] expectedPoints = {102, 0.5};
    checkPosition(expectedPoints, icon.getLocation().getPosition());
  }

  @Test
  public void testParseFeatureCollection() throws GeoJsonParseException, JSONException {
    String geojson =
        "{  \n"
            + "  \"type\": \"FeatureCollection\",\n"
            + "  \"features\": [\n"
            + "    {\n"
            + "      \"type\": \"Feature\",\n"
            + "      \"geometry\": {\n"
            + "        \"type\": \"Point\",\n"
            + "        \"coordinates\": [102.0, 0.5]\n"
            + "      },\n"
            + "      \"properties\": {\n"
            + "        \"prop0\": \"value0\"\n"
            + "      }\n"
            + "    },\n"
            + "    {\n"
            + "      \"type\": \"Feature\",\n"
            + "      \"geometry\": {\n"
            + "        \"type\": \"Point\",\n"
            + "        \"coordinates\": [104.0, 0.0]\n"
            + "      },\n"
            + "      \"properties\": {\n"
            + "        \"prop0\": \"value0\",\n"
            + "        \"prop1\": 0.0\n"
            + "      }\n"
            + "    }\n"
            + "  ]\n"
            + "}";

    MapElementLayer layer = new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(2, elementCollection.getElements().size());
    double[][] expectedPoints = {{102, 0.5}, {104, 0}};
    for (int i = 0; i < elementCollection.getElements().size(); i++) {
      MapIcon icon = (MapIcon) elementCollection.getElements().get(i);
      assertNotNull(icon);
      checkPosition(expectedPoints[i], icon.getLocation().getPosition());
    }
  }

  @Test
  public void testLongCoordinateArrayLength() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n" + "    \"type\": \"Point\", \n" + "    \"coordinates\": [30, 45, 2, 5]\n" + "}";

    MapElementLayer layer = new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    MapIcon icon = (MapIcon) elementCollection.getElements().get(0);
    assertNotNull(icon);
    double[] expectedPoints = {30, 45, 2};
    checkPosition(expectedPoints, icon.getLocation().getPosition());
  }

  /**
   * Tests the public method to catch null. Note: parse(null) will not call internalParse with null.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testNullGeoJSONThrowsException() throws GeoJsonParseException {
    GeoJsonParser.parse(null);
  }

  @Test(expected = GeoJsonParseException.class)
  public void testNoFeatureGeometryThrowsException() throws JSONException, GeoJsonParseException {
    String geojson =
        "{\n"
            + "      \"type\": \"Feature\",\n"
            + "      \"properties\": {\n"
            + "        \"prop0\": \"value0\"\n"
            + "      }\n"
            + "    }";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = GeoJsonParseException.class)
  public void testInvalidGeometryTypeThrowsException() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n" + "    \"type\": \"carrot\", \n" + "    \"coordinates\": [30, 10]\n" + "}";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = GeoJsonParseException.class)
  public void testEmptyCoordinatesArrayThrowsException()
      throws GeoJsonParseException, JSONException {
    String geojson = "{\n" + "    \"type\": \"Point\", \n" + "    \"coordinates\": []\n" + "}";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = JSONException.class)
  public void testStringCoordinatesThrowsException() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n" + "    \"type\": \"Point\", \n" + "    \"coordinates\": [\"a\", \"b\"]\n" + "}";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = JSONException.class)
  public void testNoCoordinatesThrowsException() throws GeoJsonParseException, JSONException {
    String geojson = "{\n" + "    \"type\": \"Point\" }";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = JSONException.class)
  public void testEmptyStringThrowsException() throws GeoJsonParseException, JSONException {
    String geojson = "";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = JSONException.class)
  public void testMalformedJSONThrowsException() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n" + "    \"type\": {\\ \"Point\", \n" + "    \"coordinates\": [30, 10]\n" + "}";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = JSONException.class)
  public void testEmptyBracketsThrowsException() throws GeoJsonParseException, JSONException {
    String geojson = "{}";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = GeoJsonParseException.class)
  public void testNullTypeThrowsException() throws GeoJsonParseException, JSONException {
    String geojson = "{\n" + "    \"type\": \"null\", \n" + "    \"coordinates\": [30, 10]\n" + "}";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = JSONException.class)
  public void testNullCoordinatesArrayThrowsException()
      throws GeoJsonParseException, JSONException {
    String geojson = "{\n" + "    \"type\": \"Point\", \n" + "    \"coordinates\": null\n" + "}";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = JSONException.class)
  public void testNullLongitudeThrowsException() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n" + "    \"type\": \"Point\", \n" + "    \"coordinates\": [null, 6]\n" + "}";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = JSONException.class)
  public void testNullLatitudeThrowsException() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n" + "    \"type\": \"Point\", \n" + "    \"coordinates\": [6, null]\n" + "}";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = JSONException.class)
  public void testNullAltitudeThrowsException() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n" + "    \"type\": \"Point\", \n" + "    \"coordinates\": [6, 3, null]\n" + "}";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = JSONException.class)
  public void testNaNLongitudeThrowsException() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n" + "    \"type\": \"Point\", \n" + "    \"coordinates\": [NaN, 4, 4]\n" + "}";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = JSONException.class)
  public void testNaNLatitudeThrowsException() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n" + "    \"type\": \"Point\", \n" + "    \"coordinates\": [6, NaN, 4]\n" + "}";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = JSONException.class)
  public void testNaNAltitudeThrowsException() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n" + "    \"type\": \"Point\", \n" + "    \"coordinates\": [6, 3, NaN]\n" + "}";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = JSONException.class)
  public void testBooleanCoordinatesThrowsException() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n" + "    \"type\": \"Point\", \n" + "    \"coordinates\": [true, false]\n" + "}";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = JSONException.class)
  public void testObjectCoordinatesThrowsException() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n" + "    \"type\": \"Point\", \n" + "    \"coordinates\": [{}, {}]\n" + "}";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = JSONException.class)
  public void testArrayCoordinatesThrowsException() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n" + "    \"type\": \"Point\", \n" + "    \"coordinates\": [[], []]\n" + "}";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = GeoJsonParseException.class)
  public void testLongitudeTooLowThrowsException() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n" + "    \"type\": \"Point\", \n" + "    \"coordinates\": [-181, 5]\n" + "}";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = GeoJsonParseException.class)
  public void testLongitudeTooHighThrowsException() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n" + "    \"type\": \"Point\", \n" + "    \"coordinates\": [181, 5]\n" + "}";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = GeoJsonParseException.class)
  public void testLatitudeTooLowThrowsException() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n" + "    \"type\": \"Point\", \n" + "    \"coordinates\": [5, -90.005]\n" + "}";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = GeoJsonParseException.class)
  public void testLatitudeTooHighThrowsException() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n" + "    \"type\": \"Point\", \n" + "    \"coordinates\": [5, 90.005]\n" + "}";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = GeoJsonParseException.class)
  public void testFeatureCollectionTypeNotFeatureThrowsException()
      throws JSONException, GeoJsonParseException {
    String geojson =
        "{\n"
            + "  \"type\": \"FeatureCollection\",\n"
            + "  \"features\": [\n"
            + "    {\n"
            + "      \"type\": \"Point\",\n"
            + "      \"geometry\": {\n"
            + "        \"type\": \"Point\",\n"
            + "        \"coordinates\": [102.0, 0.5]\n"
            + "      },\n"
            + "      \"properties\": {\n"
            + "        \"prop0\": \"value0\"\n"
            + "      }\n"
            + "    }\n"
            + "  ]\n"
            + "}";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = GeoJsonParseException.class)
  public void testGeometryCollectionTypeFeatureThrowsException()
      throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n"
            + "    \"type\": \"GeometryCollection\",\n"
            + "    \"geometries\": [\n"
            + "     {\n"
            + "        \"type\": \"Feature\",\n"
            + "        \"geometry\": {\n"
            + "           \"type\": \"Point\",\n"
            + "           \"coordinates\": [102.0, 0.5]\n"
            + "         },\n"
            + "           \"properties\": {\n"
            + "           \"prop0\": \"value0\"\n"
            + "         }\n"
            + "     }"
            + "    ]\n"
            + "}";

    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  private static void checkPosition(
      @NonNull double[] expectedPoints, @NonNull Geoposition position) {
    assertEquals(expectedPoints[0], position.getLongitude(), 0);
    assertEquals(expectedPoints[1], position.getLatitude(), 0);
    if (expectedPoints.length > 2) {
      assertEquals(expectedPoints[2], position.getAltitude(), 0);
    } else {
      assertEquals(0.0, position.getAltitude(), 0);
    }
  }
}
