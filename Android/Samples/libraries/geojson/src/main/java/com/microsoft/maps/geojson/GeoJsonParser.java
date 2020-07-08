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
  private AltitudeReferenceSystem mAltitudeReferenceSystem = AltitudeReferenceSystem.ELLIPSOID;

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

  private void createIconAndAddToLayer(@NonNull Geoposition position) {
    MapIcon icon = mFactory.createMapIcon();
    icon.setLocation(new Geopoint(position, mAltitudeReferenceSystem));
    mLayer.getElements().add(icon);
  }

  private void createLineAndAddToLayer(@NonNull ArrayList<Geoposition> positions) {
    MapPolyline line = mFactory.createMapPolyline();
    line.setPath(new Geopath(positions, mAltitudeReferenceSystem));
    mLayer.getElements().add(line);
  }

  private void createPolygonAndAddToLayer(
      @NonNull ArrayList<ArrayList<Geoposition>> positionLists) {
    ArrayList<Geopath> rings = new ArrayList<>(positionLists.size());
    for (ArrayList<Geoposition> ring : positionLists) {
      GeopathIndexed path = new GeopathIndexed(ring, mAltitudeReferenceSystem);
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
    mAltitudeReferenceSystem = AltitudeReferenceSystem.ELLIPSOID;
    ArrayList<ArrayList<Geoposition>> rings = parsePolygonRings(jsonRings);
    for (ArrayList<Geoposition> ring : rings) {
      setAltitudesToZeroIfAtSurface(ring);
    }
    createPolygonAndAddToLayer(rings);
  }

  @NonNull
  private ArrayList<ArrayList<Geoposition>> parsePolygonRings(@NonNull JSONArray jsonRings)
      throws JSONException, GeoJsonParseException {
    ArrayList<ArrayList<Geoposition>> rings = new ArrayList<>(jsonRings.length());
    for (int i = 0; i < jsonRings.length(); i++) {
      JSONArray pathArray = jsonRings.getJSONArray(i);
      verifyPolygonRing(pathArray);
      ArrayList<Geoposition> path = parsePositionArray(pathArray);
      rings.add(path);
    }
    return rings;
  }

  private void parseMultiPolygon(@NonNull JSONArray coordinates)
      throws JSONException, GeoJsonParseException {
    ArrayList<ArrayList> polygons = new ArrayList(coordinates.length());
    mAltitudeReferenceSystem = AltitudeReferenceSystem.ELLIPSOID;
    for (int i = 0; i < coordinates.length(); i++) {
      JSONArray jsonRings = coordinates.getJSONArray(i);
      polygons.add(parsePolygonRings(jsonRings));
    }
    for (int i = 0; i < polygons.size(); i++) {
      ArrayList<ArrayList> polygonRings = polygons.get(i);
      for (int ring = 0; ring < polygonRings.size(); ring++) {
        setAltitudesToZeroIfAtSurface(polygonRings.get(ring));
      }
    }
    for (int i = 0; i < polygons.size(); i++) {
      createPolygonAndAddToLayer(polygons.get(i));
    }
  }

  private static void verifyPolygonRing(@NonNull JSONArray jsonArray)
      throws GeoJsonParseException, JSONException {
    if (jsonArray.length() < 4) {
      throw new GeoJsonParseException(
          "Polygon ring must have at least 4 positions, "
              + "and the first and last position must be the same.");
    }

    JSONArray firstPosition = jsonArray.getJSONArray(0);
    JSONArray lastPosition = jsonArray.getJSONArray(jsonArray.length() - 1);
    if (firstPosition.length() != lastPosition.length()) {
      throw new GeoJsonParseException(
          "First and last coordinate pair of each polygon ring must be the same. Instead saw: "
              + jsonArray);
    }
    for (int i = 0; i < firstPosition.length(); i++) {
      if (firstPosition.get(i) != lastPosition.get(i)) {
        throw new GeoJsonParseException(
            "First and last coordinate pair of each polygon ring must be the same. Instead saw: "
                + jsonArray);
      }
    }
  }

  private void parsePoint(@NonNull JSONArray coordinates)
      throws JSONException, GeoJsonParseException {
    Geoposition position = parseGeoposition(coordinates);
    mAltitudeReferenceSystem =
        coordinates.length() > 2
            ? AltitudeReferenceSystem.ELLIPSOID
            : AltitudeReferenceSystem.SURFACE;
    createIconAndAddToLayer(position);
  }

  private void parseMultiPoint(@NonNull JSONArray coordinates)
      throws JSONException, GeoJsonParseException {
    mAltitudeReferenceSystem = AltitudeReferenceSystem.ELLIPSOID;
    ArrayList<Geoposition> positions = parsePositionArray(coordinates);
    setAltitudesToZeroIfAtSurface(positions);
    for (Geoposition position : positions) {
      createIconAndAddToLayer(position);
    }
  }

  @Nullable
  private ArrayList<Geoposition> parseLineArray(@NonNull JSONArray jsonArray)
      throws GeoJsonParseException, JSONException {
    if (jsonArray.length() < 2) {
      throw new GeoJsonParseException(
          "Linestring must contain at least 2 positions. Instead saw: " + jsonArray);
    }
    return parsePositionArray(jsonArray);
  }

  private void parseLineString(@NonNull JSONArray pathArray)
      throws JSONException, GeoJsonParseException {
    mAltitudeReferenceSystem = AltitudeReferenceSystem.ELLIPSOID;
    ArrayList<Geoposition> positions = parseLineArray(pathArray);
    setAltitudesToZeroIfAtSurface(positions);
    createLineAndAddToLayer(positions);
  }

  private void parseMultiLineString(@NonNull JSONArray coordinates)
      throws JSONException, GeoJsonParseException {
    ArrayList<ArrayList<Geoposition>> lines = new ArrayList<>(coordinates.length());
    mAltitudeReferenceSystem = AltitudeReferenceSystem.ELLIPSOID;
    for (int i = 0; i < coordinates.length(); i++) {
      JSONArray pathArray = coordinates.getJSONArray(i);
      lines.add(parsePositionArray(pathArray));
    }
    for (ArrayList<Geoposition> line : lines) {
      setAltitudesToZeroIfAtSurface(line);
      createLineAndAddToLayer(line);
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
  private ArrayList<Geoposition> parsePositionArray(@NonNull JSONArray pathArray)
      throws JSONException, GeoJsonParseException {
    ArrayList<Geoposition> path = new ArrayList<>(pathArray.length());
    for (int i = 0; i < pathArray.length(); i++) {
      JSONArray latLong = pathArray.getJSONArray(i);
      Geoposition position = parseGeoposition(latLong);
      if (latLong.length() < 3) {
        mAltitudeReferenceSystem = AltitudeReferenceSystem.SURFACE;
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

  private void setAltitudesToZeroIfAtSurface(@NonNull ArrayList<Geoposition> positions) {
    if (mAltitudeReferenceSystem == AltitudeReferenceSystem.SURFACE) {
      for (Geoposition position : positions) {
        position.setAltitude(0);
      }
    }
  }
}
