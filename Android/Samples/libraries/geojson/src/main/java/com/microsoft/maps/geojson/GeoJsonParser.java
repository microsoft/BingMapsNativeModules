// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.microsoft.maps.geojson;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.microsoft.maps.AltitudeReferenceSystem;
import com.microsoft.maps.Geopath;
import com.microsoft.maps.Geopoint;
import com.microsoft.maps.Geoposition;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapPolygon;
import com.microsoft.maps.MapPolyline;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class that parses GeoJSON and returns a new MapElementLayer containing all the shapes outlined in
 * the GeoJSON.
 *
 * <p>Created by Elizabeth Bartusiak (t-elbart) on 05/22/2020
 */
public class GeoJsonParser {

  private MapElementLayer mLayer;
  private MapFactories mFactory;
  private boolean mDidWarn;

  private static final MapFactories DEFAULT_MAP_FACTORIES =
      new MapFactories() {
        @Override
        public MapGeoJsonLayer createMapElementLayer() {
          return new MapGeoJsonLayer();
        }

        @Override
        public MapIcon createMapIcon() {
          return new MapIcon();
        }

        @Override
        public MapPolyline createMapPolyline() {
          return new MapPolyline();
        }

        @Override
        public MapPolygon createMapPolygon() {
          return new MapPolygon();
        }
      };

  @VisibleForTesting
  GeoJsonParser() {}

  /**
   * Parses geojson String and returns a MapGeoJsonLayer with all shapes from the geojson String.
   *
   * @param geojson String of GeoJSON to parse
   * @return MapGeoJsonLayer containing all objects
   * @throws GeoJsonParseException
   */
  @NonNull
  public static MapGeoJsonLayer parse(@NonNull String geojson) throws GeoJsonParseException {
    if (geojson == null) {
      throw new IllegalArgumentException("Input String cannot be null.");
    }

    GeoJsonParser instance = new GeoJsonParser();
    try {
      return (MapGeoJsonLayer) instance.internalParse(geojson, DEFAULT_MAP_FACTORIES);
    } catch (JSONException e) {
      throw new GeoJsonParseException(e.getMessage());
    }
  }

  @VisibleForTesting
  @NonNull
  MapElementLayer internalParse(@NonNull String geojson, @NonNull MapFactories factory)
      throws JSONException, GeoJsonParseException {
    mLayer = factory.createMapElementLayer();
    mFactory = factory;

    JSONObject object = new JSONObject(geojson);
    String type = object.getString("type");

    if (type.equals("FeatureCollection")) {
      parseFeatureCollection(object);
    } else {
      if (type.equals("Feature")) {
        if (object.isNull("geometry")) {
          throw new GeoJsonParseException("Feature geometry cannot be null.");
        }
        verifyNoMembers(object, new String[] {"features"});
        object = object.getJSONObject("geometry");
      }
      switchToType(object);
    }
    return mLayer;
  }

  private void switchToType(@NonNull JSONObject object)
      throws JSONException, GeoJsonParseException {
    String type = object.getString("type");
    switch (type) {
      case "Polygon":
        parsePolygon(getCoordinates(object));
        break;
      case "Point":
        parsePoint(getCoordinates(object));
        break;
      case "MultiPoint":
        parseMultiPoint(getCoordinates(object));
        break;
      case "LineString":
        parseLineString(getCoordinates(object));
        break;
      case "MultiLineString":
        parseMultiLineString(getCoordinates(object));
        break;
      case "MultiPolygon":
        parseMultiPolygon(getCoordinates(object));
        break;
      case "GeometryCollection":
        parseGeometryCollection(object);
        break;
      default:
        throw new GeoJsonParseException(
            "Expected a GeoJSON Geometry type, instead saw: \"" + type + "\"");
    }
    verifyNoMembers(object, new String[] {"geometry", "properties", "features"});
  }

  @NonNull
  private JSONArray getCoordinates(@NonNull JSONObject object) throws JSONException {
    return object.getJSONArray("coordinates");
  }

