// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.kml;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.util.Xml;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.microsoft.maps.AltitudeReferenceSystem;
import com.microsoft.maps.Geopath;
import com.microsoft.maps.Geopoint;
import com.microsoft.maps.Geoposition;
import com.microsoft.maps.MapElement;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapPolygon;
import com.microsoft.maps.MapPolyline;
import com.microsoft.maps.kml.styles.IconStyle;
import com.microsoft.maps.kml.styles.LineStyle;
import com.microsoft.maps.kml.styles.PolyStyle;
import com.microsoft.maps.kml.styles.StylesHolder;
import com.microsoft.maps.moduletools.AltitudeReferenceSystemWrapper;
import com.microsoft.maps.moduletools.DefaultMapFactories;
import com.microsoft.maps.moduletools.MapFactories;
import com.microsoft.maps.moduletools.ParsingHelpers;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Class that parses KML and returns a new MapElementLayer containing all the shapes outlined in the
 * KML.
 *
 * <p>Created by Elizabeth Bartusiak (t-elbart) on 07/21/2020
 */
public class KMLParser {

  private final MapElementLayer mLayer;
  private final MapFactories mFactory;
  private boolean mDidWarn;
  private String mNameSpace;
  private final XmlPullParser mParser = Xml.newPullParser();
  private final Map<String, StylesHolder> mMapStylesHolders = new HashMap<>();
  private final Map<String, ArrayList<MapElement>> mMapElementStyles = new HashMap<>();

  private static final MapFactories DEFAULT_MAP_FACTORIES = new DefaultMapFactories();

  @VisibleForTesting
  KMLParser(@NonNull MapFactories factory) {
    mFactory = factory;
    mLayer = mFactory.createMapElementLayer();
  }

  /**
   * Method to parse given kml and return MapElementLayer containing the shapes outlined in the kml.
   * Note: If the KML may contain references to external resources, parse should not be called on
   * the UI thread. The external resources will be downloaded synchronously.
   *
   * @param kml input String
   * @return MapElementLayer
   * @throws KMLParseException
   */
  @NonNull
  public static MapElementLayer parse(@NonNull String kml) throws KMLParseException {
    if (kml == null) {
      throw new IllegalArgumentException("Input String cannot be null.");
    }
    if (kml.equals("")) {
      throw new KMLParseException("Input String cannot be empty.");
    }
    KMLParser instance = new KMLParser(DEFAULT_MAP_FACTORIES);
    try {
      return instance.internalParse(kml);
    } catch (Exception e) {
      throw new KMLParseException(e.getMessage());
    }
  }

  @VisibleForTesting
  @NonNull
  MapElementLayer internalParse(@NonNull String kml)
      throws XmlPullParserException, IOException, KMLParseException {
    try (InputStream stream = new ByteArrayInputStream(kml.getBytes(UTF_8))) {
      mParser.setInput(stream, null);
      mParser.nextTag();
      mNameSpace = mParser.getNamespace();
      parseOuterLayer();
      applyStyles();
    }
    return mLayer;
  }

