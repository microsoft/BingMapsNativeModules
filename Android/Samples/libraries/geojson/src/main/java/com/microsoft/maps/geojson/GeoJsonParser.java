package com.microsoft.maps.geojson;

import androidx.annotation.NonNull;
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
 *
 * <p>TODO: return a MapGeoJsonLayer instead of MapElementLayer
 */
public class GeoJsonParser {

  private MapElementLayer mLayer;
  private MapFactories mFactory;

  private static final MapFactories DEFAULT_MAP_FACTORIES =
      new MapFactories() {
        @Override
        public MapElementLayer createMapElementLayer() {
          return new MapElementLayer();
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
   * TODO: return MapGeoJsonLayer
   *
   * @param geojson String of GeoJSON to parse
   * @return MapElementLayer containing all objects
   * @throws GeoJsonParseException
   */
  @NonNull
  public static MapElementLayer parse(@NonNull String geojson) throws GeoJsonParseException {
    if (geojson == null) {
      throw new IllegalArgumentException("Input String cannot be null.");
    }

    GeoJsonParser instance = new GeoJsonParser();
    try {
      return instance.internalParse(geojson, DEFAULT_MAP_FACTORIES);
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
        parseMultiPoint(object);
        break;
      case "LineString":
        parseLineString(getCoordinates(object));
        break;
      case "MultiLineString":
        parseMultiLineString(object);
        break;
      case "MultiPolygon":
        parseMultiPolygon(object);
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
    ArrayList<Geopath> rings = new ArrayList<>(jsonRings.length());
    for (int i = 0; i < jsonRings.length(); i++) {
      JSONArray pathArray = jsonRings.getJSONArray(i);
      GeopathIndexed path = parsePath(pathArray);
      verifyPolygonRing(path);
      rings.add(path);
    }
    MapPolygon poly = mFactory.createMapPolygon();
    poly.setPaths(rings);
    mLayer.getElements().add(poly);
  }

  private static void verifyPolygonRing(@NonNull GeopathIndexed path) throws GeoJsonParseException {
    if (path.size() < 4) {
      throw new GeoJsonParseException(
          "Polygon ring must have at least 4 positions, "
              + "and the first and last position must be the same.");
    }

    Geoposition firstPosition = path.get(0);
    Geoposition lastPosition = path.get(path.size() - 1);
    if (firstPosition.getLatitude() != lastPosition.getLatitude()
        || firstPosition.getLongitude() != lastPosition.getLongitude()
        || firstPosition.getAltitude() != lastPosition.getAltitude()) {
      throw new GeoJsonParseException(
          "First and last coordinate pair of each polygon ring must be the same.");
    }
  }

  private void parseMultiPolygon(@NonNull JSONObject shape)
      throws JSONException, GeoJsonParseException {
    JSONArray coordinates = shape.getJSONArray("coordinates");
    for (int k = 0; k < coordinates.length(); k++) {
      JSONArray jsonRings = coordinates.getJSONArray(k);
      parsePolygon(jsonRings);
    }
  }

  private void parsePoint(@NonNull JSONArray coordinates)
      throws JSONException, GeoJsonParseException {
    Geoposition position = parseGeoposition(coordinates);
    Geopoint point;
    if (coordinates.length() > 2) {
      point = new Geopoint(position, AltitudeReferenceSystem.ELLIPSOID);
    } else {
      point = new Geopoint(position);
    }
    MapIcon pushpin = mFactory.createMapIcon();
    pushpin.setLocation(point);
    mLayer.getElements().add(pushpin);
  }

  private void parseMultiPoint(@NonNull JSONObject shape)
      throws JSONException, GeoJsonParseException {
    JSONArray coordinates = shape.getJSONArray("coordinates");
    for (int i = 0; i < coordinates.length(); i++) {
      JSONArray latLong = coordinates.getJSONArray(i);
      parsePoint(latLong);
    }
  }

  private void parseLineString(@NonNull JSONArray pathArray)
      throws JSONException, GeoJsonParseException {
    MapPolyline line = mFactory.createMapPolyline();
    line.setPath(parsePath(pathArray));
    mLayer.getElements().add(line);
  }

  private void parseMultiLineString(@NonNull JSONObject shape)
      throws JSONException, GeoJsonParseException {
    JSONArray coordinates = shape.getJSONArray("coordinates");
    for (int i = 0; i < coordinates.length(); i++) {
      JSONArray pathArray = coordinates.getJSONArray(i);
      parseLineString(pathArray);
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
  private GeopathIndexed parsePath(@NonNull JSONArray pathArray)
      throws JSONException, GeoJsonParseException {
    ArrayList<Geoposition> path = new ArrayList<>(pathArray.length());
    AltitudeReferenceSystem ref = AltitudeReferenceSystem.SURFACE;
    for (int j = 0; j < pathArray.length(); j++) {
      JSONArray latLong = pathArray.getJSONArray(j);
      Geoposition position = parseGeoposition(latLong);
      if (latLong.length() > 2) {
        ref = AltitudeReferenceSystem.ELLIPSOID;
      }
      path.add(position);
    }
    return new GeopathIndexed(path, ref);
  }
}
