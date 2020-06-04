package com.microsoft.maps;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/** Unit tests to check the parser. */
public class GeoJsonParserTest {

  private static MapElementLayer mLayer;
  private static MapIcon mIcon;
  private static MapPolyline mPolyline;
  private static MapPolygon mPolygon;
  private static ArrayList<Geopoint> mPoints = new ArrayList<>();
  private static ArrayList<Geopath> mPaths = new ArrayList<>();
  @Mock private MapElementCollection mCollection;

  private static final MapFactories DEFAULT_MAP_FACTORIES =
      new MapFactories() {
        @Override
        public MapElementLayer createMapElementLayer() {
          return mLayer;
        }

        @Override
        public MapIcon createMapIcon() {
          mIcon = Mockito.mock(MapIcon.class);

          Mockito.doAnswer(
                  new Answer<Void>() {
                    public Void answer(InvocationOnMock invocation) throws Throwable {
                      assertTrue(invocation.getArgument(0) instanceof Geopoint);
                      Geopoint arg = invocation.getArgument(0);
                      mPoints.add(arg);
                      return null;
                    }
                  })
              .when(mIcon)
              .setLocation(Mockito.any(Geopoint.class));

          return mIcon;
        }

        @Override
        public MapPolyline createMapPolyline() {
          mPolyline = Mockito.mock(MapPolyline.class);
          return mPolyline;
        }

        @Override
        public MapPolygon createMapPolygon() {
          mPolygon = Mockito.mock(MapPolygon.class);

          Mockito.doAnswer(
                  new Answer<Boolean>() {
                    @NonNull
                    public Boolean answer(InvocationOnMock invocation) throws Throwable {
                      ArrayList<Geopath> arg = invocation.getArgument(0);
                      mPaths.addAll(arg);
                      return true;
                    }
                  })
              .when(mPolygon)
              .setPaths(Mockito.<Geopath>anyList());
          return mPolygon;
        }
      };

  @Before
  public void setup() {
    BingMapsLoader.mockInitialize();
    MockitoAnnotations.initMocks(this);
    mLayer = Mockito.mock(MapElementLayer.class);
    mIcon = DEFAULT_MAP_FACTORIES.createMapIcon();
    mPolygon = DEFAULT_MAP_FACTORIES.createMapPolygon();
    mPolyline = DEFAULT_MAP_FACTORIES.createMapPolyline();
    when(mLayer.getElements()).thenReturn(mCollection);
  }

  /**
   * Tests that parser creates a new Polygon (only one Geopath) with the correct coordinates and
   * adds it to the map.
   */
  @Test
  public void parseSimplePolygon_isCorrect() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n"
            + "    \"type\": \"Polygon\", \n"
            + "    \"coordinates\": [\n"
            + "        [[30, 10], [40, 40], [20, 40], [10, 20], [30, 10]]\n"
            + "    ]\n"
            + "}";

    Mockito.doAnswer(
            new Answer<Boolean>() {
              @NonNull
              public Boolean answer(InvocationOnMock invocation) throws Throwable {
                assertTrue(invocation.getArgument(0) instanceof MapPolygon);
                return true;
              }
            })
        .when(mCollection)
        .add(Mockito.any(MapElement.class));

