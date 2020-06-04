package com.microsoft.maps;

import android.opengl.GLException;
import android.util.Log;

import androidx.annotation.VisibleForTesting;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.helpers.XMLReaderFactory;

import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

/**
 * Class that parses GeoJSON and returns a new MapElementLayer containing all the shapes outlined in
 * the GeoJSON.
 *
 * <p>Created by Elizabeth Bartusiak (t-elbart) on 05/22/2020
 *
 * <p>TODO: return a MapGeoJsonLayer instead of MapElementLayer TODO: Make parser asynchronous
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
  public static MapElementLayer parse(String geojson) throws GeoJsonParseException {
    GeoJsonParser instance = new GeoJsonParser();
    try {
      return instance.internalParse(geojson, DEFAULT_MAP_FACTORIES);
    } catch (JSONException e) {
      throw new GeoJsonParseException(e.getMessage());
    }
  }

  @VisibleForTesting
  MapElementLayer internalParse(String geojson, MapFactories factory)
      throws JSONException, GeoJsonParseException {
    mLayer = factory.createMapElementLayer();
    mFactory = factory;

    JSONObject object = new JSONObject(geojson);
    ;
    String type = object.getString("type");

    if (type.equals("FeatureCollection")) {
      parseFeatureCollection(object);
    } else {
      if (type.equals("Feature")) {
        if (object.isNull("geometry")) {
          String message = "Feature geometry cannot be null.";
          throw new GeoJsonParseException(message);
        }
        object = object.getJSONObject("geometry");
      }
      switchToType(object);
    }
    return mLayer;
  }

  private void switchToType(JSONObject object) throws JSONException, GeoJsonParseException {
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
        String message = "Expected a GeoJSON Geometry Type, instead saw: \"" + type + "\"";
        throw new GeoJsonParseException(message);
    }
  }

  private JSONArray getCoordinates(JSONObject object) throws JSONException {
    return object.getJSONArray("coordinates");
  }

  private void parseGeometryCollection(JSONObject object)
      throws JSONException, GeoJsonParseException {
    JSONArray array = object.getJSONArray("geometries");

    for (int i = 0; i < array.length(); i++) {
      JSONObject shape = array.getJSONObject(i);
      switchToType(shape);
    }
  }

  private void parseFeatureCollection(JSONObject object)
      throws JSONException, GeoJsonParseException {
    JSONArray array = object.getJSONArray("features");

    for (int i = 0; i < array.length(); i++) {
      JSONObject shape = array.getJSONObject(i).getJSONObject("geometry");
      switchToType(shape);
    }
  }

  private void parsePolygon(JSONArray jsonRings) throws JSONException, GeoJsonParseException {
    ArrayList<Geopath> rings = new ArrayList<>(jsonRings.length());
    for (int i = 0; i < jsonRings.length(); i++) {
      JSONArray pathArray = jsonRings.getJSONArray(i);
      rings.add(parsePath(pathArray));
    }
    MapPolygon poly = mFactory.createMapPolygon();
    poly.setPaths(rings);
    mLayer.getElements().add(poly);
  }

  private void parseMultiPolygon(JSONObject shape) throws JSONException, GeoJsonParseException {
    JSONArray coordinates = shape.getJSONArray("coordinates");

    for (int k = 0; k < coordinates.length(); k++) {
      JSONArray jsonRings = coordinates.getJSONArray(k);
      parsePolygon(jsonRings);
    }
  }

  private void parsePoint(JSONArray coordinates) throws JSONException, GeoJsonParseException {
    Geoposition position = parseGeoposition(coordinates);
    Geopoint point = new Geopoint(position);
    MapIcon pushpin = mFactory.createMapIcon();
    pushpin.setLocation(point);
    mLayer.getElements().add(pushpin);
  }

  private void parseMultiPoint(JSONObject shape) throws JSONException, GeoJsonParseException {
    JSONArray coordinates = shape.getJSONArray("coordinates");

    for (int i = 0; i < coordinates.length(); i++) {
      JSONArray latLong = coordinates.getJSONArray(i);
      parsePoint(latLong);
    }
  }

  private void parseLineString(JSONArray pathArray) throws JSONException, GeoJsonParseException {
    MapPolyline line = mFactory.createMapPolyline();
    line.setPath(parsePath(pathArray));
    mLayer.getElements().add(line);
  }

  private void parseMultiLineString(JSONObject shape) throws JSONException, GeoJsonParseException {
    JSONArray coordinates = shape.getJSONArray("coordinates");

    for (int i = 0; i < coordinates.length(); i++) {
      JSONArray pathArray = coordinates.getJSONArray(i);
      parseLineString(pathArray);
    }
  }

  private Geoposition parseGeoposition(JSONArray coordinates)
      throws JSONException, GeoJsonParseException {
    if (coordinates.length() >= 2) {
      double longitude = coordinates.getDouble(0);
      double latitude = coordinates.getDouble(1);

      if (coordinates.length() > 2) {
        double altitude = coordinates.getDouble(2);
        return new Geoposition(latitude, longitude, altitude);
      }
      return new Geoposition(latitude, longitude);
    } else {
      String message =
          "coordinates array must contain at least latitude and longitude,"
              + " instead saw: "
              + coordinates.toString();
      throw new GeoJsonParseException(message);
    }
  }

  private Geopath parsePath(JSONArray pathArray) throws JSONException, GeoJsonParseException {
    ArrayList<Geoposition> path = new ArrayList<>(pathArray.length());
    for (int j = 0; j < pathArray.length(); j++) {
      JSONArray latLong = pathArray.getJSONArray(j);
      Geoposition position = parseGeoposition(latLong);
      path.add(position);
    }
    return new Geopath(path, AltitudeReferenceSystem.ELLIPSOID);
  }
}