  private void createIconAndAddToLayer(
      @NonNull Geoposition position, AltitudeReferenceSystem altitudeReferenceSystem) {
    MapIcon icon = mFactory.createMapIcon();
    icon.setLocation(new Geopoint(position, altitudeReferenceSystem));
    mLayer.getElements().add(icon);
  }

  private void createPolylineAndAddToLayer(
      @NonNull ArrayList<Geoposition> positions, AltitudeReferenceSystem altitudeReferenceSystem) {
    MapPolyline line = mFactory.createMapPolyline();
    line.setPath(new Geopath(positions, altitudeReferenceSystem));
    mLayer.getElements().add(line);
  }

  private void createPolygonAndAddToLayer(
      @NonNull ArrayList<ArrayList<Geoposition>> positionLists,
      AltitudeReferenceSystem altitudeReferenceSystem) {
    ArrayList<Geopath> rings = new ArrayList<>(positionLists.size());
    for (ArrayList<Geoposition> ring : positionLists) {
      Geopath path = new Geopath(ring, altitudeReferenceSystem);
      rings.add(path);
    }
    MapPolygon polygon = mFactory.createMapPolygon();
    polygon.setPaths(rings);
    mLayer.getElements().add(polygon);
  }

  private void parseGeometryCollection(@NonNull JSONObject object)
      throws JSONException, GeoJsonParseException {
    JSONArray array = object.getJSONArray("geometries");
    for (int i = 0; i < array.length(); i++) {
      JSONObject shape = array.getJSONObject(i);
      switchToType(shape);
    }
  }

  private void parseFeatureCollection(@NonNull JSONObject object)
      throws JSONException, GeoJsonParseException {
    verifyNoMembers(object, new String[] {"geometry", "properties", "coordinates", "geometries"});
    JSONArray array = object.getJSONArray("features");
    for (int i = 0; i < array.length(); i++) {
      JSONObject element = array.getJSONObject(i);
      String feature = element.getString("type");
      if (!feature.equals("Feature")) {
        throw new GeoJsonParseException(
            "GeoJSON Features must have type \"Feature\" instead saw: " + feature);
      }
      verifyNoMembers(element, new String[] {"features"});
      JSONObject shape = element.getJSONObject("geometry");
      switchToType(shape);
    }
  }

  private void parsePolygon(@NonNull JSONArray jsonRings)
      throws JSONException, GeoJsonParseException {
    AltitudeReferenceSystemWrapper altitudeReferenceSystemWrapper =
        new AltitudeReferenceSystemWrapper(AltitudeReferenceSystem.ELLIPSOID);
    ArrayList<ArrayList<Geoposition>> rings =
        parsePolygonRings(jsonRings, altitudeReferenceSystemWrapper);
    for (ArrayList<Geoposition> ring : rings) {
      setAltitudesToZeroIfAtSurface(
          ring, altitudeReferenceSystemWrapper.getAltitudeReferenceSystem());
    }
    createPolygonAndAddToLayer(rings, altitudeReferenceSystemWrapper.getAltitudeReferenceSystem());
  }

  @NonNull
  private ArrayList<ArrayList<Geoposition>> parsePolygonRings(
      @NonNull JSONArray jsonRings,
      @NonNull AltitudeReferenceSystemWrapper altitudeReferenceSystemWrapper)
      throws JSONException, GeoJsonParseException {
    ArrayList<ArrayList<Geoposition>> rings = new ArrayList<>(jsonRings.length());
    for (int i = 0; i < jsonRings.length(); i++) {
      JSONArray pathArray = jsonRings.getJSONArray(i);
      ArrayList<Geoposition> path = parsePositionArray(pathArray, altitudeReferenceSystemWrapper);
      verifyPolygonRing(path);
      rings.add(path);
    }
    return rings;
  }