    mPaths = new ArrayList<>();
    mLayer = new GeoJsonParser().internalParse(geojson, DEFAULT_MAP_FACTORIES);
    verify(mCollection, times(1)).add(Mockito.any(MapElement.class));
    assertEquals(1, mPaths.size());
    double[][] points = {{30, 10}, {40, 40}, {20, 40}, {10, 20}, {30, 10}};
    int index = 0;
    for (Geoposition position : mPaths.get(0)) {
      assertEquals(points[index][0], position.getLongitude());
      assertEquals(points[index][1], position.getLatitude());
      assertEquals(0.0, position.getAltitude());
      index++;
    }
  }

  /**
   * Tests that parser creates a new Polygon (with more than one Geopath) with the correct
   * coordinates and adds it to the map.
   */
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

    Mockito.doAnswer(
            new Answer<Boolean>() {
              @NonNull
              public Boolean answer(InvocationOnMock invocation) throws Throwable {
                assertTrue(invocation.getArgument(0) instanceof MapPolygon);
                return true;
              }
            })
        .when(mCollection)
        .add(Mockito.any(MapElement.class));

    mPaths = new ArrayList<>();
    mLayer = new GeoJsonParser().internalParse(geojson, DEFAULT_MAP_FACTORIES);
    verify(mCollection, times(1)).add(Mockito.any(MapElement.class));
    verifyNoMoreInteractions(mCollection);
    verify(mPolygon, times(1)).setPaths(Mockito.<Geopath>anyList());
    verifyNoMoreInteractions(mPolygon);

    assertEquals(2, mPaths.size());
    double[][][] points = {
      {{35, 10}, {45, 45}, {15, 40}, {10, 20}, {35, 10}},
      {{20, 30}, {35, 35}, {30, 20}, {20, 30}}
    };
    for (int ring = 0; ring < mPaths.size(); ring++) {
      Geopath path = mPaths.get(ring);
      int pair = 0;
      for (Geoposition position : path) {
        assertEquals(points[ring][pair][0], position.getLongitude());
        assertEquals(points[ring][pair][1], position.getLatitude());
        assertEquals(0.0, position.getAltitude());
        pair++;
      }
    }
  }

  /**
   * Tests that parser creates a new Polyline with the correct coordinates and adds it to the
   * Maplayer.
   */
  @Test
  public void parsePolyline_isCorrect() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n"
            + "    \"type\": \"LineString\", \n"
            + "    \"coordinates\": [\n"
            + "        [30, 10], [10, 30], [40, 40]\n"
            + "    ]\n"
            + "}";

    final int[][] points = {{30, 10}, {10, 30}, {10, 30}};

    Mockito.doAnswer(
            new Answer<Boolean>() {
              @NonNull
              public Boolean answer(InvocationOnMock invocation) throws Throwable {
                assertTrue(invocation.getArgument(0) instanceof MapPolyline);
                return true;
              }
            })
        .when(mCollection)
        .add(Mockito.any(MapElement.class));

    // CHECK POLYLINE CORRECT

    mLayer = new GeoJsonParser().internalParse(geojson, DEFAULT_MAP_FACTORIES);
    verify(mCollection, times(1)).add(Mockito.any(MapElement.class));
  }

  /** Tests that parser creates a new Point with correct coordinates. */
  @Test
  public void parsePoint_isCorrect() throws GeoJsonParseException, JSONException {
    String geojson = "{ \"type\": \"Point\", \"coordinates\": [30, 10, 5] }";

    ArgumentCaptor<Geopoint> valueCapture = ArgumentCaptor.forClass(Geopoint.class);
    doNothing().when(mIcon).setLocation(valueCapture.capture());

    Mockito.doAnswer(
            new Answer<Boolean>() {
              @NonNull
              public Boolean answer(InvocationOnMock invocation) throws Throwable {
                assertTrue(invocation.getArgument(0) instanceof MapIcon);
                return true;
              }
            })
        .when(mCollection)
        .add(Mockito.any(MapElement.class));

    mLayer = new GeoJsonParser().internalParse(geojson, DEFAULT_MAP_FACTORIES);
    verify(mIcon, times(1)).setLocation(valueCapture.capture());
    Geoposition position = valueCapture.getValue().getPosition();
    assertEquals(30.0, position.getLongitude());
    assertEquals(10.0, position.getLatitude());
    assertEquals(5.0, position.getAltitude());
    verify(mCollection, times(1)).add(Mockito.any(MapElement.class));
  }

  /** Tests that parser creates a new MultiPolygon. */
  @Test
  public void parseMultiPolygon_isCorrect() {}

  /** Tests that parser creates a new MultiPolyline. */
  @Test
  public void parseMultiPolyline_isCorrect() {}

  /** Tests that parser correctly creates multiple MapIcons with correct coordinates. */
  @Test
  public void parseMultiPoint_isCorrect() throws GeoJsonParseException, JSONException {
    String geojson =
        "{\n"
            + "    \"type\": \"MultiPoint\", \n"
            + "    \"coordinates\": [\n"
            + "        [10, 40], [40, 30], [20, 20], [30, 10]\n"
            + "    ]\n"
            + "}";

    Mockito.doAnswer(
            new Answer<Boolean>() {
              @NonNull
              public Boolean answer(InvocationOnMock invocation) throws Throwable {
                assertTrue(invocation.getArgument(0) instanceof MapIcon);
                return true;
              }
            })
        .when(mCollection)
        .add(Mockito.any(MapElement.class));

    mPoints = new ArrayList<>();
    mLayer = new GeoJsonParser().internalParse(geojson, DEFAULT_MAP_FACTORIES);
    verify(mCollection, times(4)).add(Mockito.any(MapElement.class));
    assertEquals(4, mPoints.size());

    double[][] points = {{10, 40}, {40, 30}, {20, 20}, {30, 10}};
    for (int i = 0; i < mPoints.size(); i++) {
      Geoposition position = mPoints.get(i).getPosition();
      assertEquals(points[i][0], position.getLongitude());
      assertEquals(points[i][1], position.getLatitude());
      assertEquals(0.0, position.getAltitude());
    }
  }

  /**
   * Tests that the parser creates the correct number of polygons for the countries.geojson file.
   */
  @Test
  public void countriesParser_isCorrect() {
    final ArrayList<MapElement> list = new ArrayList<>();

    Mockito.doAnswer(
            new Answer<Boolean>() {
              @NonNull
              public Boolean answer(InvocationOnMock invocation) throws Throwable {
                list.add((MapElement) (invocation.getArgument(0)));
                return true;
              }
            })
        .when(mCollection)
        .add(Mockito.any(MapElement.class));

    InputStream is = this.getClass().getResourceAsStream("countries.geojson");
    String geojson = new Scanner(is).useDelimiter("\\A").next();
    try {
      mLayer = new GeoJsonParser().internalParse(geojson, DEFAULT_MAP_FACTORIES);
      assertEquals(4252, list.size());
    } catch (GeoJsonParseException | JSONException e) {
      e.printStackTrace();
    }
  }
}
