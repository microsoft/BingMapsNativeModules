// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.kml;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.util.Xml;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.microsoft.maps.AltitudeReferenceSystem;
import com.microsoft.maps.Geopoint;
import com.microsoft.maps.Geoposition;
import com.microsoft.maps.MapElement;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.moduletools.AltitudeReferenceSystemWrapper;
import com.microsoft.maps.moduletools.DefaultMapFactories;
import com.microsoft.maps.moduletools.MapFactories;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
  private String mNameSpace;
  private final XmlPullParser mParser = Xml.newPullParser();

  private static final MapFactories DEFAULT_MAP_FACTORIES = new DefaultMapFactories();

  @VisibleForTesting
  KMLParser(@NonNull MapFactories factory) {
    mFactory = factory;
    mLayer = mFactory.createMapElementLayer();
  }

  /**
   * Method to parse given kml and return MapElementLayer containing the shapes outlined in the kml.
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
      parsePlacemark();
    }
    return mLayer;
  }

  private void parsePlacemark() throws IOException, XmlPullParserException, KMLParseException {
    while (mParser.next() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String type = mParser.getName();
      if (type.equals("Placemark")) {
        parseNameAndShape();
      }
    }
  }

  private void parseNameAndShape() throws IOException, XmlPullParserException, KMLParseException {
    String title = null;
    MapElement element = null;
    while (mParser.next() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String type = mParser.getName();
      switch (type) {
        case "name":
          mParser.require(XmlPullParser.START_TAG, mNameSpace, "name");
          title = parseText();
          mParser.require(XmlPullParser.END_TAG, mNameSpace, "name");
          break;
        case "Point":
          element = parsePoint();
          break;
        default:
          skipToEndOfTag();
      }
    }
    if (element != null) {
      if (title != null && element instanceof MapIcon) {
        ((MapIcon) element).setTitle(title);
      }
      mLayer.getElements().add(element);
    }
  }

  @NonNull
  private MapIcon parsePoint() throws IOException, XmlPullParserException, KMLParseException {
    mParser.require(XmlPullParser.START_TAG, mNameSpace, "Point");
    while (mParser.next() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String type = mParser.getName();
      if (type.equals("coordinates")) {
        AltitudeReferenceSystemWrapper altitudeReferenceSystemWrapper =
            new AltitudeReferenceSystemWrapper(AltitudeReferenceSystem.GEOID);
        // parseCoordinates throws an error if coordinates given are not valid.
        // Otherwise, the following coordinates ArrayList will contain at least one Geoposition.
        ArrayList<Geoposition> coordinates = parseCoordinates(altitudeReferenceSystemWrapper);
        if (coordinates.size() > 1) {
          throw new KMLParseException(
              "coordinates for a Point can only contain one position. Instead saw: "
                  + coordinates.size()
                  + " at position: "
                  + mParser.getPositionDescription());
        }
        MapIcon icon = mFactory.createMapIcon();
        icon.setLocation(
            new Geopoint(
                coordinates.get(0), altitudeReferenceSystemWrapper.getAltitudeReferenceSystem()));
        mParser.require(XmlPullParser.END_TAG, mNameSpace, "Point");
        return icon;
      } else {
        skipToEndOfTag();
      }
    }
    // No coordintes element seen; throw error
    throw new KMLParseException(
        "<Point> must contain <coordinates> around XML position "
            + mParser.getPositionDescription());
  }

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
      }
      positions.add(new Geoposition(latitude, longitude, altitude));
    }
    mParser.require(XmlPullParser.END_TAG, mNameSpace, "coordinates");
    mParser.nextTag();
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

  /* This method expects to begin at a start tag. If it does not see a start tag to begin with, the
   * XML is malformed and an exception is thrown.*/
  private void skipToEndOfTag() throws XmlPullParserException, IOException, KMLParseException {
    if (mParser.getEventType() != XmlPullParser.START_TAG) {
      throw new KMLParseException(
          "Expected start tag at position: " + mParser.getPositionDescription());
    }
    int depth = 1;
    while (depth != 0) {
      switch (mParser.next()) {
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
}