  private void parseMultiPolygon(@NonNull JSONArray coordinates)
      throws JSONException, GeoJsonParseException {
    ArrayList<ArrayList> polygons = new ArrayList<>(coordinates.length());
    AltitudeReferenceSystemWrapper altitudeReferenceSystemWrapper =
        new AltitudeReferenceSystemWrapper(AltitudeReferenceSystem.ELLIPSOID);
    for (int i = 0; i < coordinates.length(); i++) {
      JSONArray jsonRings = coordinates.getJSONArray(i);
      polygons.add(parsePolygonRings(jsonRings, altitudeReferenceSystemWrapper));
    }
    for (ArrayList<ArrayList> polygonRings : polygons) {
      for (ArrayList<Geoposition> ring : polygonRings) {
        setAltitudesToZeroIfAtSurface(
            ring, altitudeReferenceSystemWrapper.getAltitudeReferenceSystem());
      }
    }
    for (ArrayList<ArrayList<Geoposition>> polygon : polygons) {
      createPolygonAndAddToLayer(
          polygon, altitudeReferenceSystemWrapper.getAltitudeReferenceSystem());
    }
  }

  private static void verifyPolygonRing(@NonNull ArrayList<Geoposition> positions)
      throws GeoJsonParseException {
    if (positions.size() < 4) {
      StringBuilder positionsStringBuilder = new StringBuilder();
      for (Geoposition position : positions) {
        positionsStringBuilder.append(position).append(", ");
      }
      positionsStringBuilder.append(positions.get(positions.size() - 1));
      throw new GeoJsonParseException(
          "Polygon ring must have at least 4 positions, "
              + "and the first and last position must be the same. Instead saw Geopositions: ["
              + positionsStringBuilder
              + "].");
    }

    Geoposition firstPosition = positions.get(0);
    Geoposition lastPosition = positions.get(positions.size() - 1);
    if (firstPosition.getLongitude() != lastPosition.getLongitude()
        || firstPosition.getLatitude() != lastPosition.getLatitude()
        || firstPosition.getAltitude() != lastPosition.getAltitude()) {
      throw new GeoJsonParseException(
          "First and last coordinate pair of each polygon ring must be the same. Instead saw Geopositions: first: "
              + firstPosition
              + " last: "
              + lastPosition);
    }
  }

  private void parsePoint(@NonNull JSONArray coordinates)
      throws JSONException, GeoJsonParseException {
    Geoposition position = parseGeoposition(coordinates);
    AltitudeReferenceSystemWrapper altitudeReferenceSystemWrapper =
        new AltitudeReferenceSystemWrapper(AltitudeReferenceSystem.ELLIPSOID);
    if (coordinates.length() < 3) {
      altitudeReferenceSystemWrapper.setAltitudeReferenceSystem(AltitudeReferenceSystem.SURFACE);
    }
    createIconAndAddToLayer(position, altitudeReferenceSystemWrapper.getAltitudeReferenceSystem());
  }

  private void parseMultiPoint(@NonNull JSONArray coordinates)
      throws JSONException, GeoJsonParseException {
    AltitudeReferenceSystemWrapper altitudeReferenceSystemWrapper =
        new AltitudeReferenceSystemWrapper(AltitudeReferenceSystem.ELLIPSOID);
    ArrayList<Geoposition> positions =
        parsePositionArray(coordinates, altitudeReferenceSystemWrapper);
    setAltitudesToZeroIfAtSurface(
        positions, altitudeReferenceSystemWrapper.getAltitudeReferenceSystem());
    for (Geoposition position : positions) {
      createIconAndAddToLayer(
          position, altitudeReferenceSystemWrapper.getAltitudeReferenceSystem());
    }
  }

  @Nullable
  private ArrayList<Geoposition> parseLineArray(
      @NonNull JSONArray jsonArray,
      @NonNull AltitudeReferenceSystemWrapper altitudeReferenceSystemWrapper)
      throws GeoJsonParseException, JSONException {
    if (jsonArray.length() < 2) {
      throw new GeoJsonParseException(
          "Linestring must contain at least 2 positions. Instead saw: " + jsonArray);
    }
    return parsePositionArray(jsonArray, altitudeReferenceSystemWrapper);
  }