  private void parseOuterLayer() throws IOException, XmlPullParserException, KMLParseException {
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String type = mParser.getName();
      switch (type) {
        case "Placemark":
          parseNameAndShape();
          break;
        case "Style":
          parseStyleAddToMapStylesHolders();
          break;
        case "Document":
        case "Folder":
          parseOuterLayer();
          break;
        default:
          skipToEndOfTag();
          break;
      }
    }
  }

  private void parseStyleAddToMapStylesHolders()
      throws XmlPullParserException, IOException, KMLParseException {
    String id = mParser.getAttributeValue(null, "id");
    if (mMapStylesHolders.containsKey(id)) {
      throw new KMLParseException(
          "Error at: " + mParser.getPositionDescription() + " ID " + id + " already seen.");
    }
    StylesHolder stylesHolder = new StylesHolder();
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String type = mParser.getName();
      switch (type) {
        case "IconStyle":
          parseIconStyle(stylesHolder.getIconStyle());
          break;
        case "LineStyle":
          parseLineStyle(stylesHolder.getLineStyle());
          break;
        case "PolyStyle":
          parsePolyStyle(stylesHolder.getPolyStyle());
          break;
        default:
          skipToEndOfTag();
          break;
      }
    }
    mMapStylesHolders.put(id, stylesHolder);
  }

  private void parseIconStyle(@NonNull IconStyle iconStyle)
      throws XmlPullParserException, IOException, KMLParseException {
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      if (mParser.getName().equals("Icon")) {
        parseIcon(iconStyle);
      } else {
        skipToEndOfTag();
      }
    }
  }

  private void parseIcon(@NonNull IconStyle iconStyle)
      throws XmlPullParserException, IOException, KMLParseException {
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      if (mParser.getName().equals("href")) {
        String url = parseText();
        InputStream inputStream = new URL(url).openConnection().getInputStream();
        iconStyle.setImage(mFactory.createMapImage(inputStream));
      }
    }
  }

  private void parseLineStyle(@NonNull LineStyle lineStyle)
      throws XmlPullParserException, IOException, KMLParseException {
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String type = mParser.getName();
      if (type.equals("width")) {
        double parsedWidth = Double.parseDouble(parseText());
        if (Double.isNaN(parsedWidth)) {
          throw new KMLParseException(
              "Error at: " + mParser.getPositionDescription() + " width cannot be NaN.");
        }
        int width = (int) parsedWidth;
        if (width <= 0) {
          throw new KMLParseException(
              "Error at: "
                  + mParser.getPositionDescription()
                  + " width must be greater than 0."
                  + "Instead saw int value: "
                  + width);
        }
        lineStyle.setWidth(width);
      } else if (type.equals("color")) {
        lineStyle.setStrokeColor(parseColor());
      } else {
        skipToEndOfTag();
      }
    }
  }

  private void parsePolyStyle(@NonNull PolyStyle polyStyle)
      throws XmlPullParserException, IOException, KMLParseException {
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      switch (mParser.getName()) {
        case "fill":
          String shouldFill = parseText();
          if (shouldFill.equals("1") || shouldFill.equalsIgnoreCase("true")) {
            polyStyle.setShouldFill(true);
          } else if (shouldFill.equals("0") || shouldFill.equalsIgnoreCase("false")) {
            polyStyle.setShouldFill(false);
          } else {
            throw new KMLParseException(
                "Invalid value ("
                    + shouldFill
                    + ") for <fill> at position "
                    + mParser.getPositionDescription());
          }
          break;
        case "outline":
          String shouldOutline = parseText();
          if (shouldOutline.equals("1") || shouldOutline.equalsIgnoreCase("true")) {
            polyStyle.setShouldOutline(true);
          } else if (shouldOutline.equals("0") || shouldOutline.equalsIgnoreCase("false")) {
            polyStyle.setShouldOutline(false);
          } else {
            throw new KMLParseException(
                "Invalid value ("
                    + shouldOutline
                    + ") for <outline> at position "
                    + mParser.getPositionDescription());
          }
          break;
        case "color":
          if (polyStyle.shouldFill()) {
            polyStyle.setFillColor(parseColor());
          }
          break;
        default:
          skipToEndOfTag();
          break;
      }
    }
  }

  private void parseNameAndShape() throws IOException, XmlPullParserException, KMLParseException {
    String title = null;
    MapElement element = null;
    String styleId = null;
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      switch (mParser.getName()) {
        case "name":
          mParser.require(XmlPullParser.START_TAG, mNameSpace, "name");
          title = parseText();
          mParser.require(XmlPullParser.END_TAG, mNameSpace, "name");
          break;
        case "styleUrl":
          String url = parseText();
          if (url.indexOf('#') == 0) {
            styleId = url.substring(1);
          }
          break;
        case "MultiGeometry":
          parseMultiGeometry();
          break;
        default:
          element = parseGeometryIfApplicable();
          break;
      }
    }
    if (element != null) {
      if (title != null && element instanceof MapIcon) {
        ((MapIcon) element).setTitle(title);
      }
      if (styleId != null) {
        if (!mMapElementStyles.containsKey(styleId)) {
          mMapElementStyles.put(styleId, new ArrayList<MapElement>());
        }
        mMapElementStyles.get(styleId).add(element);
      }
      mLayer.getElements().add(element);
    }
  }

  private void parseMultiGeometry() throws XmlPullParserException, IOException, KMLParseException {
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      MapElement element = parseGeometryIfApplicable();
      if (element != null) {
        mLayer.getElements().add(element);
      }
    }
  }

  @Nullable
  private MapElement parseGeometryIfApplicable()
      throws XmlPullParserException, IOException, KMLParseException {
    switch (mParser.getName()) {
      case "Point":
        return parsePoint();
      case "LineString":
        return parseLineString();
      case "Polygon":
        return parsePolygon();
      default:
        skipToEndOfTag();
        return null;
    }
  }

  @NonNull
  private MapIcon parsePoint() throws IOException, XmlPullParserException, KMLParseException {
    mParser.require(XmlPullParser.START_TAG, mNameSpace, "Point");
    MapIcon icon = mFactory.createMapIcon();
    boolean hasParsedCoordinates = false;
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String type = mParser.getName();
      if (type.equals("coordinates")) {
        verifyElementNotSeen("coordinates", hasParsedCoordinates);
        AltitudeReferenceSystemWrapper altitudeReferenceSystemWrapper =
            new AltitudeReferenceSystemWrapper(AltitudeReferenceSystem.GEOID);
        ArrayList<Geoposition> coordinates = parseCoordinates(altitudeReferenceSystemWrapper);
        if (coordinates.size() > 1) {
          throw new KMLParseException(
              "coordinates for a Point can only contain one position. Instead saw: "
                  + coordinates.size()
                  + " at position: "
                  + mParser.getPositionDescription());
        }
        icon.setLocation(
            new Geopoint(
                coordinates.get(0), altitudeReferenceSystemWrapper.getAltitudeReferenceSystem()));
        hasParsedCoordinates = true;
      } else {
        skipToEndOfTag();
      }
    }
    verifyElementSeen("coordinates", hasParsedCoordinates);
    return icon;
  }

  @NonNull
  private MapPolyline parseLineString()
      throws IOException, XmlPullParserException, KMLParseException {
    mParser.require(XmlPullParser.START_TAG, mNameSpace, "LineString");
    MapPolyline line = mFactory.createMapPolyline();
    boolean hasParsedCoordinates = false;
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String type = mParser.getName();
      if (type.equals("coordinates")) {
        verifyElementNotSeen("coordinates", hasParsedCoordinates);
        AltitudeReferenceSystemWrapper altitudeReferenceSystemWrapper =
            new AltitudeReferenceSystemWrapper(AltitudeReferenceSystem.GEOID);
        ArrayList<Geoposition> positions = parseCoordinates(altitudeReferenceSystemWrapper);
        if (positions.size() < 2) {
          throw new KMLParseException(
              "coordinates for a LineString must contain at least two positions. Instead saw: "
                  + positions.size()
                  + " at position: "
                  + mParser.getPositionDescription());
        }
        ParsingHelpers.setAltitudesToZeroIfAtSurface(
            positions, altitudeReferenceSystemWrapper.getAltitudeReferenceSystem());
        line.setPath(
            new Geopath(positions, altitudeReferenceSystemWrapper.getAltitudeReferenceSystem()));
        hasParsedCoordinates = true;
      } else {
        skipToEndOfTag();
      }
    }
    verifyElementSeen("coordinates", hasParsedCoordinates);
    return line;
  }

  /* A Polygon MUST have only one outer boundary, and a Polygon may have 0 or more
   * inner boundaries.
   */
  @NonNull
  private MapPolygon parsePolygon() throws IOException, XmlPullParserException, KMLParseException {
    mParser.require(XmlPullParser.START_TAG, mNameSpace, "Polygon");
    MapPolygon polygon = mFactory.createMapPolygon();
    ArrayList<ArrayList<Geoposition>> rings = new ArrayList<>();
    boolean hasOuterBoundary = false;
    AltitudeReferenceSystemWrapper altitudeReferenceSystemWrapper =
        new AltitudeReferenceSystemWrapper(AltitudeReferenceSystem.GEOID);
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String type = mParser.getName();
      if (type.equals("outerBoundaryIs") || type.equals("innerBoundaryIs")) {
        if (type.equals("outerBoundaryIs")) {
          verifyElementNotSeen("outerBoundaryIs", hasOuterBoundary);
          hasOuterBoundary = true;
        }
        rings.add(parsePolygonRing(type, altitudeReferenceSystemWrapper));
      } else {
        skipToEndOfTag();
      }
    }
    verifyElementSeen("outerBoundaryIs", hasOuterBoundary);
    ArrayList<Geopath> paths = new ArrayList<>(rings.size());
    for (ArrayList<Geoposition> ring : rings) {
      ParsingHelpers.setAltitudesToZeroIfAtSurface(
          ring, altitudeReferenceSystemWrapper.getAltitudeReferenceSystem());
      paths.add(new Geopath(ring, altitudeReferenceSystemWrapper.getAltitudeReferenceSystem()));
    }
    polygon.setPaths(paths);
    return polygon;
  }

  /* ArrayList positions is initialized by parseCoordinates, or an error is thrown if no
   * <coordinates> tag is present. */
  @NonNull
  private ArrayList<Geoposition> parsePolygonRing(
      @NonNull String tag, @NonNull AltitudeReferenceSystemWrapper altitudeReferenceSystemWrapper)
      throws IOException, XmlPullParserException, KMLParseException {
    mParser.require(XmlPullParser.START_TAG, mNameSpace, tag);
    mParser.nextTag();
    mParser.require(XmlPullParser.START_TAG, mNameSpace, "LinearRing");
    ArrayList<Geoposition> positions = null;
    boolean hasParsedCoordinates = false;
    while (moveToNext() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String type = mParser.getName();
      if (type.equals("coordinates")) {
        verifyElementNotSeen(type, hasParsedCoordinates);
        positions = parseCoordinates(altitudeReferenceSystemWrapper);
        String exceptionMessage = ParsingHelpers.getErrorMessageForPolygonRing(positions);
        if (exceptionMessage != null) {
          throw new KMLParseException(
              "Error at: " + mParser.getPositionDescription() + " " + exceptionMessage);
        }
        hasParsedCoordinates = true;
      }
    }
    verifyElementSeen("coordinates", hasParsedCoordinates);
    mParser.require(XmlPullParser.END_TAG, mNameSpace, "LinearRing");
    mParser.nextTag();
    mParser.require(XmlPullParser.END_TAG, mNameSpace, tag);
    return positions;
  }

  /* parseCoordinates throws an error if coordinates given are not valid.
   * Otherwise, the returned ArrayList will contain at least one Geoposition. */
  @NonNull
  private ArrayList<Geoposition> parseCoordinates(
      @NonNull AltitudeReferenceSystemWrapper altitudeReferenceSystemWrapper)
      throws IOException, XmlPullParserException, KMLParseException {
    mParser.require(XmlPullParser.START_TAG, mNameSpace, "coordinates");
    String coordinates = parseText();
    String[] allCoordinates = coordinates.split("\\s+");
    ArrayList<Geoposition> positions = new ArrayList<>(allCoordinates.length);
    for (String str : allCoordinates) {
      String[] latLongAlt = str.split(",");
      if (latLongAlt.length < 2) {
        throw new KMLParseException(
            "Error at: "
                + mParser.getPositionDescription()
                + " coordinates must contain at least latitude and longitude, separated by only a comma.");
      }
      double longitude = Double.parseDouble(latLongAlt[0]);
      if (Double.isNaN(longitude)) {
        throw new KMLParseException(
            "Error at: " + mParser.getPositionDescription() + " longitude cannot be NaN.");
      }
      if (longitude < -180 || longitude > 180) {
        throw new KMLParseException(
            "Longitude must be in the range [-180, 180], instead saw: "
                + longitude
                + " at position: "
                + mParser.getPositionDescription());
      }
      double latitude = Double.parseDouble(latLongAlt[1]);
      if (Double.isNaN(latitude)) {
        throw new KMLParseException(
            "Error at: " + mParser.getPositionDescription() + " latitude cannot be NaN.");
      }
      if (latitude < -90 || latitude > 90) {
        throw new KMLParseException(
            "Latitude must be in the range [-90, 90], instead saw: "
                + latitude
                + " at position: "
                + mParser.getPositionDescription());
      }
      double altitude = 0;
      if (latLongAlt.length > 2) {
        altitude = Double.parseDouble(latLongAlt[2]);
        if (Double.isNaN(altitude)) {
          throw new KMLParseException(
              "Error at: " + mParser.getPositionDescription() + " altitude cannot be NaN.");
        }
      } else {
        altitudeReferenceSystemWrapper.setAltitudeReferenceSystem(AltitudeReferenceSystem.SURFACE);
        if (!mDidWarn) {
          ParsingHelpers.logAltitudeWarning();
          mDidWarn = true;
        }
      }
      positions.add(new Geoposition(latitude, longitude, altitude));
    }
    mParser.require(XmlPullParser.END_TAG, mNameSpace, "coordinates");
    return positions;
  }

  @NonNull
  private String parseText() throws IOException, XmlPullParserException, KMLParseException {
    if (mParser.next() != XmlPullParser.TEXT) {
      throw new KMLParseException("Expected TEXT at position: " + mParser.getPositionDescription());
    } else {
      String result = mParser.getText().trim();
      mParser.nextTag();
      return result;
    }
  }

  private int parseColor() throws XmlPullParserException, IOException, KMLParseException {
    long alphaBlueGreenRed = Long.parseLong(parseText(), 16);
    return formatColorForMapControl((int) alphaBlueGreenRed);
  }

  private static int formatColorForMapControl(int oldColor) {
    int newColor = oldColor;
    newColor = newColor & 0xFF00FF00;
    newColor = ((oldColor & 0xFF) << 16) | newColor;
    return ((oldColor & 0x00FF0000) >> 16) | newColor;
  }

  /* This method expects to begin at a start tag. If it does not see a start tag to begin with, the
   * XML is malformed and an exception is thrown.*/
  private void skipToEndOfTag() throws XmlPullParserException, IOException, KMLParseException {
    if (mParser.getEventType() != XmlPullParser.START_TAG) {
      throw new KMLParseException(
          "Expected start tag at position: " + mParser.getPositionDescription());
    }
    int depth = 1;
    while (depth != 0) {
      switch (moveToNext()) {
        case XmlPullParser.END_TAG:
          depth--;
          break;
        case XmlPullParser.START_TAG:
          depth++;
          break;
        default:
          break;
      }
    }
  }

  private void verifyElementNotSeen(@NonNull String tag, boolean hasSeenTag)
      throws KMLParseException {
    if (hasSeenTag) {
      throw new KMLParseException(
          "Error at: + "
              + mParser.getPositionDescription()
              + " Geometry Object can only contain one"
              + tag
              + " element.");
    }
  }

  private void verifyElementSeen(@NonNull String tag, boolean hasSeenTag) throws KMLParseException {
    if (!hasSeenTag) {
      throw new KMLParseException(
          "Geometry Object must contain "
              + tag
              + " element around XML position "
              + mParser.getPositionDescription());
    }
  }

  private void applyStyles() throws KMLParseException {
    for (String id : mMapElementStyles.keySet()) {
      StylesHolder stylesHolder = mMapStylesHolders.get(id);
      if (stylesHolder == null) {
        throw new KMLParseException("Style id " + id + " not found.");
      }
      for (MapElement element : mMapElementStyles.get(id)) {
        if (element instanceof MapIcon) {
          ((MapIcon) element).setImage(stylesHolder.getIconStyle().getImage());
        } else if (element instanceof MapPolyline) {
          MapPolyline line = (MapPolyline) element;
          line.setStrokeWidth(stylesHolder.getLineStyle().getWidth());
          line.setStrokeColor(stylesHolder.getLineStyle().getStrokeColor());
        } else if (element instanceof MapPolygon) {
          MapPolygon polygon = (MapPolygon) element;
          LineStyle lineStyle = stylesHolder.getLineStyle();
          PolyStyle polyStyle = stylesHolder.getPolyStyle();
          if (polyStyle.shouldOutline()) {
            polygon.setStrokeColor(lineStyle.getStrokeColor());
          } else {
            polygon.setStrokeColor(polyStyle.getTransparent());
            polygon.setStrokeWidth(lineStyle.getWidth());
          }
          if (polyStyle.shouldFill()) {
            polygon.setFillColor(polyStyle.getFillColor());
          } else {
            polygon.setFillColor(polyStyle.getTransparent());
          }
        }
      }
    }
  }

  private int moveToNext() throws IOException, XmlPullParserException, KMLParseException {
    int eventType = mParser.next();
    if (eventType == XmlPullParser.END_DOCUMENT) {
      throw new KMLParseException(
          "Unexpected end of document around position " + mParser.getPositionDescription());
    }
    return eventType;
  }
}
