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
  public void parseOneRingPolygon_isCorrect() throws GeoJsonParseException, JSONException {
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
    double[][] points = {{30, 10}, {40, 40}, {20, 40}, {10, 20}, {30, 10}};
    int index = 0;
    for (Geoposition position : polygon.getPaths().get(0)) {
      checkPosition(position, points[index]);
      index++;
    }
  }

  @Test
  public void parseMultiRingPolygon_isCorrect() throws GeoJsonParseException, JSONException {
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
    double[][][] points = {
      {{35, 10}, {45, 45}, {15, 40}, {10, 20}, {35, 10}},
      {{20, 30}, {35, 35}, {30, 20}, {20, 30}}
    };
    for (int ring = 0; ring < polygon.getPaths().size(); ring++) {
      Geopath path = polygon.getPaths().get(ring);
      int index = 0;
      for (Geoposition position : path) {
        checkPosition(position, points[ring][index]);
        index++;
      }
    }
  }

  @Test
  public void parsePolyline_isCorrect() throws GeoJsonParseException, JSONException {
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
    double[][] points = {{30, 10, 3}, {10, 30, 9}, {40, 40, 0}};
    int index = 0;
    for (Geoposition position : polyline.getPath()) {
      checkPosition(position, points[index]);
      index++;
    }
  }

  @Test
  public void parsePoint_isCorrect() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n" + "    \"type\": \"Point\", \n" + "    \"coordinates\": [30, 10]\n" + "}";

    MapElementLayer layer = new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    MapIcon icon = (MapIcon) elementCollection.getElements().get(0);
    assertNotNull(icon);
    double[] points = {30, 10, 0};
    checkPosition(icon.getLocation().getPosition(), points);
  }

  @Test
  public void parseMultiPolygon_isCorrect() throws JSONException, GeoJsonParseException {
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
    for (int i = 0; i < elementCollection.getElements().size(); i++) {
      MapPolygon polygon = (MapPolygon) elementCollection.getElements().get(i);
      assertNotNull(polygon);
      assertEquals(1, polygon.getPaths().size());
      double[][] points = {{30, 20}, {45, 40}, {10, 40}, {30, 20}};
      if (i == 1) {
        points = new double[5][2];
        points[0][0] = 15.0;
        points[0][1] = 5.0;

        points[1][0] = 40.0;
        points[1][1] = 10.0;

        points[2][0] = 10.0;
        points[2][1] = 20.0;

        points[3][0] = 5.0;
        points[3][1] = 10.0;

        points[4][0] = 15.0;
        points[4][1] = 5.0;
      }
      int index = 0;
      for (Geoposition position : polygon.getPaths().get(0)) {
        checkPosition(position, points[index]);
        index++;
      }
    }
  }

  @Test
  public void parseMultiPolyline_isCorrect() throws JSONException, GeoJsonParseException {
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
    for (int i = 0; i < elementCollection.getElements().size(); i++) {
      MapPolyline polyline = (MapPolyline) elementCollection.getElements().get(i);
      assertNotNull(polyline);
      assertEquals(3, polyline.getPath().size());
      double[][] points = {{10, 10}, {20, 20}, {10, 40}};

      if (i == 1) {
        points[0][0] = points[0][1] = 40.0;
        points[1][0] = points[1][1] = 30.0;
        points[2][0] = 40.0;
        points[2][1] = 20.0;
      }

      int index = 0;
      for (Geoposition position : polyline.getPath()) {
        checkPosition(position, points[index]);
        index++;
      }
    }
  }

  @Test
  public void parseMultiPoint_isCorrect() throws GeoJsonParseException, JSONException {
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
    double[][] points = {{10, 40}, {40, 30}, {20, 20}, {30, 10}};
    for (int i = 0; i < elementCollection.getElements().size(); i++) {
      MapIcon icon = (MapIcon) elementCollection.getElements().get(i);
      assertNotNull(icon);
      checkPosition(icon.getLocation().getPosition(), points[i]);
    }
  }

  @Test
  public void parseGeometryCollection_isCorrect() throws GeoJsonParseException, JSONException {
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

    double[] points = {40, 10};
    checkPosition(icon.getLocation().getPosition(), points);

    MapPolyline line = (MapPolyline) elementCollection.getElements().get(1);
    assertNotNull(line);
    points[0] = 10;
    points[1] = 20;
    for (Geoposition position : line.getPath()) {
      checkPosition(position, points);
    }
  }

  @Test
  public void parseFeature_isCorrect() throws GeoJsonParseException, JSONException {
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

    double[] points = {102, 0.5};
    checkPosition(icon.getLocation().getPosition(), points);
  }

  @Test
  public void parseFeatureCollection_isCorrect() throws GeoJsonParseException, JSONException {
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
    double[][] points = {{102, 0.5}, {104, 0}};
    for (int i = 0; i < elementCollection.getElements().size(); i++) {
      MapIcon icon = (MapIcon) elementCollection.getElements().get(i);
      assertNotNull(icon);
      checkPosition(icon.getLocation().getPosition(), points[i]);
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void throwExceptionNullGeoJSON_isCorrect() throws GeoJsonParseException {
    GeoJsonParser.parse(null);
  }

  @Test(expected = GeoJsonParseException.class)
  public void throwExceptionNoFeatureGeometry_isCorrect()
      throws JSONException, GeoJsonParseException {
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
  public void throwExceptionBadType_isCorrect() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n" + "    \"type\": \"carrot\", \n" + "    \"coordinates\": [30, 10]\n" + "}";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  @Test(expected = GeoJsonParseException.class)
  public void throwExceptionBadCoordinates_isCorrect() throws GeoJsonParseException, JSONException {
    String geojson = "{\n" + "    \"type\": \"Point\", \n" + "    \"coordinates\": []\n" + "}";
    new GeoJsonParser().internalParse(geojson, MOCK_MAP_FACTORIES);
  }

  private void checkPosition(@NonNull Geoposition position, @NonNull double[] points) {
    assertEquals(points[0], position.getLongitude(), 0);
    assertEquals(points[1], position.getLatitude(), 0);
    if (points.length > 2) {
      assertEquals(points[2], position.getAltitude(), 0);
    } else {
      assertEquals(0.0, position.getAltitude(), 0);
    }
  }
}
