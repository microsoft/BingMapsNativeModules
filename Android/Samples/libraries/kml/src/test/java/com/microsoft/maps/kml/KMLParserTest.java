// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.kml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.microsoft.maps.AltitudeReferenceSystem;
import com.microsoft.maps.Geoposition;
import com.microsoft.maps.MapElement;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapPolyline;
import com.microsoft.maps.MockBingMapsLoader;
import com.microsoft.maps.MockMapElementCollection;
import com.microsoft.maps.moduletools.MapFactories;
import com.microsoft.maps.moduletoolstest.MockParserMapFactories;
import com.microsoft.maps.moduletoolstest.TestHelpers;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.xmlpull.v1.XmlPullParserException;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class KMLParserTest {

  private static final MapFactories MOCK_MAP_FACTORIES = new MockParserMapFactories();

  @Before
  public void setup() {
    MockBingMapsLoader.mockInitialize();
  }

  @Test
  public void testSkip() throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <TagToSkip></TagToSkip>"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test
  public void testNoNameText() throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test
  public void testCommentFirstLine() throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<!--Cool comment-->"
            + "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test
  public void testCommentLastLine() throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>\n"
            + "<!--Cool comment-->";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test
  public void testCommentAfterXmlDeclaration()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<!--Cool comment-->"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test
  public void testCommentAfterPlacemark()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "<!--Cool comment-->"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test
  public void testCommentNameText() throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name><!--Cool comment-->city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,9\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    MapIcon icon = (MapIcon) elementCollection.getElements().get(0);
    assertNotNull(icon);
    double[] expectedPoints = {-107.55, 43, 9};
    TestHelpers.assertPositionEquals(expectedPoints, icon.getLocation().getPosition());
    assertEquals(AltitudeReferenceSystem.GEOID, icon.getLocation().getAltitudeReferenceSystem());
    assertEquals("city", icon.getTitle());
  }

  @Test
  public void testParsePoint() throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    MapIcon icon = (MapIcon) elementCollection.getElements().get(0);
    assertNotNull(icon);
    double[] expectedPoints = {-107.55, 43};
    TestHelpers.assertPositionEquals(expectedPoints, icon.getLocation().getPosition());
    assertEquals(AltitudeReferenceSystem.SURFACE, icon.getLocation().getAltitudeReferenceSystem());
    assertEquals("city", icon.getTitle());
  }

  @Test
  public void testExtraCoordinates() throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,7,98,6\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    MapIcon icon = (MapIcon) elementCollection.getElements().get(0);
    assertNotNull(icon);
    double[] expectedPoints = {-107.55, 43, 7};
    TestHelpers.assertPositionEquals(expectedPoints, icon.getLocation().getPosition());
    assertEquals(AltitudeReferenceSystem.GEOID, icon.getLocation().getAltitudeReferenceSystem());
    assertEquals("city", icon.getTitle());
  }

  @Test
  public void testParseMultiplePlacemarksPoint()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <TagToSkip></TagToSkip>"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,45\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "<Placemark>\n"
            + "    <name>city2</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -109.55,43\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(2, elementCollection.getElements().size());
    double[][] expectedPoints = {{-107.55, 45}, {-109.55, 43}};
    String[] expectedTitles = {"city", "city2"};
    int index = 0;
    for (MapElement element : elementCollection.getElements()) {
      MapIcon icon = (MapIcon) element;
      TestHelpers.assertPositionEquals(expectedPoints[index], icon.getLocation().getPosition());
      assertEquals(
          AltitudeReferenceSystem.SURFACE, icon.getLocation().getAltitudeReferenceSystem());
      assertEquals(expectedTitles[index], icon.getTitle());
      index++;
    }
  }

  @Test
  public void testCoordinatesNotLastTag()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <LineString>\n"
            + "        <coordinates>\n"
            + "            -107.55,45,98 67,78\n"
            + "        </coordinates>\n"
            + "        <extrude>1</extrude>\n"
            + "    </LineString>\n"
            + "</Placemark>\n"
            + "<Placemark>\n"
            + "    <name>city2</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -67,-78\n"
            + "        </coordinates>\n"
            + "        <extrude>1</extrude>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(2, elementCollection.getElements().size());
    double[][] expectedPoints = {{-107.55, 45}, {67, 78}, {-67, -78}};
    int index = 0;
    MapPolyline line = (MapPolyline) elementCollection.getElements().get(0);
    assertEquals(AltitudeReferenceSystem.SURFACE, line.getPath().getAltitudeReferenceSystem());
    for (Geoposition position : line.getPath()) {
      TestHelpers.assertPositionEquals(expectedPoints[index], position);
      index++;
    }
    MapIcon icon = (MapIcon) elementCollection.getElements().get(1);
    assertEquals(AltitudeReferenceSystem.SURFACE, icon.getLocation().getAltitudeReferenceSystem());
    TestHelpers.assertPositionEquals(expectedPoints[index], icon.getLocation().getPosition());
  }

  @Test
  public void testParseLineStringWithAltitudes()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <LineString>\n"
            + "        <coordinates>\n"
            + "            -107.55,45,98 67,78,89\n"
            + "        </coordinates>\n"
            + "    </LineString>\n"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    double[][] expectedPoints = {{-107.55, 45, 98}, {67, 78, 89}};
    int index = 0;
    for (MapElement element : elementCollection.getElements()) {
      MapPolyline line = (MapPolyline) element;
      assertEquals(AltitudeReferenceSystem.GEOID, line.getPath().getAltitudeReferenceSystem());
      for (Geoposition position : line.getPath()) {
        TestHelpers.assertPositionEquals(expectedPoints[index], position);
        index++;
      }
    }
  }

  @Test
  public void testParseLineStringNotAllAltitudes()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <LineString>\n"
            + "        <coordinates>\n"
            + "            -107.55,45,98 67,78\n"
            + "        </coordinates>\n"
            + "    </LineString>\n"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    MapElementLayer layer = new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
    MockMapElementCollection elementCollection = (MockMapElementCollection) layer.getElements();
    assertNotNull(elementCollection);
    assertEquals(1, elementCollection.getElements().size());
    double[][] expectedPoints = {{-107.55, 45}, {67, 78}};
    int index = 0;
    for (MapElement element : elementCollection.getElements()) {
      MapPolyline line = (MapPolyline) element;
      assertEquals(AltitudeReferenceSystem.SURFACE, line.getPath().getAltitudeReferenceSystem());
      for (Geoposition position : line.getPath()) {
        TestHelpers.assertPositionEquals(expectedPoints[index], position);
        index++;
      }
    }
  }

  /**
   * Tests the public method to catch null. Note: parse(null) will not call internalParse with null.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testNullKMLThrowsException() throws KMLParseException {
    KMLParser.parse(null);
  }

  @Test(expected = KMLParseException.class)
  public void testEmptyStringThrowsException() throws KMLParseException {
    String kml = "";
    KMLParser.parse(kml);
  }

  @Test(expected = XmlPullParserException.class)
  public void testNotXMLThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml = "foo";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = XmlPullParserException.class)
  public void testUnexpectedEndTagThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml = "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n" + "</BadEnding>\n" + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testNoCoordinatesTextThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates></coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testNoCoordinatesElementThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testLongitudeTooHighThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -189,43,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testLongitudeTooLowThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            189,43,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testLatitudeTooHighThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            78,98,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testLatitudeTooLowThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            78,-98,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = NumberFormatException.class)
  public void testCoordinatesNotDoublesThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            foo,bar\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testNotEnoughCoordinatesThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            5\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testCoordinatesEmptyWhitespaceThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            \n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testEmptyNameTextThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <name></name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = XmlPullParserException.class)
  public void testMalformedEndTagThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <MalformedEnd>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,0\n"
            + "        </coordinates>\n"
            + "    /MalformedEnd>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testPointHasMultipleCoordinatesThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,0 98,7,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testLongitudeNaNThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            NaN,43,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testLatitudeNaNThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            78,NaN,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testAltitudeNaNThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            98,43,NaN\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testEmptyStringCoordinatesThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            \n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testParseLineStringOnePositionThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <LineString>\n"
            + "        <coordinates>\n"
            + "            -107.55,45,98\n"
            + "        </coordinates>\n"
            + "    </LineString>\n"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }

  @Test(expected = KMLParseException.class)
  public void testMultipleCoordinatesTagThrowsException()
      throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Document>"
            + "<Placemark>\n"
            + "    <name>city</name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,7,98,6\n"
            + "        </coordinates>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,7,98,6\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</Document>"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
  }
}
