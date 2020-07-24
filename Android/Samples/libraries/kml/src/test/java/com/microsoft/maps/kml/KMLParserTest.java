// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.kml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.microsoft.maps.MapElement;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MockBingMapsLoader;
import com.microsoft.maps.MockMapElementCollection;
import com.microsoft.maps.moduletools.MapFactories;
import com.microsoft.maps.moduletoolstest.CheckPosition;
import com.microsoft.maps.moduletoolstest.MockParserMapFactories;
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
            + "            -107.55,43,0\n"
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
    double[] expectedPoints = {-107.55, 43};
    CheckPosition.checkPosition(expectedPoints, icon.getLocation().getPosition());
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
    CheckPosition.checkPosition(expectedPoints, icon.getLocation().getPosition());
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
    for (MapElement element : elementCollection) {
      MapIcon icon = (MapIcon) element;
      CheckPosition.checkPosition(expectedPoints[index], icon.getLocation().getPosition());
      assertEquals(expectedTitles[index], icon.getTitle());
    }
  }

  @Test
  public void testPointSkips() throws XmlPullParserException, IOException, KMLParseException {
    String kml =
        "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n"
            + "<Placemark>\n"
            + "    <Point>\n"
            + "        <TagToSkip>skipping</TagToSkip>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,0\n"
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
    double[] expectedPoints = {-107.55, 43};
    CheckPosition.checkPosition(expectedPoints, icon.getLocation().getPosition());
    assertNull(icon.getTitle());
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
}
