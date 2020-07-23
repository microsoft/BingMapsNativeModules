// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.kml;

import com.microsoft.maps.MockBingMapsLoader;
import com.microsoft.maps.moduletools.MapFactories;
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
  public void testEmptyNameText() throws XmlPullParserException, IOException, KMLParseException {
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
            + "    <name><!--Cool comment--></name>\n"
            + "    <Point>\n"
            + "        <coordinates>\n"
            + "            -107.55,43,0\n"
            + "        </coordinates>\n"
            + "    </Point>\n"
            + "</Placemark>\n"
            + "</kml>";
    new KMLParser(MOCK_MAP_FACTORIES).internalParse(kml);
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
}
