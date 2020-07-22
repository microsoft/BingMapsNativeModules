// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.kml;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.util.Xml;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.moduletools.DefaultMapFactories;
import com.microsoft.maps.moduletools.MapFactories;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Class that parses KML and returns a new MapElementLayer containing all the shapes outlined in the
 * KML.
 *
 * <p>Created by Elizabeth Bartusiak (t-elbart) on 07/21/2020
 */
public class KMLParser {

  private MapElementLayer mLayer;
  private MapFactories mFactory;
  private XmlPullParser mParser = Xml.newPullParser();
  private String mNameSpace;
  private static final MapFactories DEFAULT_MAP_FACTORIES = new DefaultMapFactories();

  @VisibleForTesting
  KMLParser() {}

  @NonNull
  public static MapElementLayer parse(@NonNull String kml) throws KMLParseException {
    if (kml == null) {
      throw new IllegalArgumentException("Input String cannot be null.");
    }
    if (kml.equals("")) {
      throw new KMLParseException("Input String cannot be empty.");
    }
    KMLParser instance = new KMLParser();
    try {
      return instance.internalParse(kml, DEFAULT_MAP_FACTORIES);
    } catch (Exception e) {
      throw new KMLParseException(e.getMessage());
    }
  }

  @VisibleForTesting
  @NonNull
  MapElementLayer internalParse(@NonNull String kml, @NonNull MapFactories factory)
      throws XmlPullParserException, IOException, KMLParseException {
    mFactory = factory;
    mLayer = mFactory.createMapElementLayer();
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
        // used for MapIcon Title
        String name = parseName();
      }
    }
  }

  @Nullable
  private String parseName() throws IOException, XmlPullParserException, KMLParseException {
    while (mParser.next() != XmlPullParser.END_TAG) {
      if (mParser.getEventType() != XmlPullParser.START_TAG) {
        continue;
      }
      String type = mParser.getName();
      if (type.equals("name")) {
        mParser.require(XmlPullParser.START_TAG, mNameSpace, "name");
        String title = parseText();
        mParser.require(XmlPullParser.END_TAG, mNameSpace, "name");
        return title;
      } else {
        skip();
      }
    }
    return null;
  }

  @Nullable
  private String parseText() throws IOException, XmlPullParserException {
    String result = null;
    if (mParser.next() == XmlPullParser.TEXT) {
      result = mParser.getText();
      mParser.nextTag();
    }
    return result;
  }

  private void skip() throws XmlPullParserException, IOException, KMLParseException {
    if (mParser.getEventType() != XmlPullParser.START_TAG) {
      throw new KMLParseException("Expected start tag.");
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