  private void parseLineString(@NonNull JSONArray pathArray)
      throws JSONException, GeoJsonParseException {
    AltitudeReferenceSystemWrapper altitudeReferenceSystemWrapper =
        new AltitudeReferenceSystemWrapper(AltitudeReferenceSystem.ELLIPSOID);
    ArrayList<Geoposition> positions = parseLineArray(pathArray, altitudeReferenceSystemWrapper);
    setAltitudesToZeroIfAtSurface(
        positions, altitudeReferenceSystemWrapper.getAltitudeReferenceSystem());
    createPolylineAndAddToLayer(
        positions, altitudeReferenceSystemWrapper.getAltitudeReferenceSystem());
  }

  private void parseMultiLineString(@NonNull JSONArray coordinates)
      throws JSONException, GeoJsonParseException {
    ArrayList<ArrayList<Geoposition>> lines = new ArrayList<>(coordinates.length());
    AltitudeReferenceSystemWrapper altitudeReferenceSystemWrapper =
        new AltitudeReferenceSystemWrapper(AltitudeReferenceSystem.ELLIPSOID);
    for (int i = 0; i < coordinates.length(); i++) {
      JSONArray pathArray = coordinates.getJSONArray(i);
      lines.add(parsePositionArray(pathArray, altitudeReferenceSystemWrapper));
    }
    for (ArrayList<Geoposition> line : lines) {
      setAltitudesToZeroIfAtSurface(
          line, altitudeReferenceSystemWrapper.getAltitudeReferenceSystem());
      createPolylineAndAddToLayer(
          line, altitudeReferenceSystemWrapper.getAltitudeReferenceSystem());
    }
  }

  private static void verifyNoMembers(@NonNull JSONObject object, @NonNull String[] members)
      throws JSONException, GeoJsonParseException {
    for (String str : members) {
      if (object.has(str)) {
        String type = object.getString("type");
        throw new GeoJsonParseException(type + " cannot have a \"" + str + "\" member.");
      }
    }
  }

  @NonNull
  private Geoposition parseGeoposition(@NonNull JSONArray coordinates)
      throws JSONException, GeoJsonParseException {
    if (coordinates.length() >= 2) {
      double longitude = coordinates.getDouble(0);
      if (longitude < -180 || longitude > 180) {
        throw new GeoJsonParseException(
            "Longitude must be in the range [-180, 180], instead saw: " + longitude);
      }

      double latitude = coordinates.getDouble(1);
      if (latitude < -90 || latitude > 90) {
        throw new GeoJsonParseException(
            "Latitude must be in the range [-90, 90], instead saw: " + latitude);
      }

      if (coordinates.length() > 2) {
        double altitude = coordinates.getDouble(2);
        return new Geoposition(latitude, longitude, altitude);
      }
      return new Geoposition(latitude, longitude);
    } else {
      throw new GeoJsonParseException(
          "coordinates array must contain at least latitude and longitude, instead saw: "
              + coordinates.toString());
    }
  }

  @NonNull
  private ArrayList<Geoposition> parsePositionArray(
      @NonNull JSONArray pathArray,
      @NonNull AltitudeReferenceSystemWrapper altitudeReferenceSystemWrapper)
      throws JSONException, GeoJsonParseException {
    ArrayList<Geoposition> path = new ArrayList<>(pathArray.length());
    for (int i = 0; i < pathArray.length(); i++) {
      JSONArray latLong = pathArray.getJSONArray(i);
      Geoposition position = parseGeoposition(latLong);
      if (latLong.length() < 3) {
        altitudeReferenceSystemWrapper.setAltitudeReferenceSystem(AltitudeReferenceSystem.SURFACE);
        if (!mDidWarn) {
          Log.w(
              "Altitude",
              "Unless all positions in a Geometry Object contain an altitude coordinate, all altitudes will be set to 0 at surface level for that Geometry Object.");
          mDidWarn = true;
        }
      }
      path.add(position);
    }
    return path;
  }

  private void setAltitudesToZeroIfAtSurface(
      @NonNull ArrayList<Geoposition> positions, AltitudeReferenceSystem altitudeReferenceSystem) {
    if (altitudeReferenceSystem == AltitudeReferenceSystem.SURFACE) {
      for (Geoposition position : positions) {
        position.setAltitude(0);
      }
    }
  }
}
