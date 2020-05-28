package com.example.mapspractice;

import com.microsoft.maps.Geopath;
import com.microsoft.maps.Geopoint;
import com.microsoft.maps.Geoposition;
import com.microsoft.maps.MapElementLayer;
import com.microsoft.maps.MapIcon;
import com.microsoft.maps.MapPolygon;
import com.microsoft.maps.MapPolyline;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Class that parses GeoJSON and returns a new MapElementLayer containing all the shapes outlined in
 * the GeoJSON. Altitude coordinate is currently not implemented.
 *
 * <p>Created by Elizabeth Bartusiak (t-elbart) on 05/22/2020
 *
 * <p>TODO: return a MapGeoJsonLayer instead of MapElementLayer
 * TODO: throw custom GeoJSONException instead of JSONException
 * TODO: Make parser asynchronous
 * TODO: Look into handling properties defined in Features
 */
public class GeoJsonParser {

  private static MapElementLayer mLayer;

  /**
   * TODO: return MapGeoJsonLayer
   * TODO: throw GeoJSONException
   *
   * @param geojson String of GeoJSON to parse
   * @return MapElementLayer containing all objects
   * @throws JSONException thrown for ill-formed GeoJSON
   */
  public static MapElementLayer parse(String geojson) throws JSONException {
    mLayer = new MapElementLayer();
    JSONObject object = new JSONObject(geojson);
    String type = object.getString("type");

    if (type.equals("FeatureCollection")) {
      parseFeatureCollection(object);
    } else {
      if (type.equals("Feature")) {
        object = object.getJSONObject("geometry");
        type = object.getString("type");
      }

      switch (type) {
        case "Polygon":
          parsePolygon(object);
          break;
        case "Point":
          parsePoint(object);
          break;
        case "MultiPoint":
          parseMultiPoint(object);
          break;
        case "LineString":
          parseLineString(object);
          break;
        case "MultiLineString":
          parseMultiLineString(object);
          break;
        case "MultiPolygon":
          parseMultiPolygon(object);
          break;
      }
    }

    return mLayer;
  }

  /**
   * TODO: error handling, returning false upon parsing failure
   * TODO: throw GeoJSONException
   *
   * @param object
   * @return true on successfully adding GeometryCollection to MapLayer, false otherwise
   * @throws JSONException
   */
  private static boolean parseGeometryCollection(JSONObject object) throws JSONException {
    JSONArray array = object.getJSONArray("geometries");

    for (int i = 0; i < array.length(); i++) {
      JSONObject shape = array.getJSONObject(i);

      String shapeType = shape.getString("type");
      switch (shapeType) {
        case "Polygon":
          parsePolygon(shape);
          break;
        case "Point":
          parsePoint(shape);
          break;
        case "MultiPoint":
          parseMultiPoint(shape);
          break;
        case "LineString":
          parseLineString(shape);
          break;
        case "MultiLineString":
          parseMultiLineString(shape);
          break;
        case "MultiPolygon":
          parseMultiPolygon(shape);
          break;
        case "GeometryCollection":
          // GeoJson specifications advise against nested GeometryCollections, but they are not
          // strictly prohibited
          parseGeometryCollection(shape);
          break;
      }
    }

    return true;
  }

  /**
   * TODO: error handling, returning false upon parsing failure
   * TODO: throw GeoJSONException
   *
   * @param object
   * @return true on successfully adding GeometryCollection to MapLayer, false otherwise
   * @throws JSONException
   */
  private static boolean parseFeatureCollection(JSONObject object) throws JSONException {
    JSONArray array = object.getJSONArray("features");

    for (int i = 0; i < array.length(); i++) {
      JSONObject shape = array.getJSONObject(i).getJSONObject("geometry");
      String shapeType = shape.getString("type");

      switch (shapeType) {
        case "Polygon":
          parsePolygon(shape);
          break;
        case "Point":
          parsePoint(shape);
          break;
        case "MultiPoint":
          parseMultiPoint(shape);
          break;
        case "LineString":
          parseLineString(shape);
          break;
        case "MultiLineString":
          parseMultiLineString(shape);
          break;
        case "MultiPolygon":
          parseMultiPolygon(shape);
          break;
        case "GeometryCollection":
          parseGeometryCollection(shape);
          break;
      }
    }

    return true;
  }

  /**
   * TODO: error handling, returning false upon parsing failure
   * TODO: throw GeoJSONException
   * TODO: Allow for altitude in coordinates?
   *
   * @param shape
   * @return true on successfully adding GeometryCollection to MapLayer, false otherwise
   * @throws JSONException
   */
  private static boolean parsePolygon(JSONObject shape) throws JSONException {
    // initialize to null in case there is an exception in subsequent lines?
    MapPolygon poly = new MapPolygon();

    JSONArray coordinates = shape.getJSONArray("coordinates");
    ArrayList<Geopath> allPaths = new ArrayList<>();

    for (int i = 0; i < coordinates.length(); i++) {
      JSONArray pathArray = coordinates.getJSONArray(i);
      ArrayList<Geoposition> points = new ArrayList<>();

      for (int j = 0; j < pathArray.length(); j++) {
        double longitude = pathArray.getJSONArray(j).getDouble(0);
        double latitude = pathArray.getJSONArray(j).getDouble(1);
        points.add(new Geoposition(latitude, longitude));
      }
      allPaths.add(new Geopath(points));
    }
    poly.setPaths(allPaths);
    mLayer.getElements().add(poly);

    return true;
  }

  /**
   * TODO: error handling, returning false upon parsing failure
   * TODO: throw GeoJSONException
   * TODO: Allow for altitude in coordinates?
   *
   * @param shape
   * @return true on successfully adding GeometryCollection to MapLayer, false otherwise
   * @throws JSONException
   */
  private static boolean parseMultiPolygon(JSONObject shape) throws JSONException {
    // initialize to null or new MapPolygon?
    MapPolygon poly = null;
    JSONArray coordinates = shape.getJSONArray("coordinates");

    for (int k = 0; k < coordinates.length(); k++) {
      poly = new MapPolygon();
      JSONArray jsonRings = coordinates.getJSONArray(k);
      ArrayList<Geopath> rings = new ArrayList<>();

      for (int i = 0; i < jsonRings.length(); i++) {
        JSONArray path = jsonRings.getJSONArray(i);
        ArrayList<Geoposition> points = new ArrayList<>();

        for (int j = 0; j < path.length(); j++) {
          double longitude = path.getJSONArray(j).getDouble(0);
          double latitude = path.getJSONArray(j).getDouble(1);
          points.add(new Geoposition(latitude, longitude));
        }
        rings.add(new Geopath(points));
      }
      poly.setPaths(rings);
      mLayer.getElements().add(poly);
    }

    return true;
  }

  /**
   * TODO: error handling, returning false upon parsing failure
   * TODO: throw GeoJSONException
   * TODO: Allow for altitude in coordinates?
   *
   * @param shape
   * @return true on successfully adding GeometryCollection to MapLayer, false otherwise
   * @throws JSONException
   */
  private static boolean parsePoint(JSONObject shape) throws JSONException {
    // should this be initialized to new MapIcon?
    MapIcon pushpin;
    JSONArray coordinates = shape.getJSONArray("coordinates");
    double longitude = coordinates.getDouble(0);
    double latitude = coordinates.getDouble(1);

    Geopoint point = new Geopoint(new Geoposition(latitude, longitude));
    pushpin = new MapIcon();
    pushpin.setLocation(point);
    mLayer.getElements().add(pushpin);

    return true;
  }

  /**
   * TODO: error handling, returning false upon parsing failure
   * TODO: throw GeoJSONException
   * TODO: Allow for altitude in coordinates?
   *
   * @param shape
   * @return true on successfully adding GeometryCollection to MapLayer, false otherwise
   * @throws JSONException
   */
  private static boolean parseMultiPoint(JSONObject shape) throws JSONException {
    // should this be initialized to new MapIcon?
    MapIcon pushpin;
    JSONArray coordinates = shape.getJSONArray("coordinates");

    for (int i = 0; i < coordinates.length(); i++) {
      double longitude = coordinates.getJSONArray(i).getDouble(0);
      double latitude = coordinates.getJSONArray(i).getDouble(1);

      Geopoint point = new Geopoint(new Geoposition(latitude, longitude));
      pushpin = new MapIcon();
      pushpin.setLocation(point);
      mLayer.getElements().add(pushpin);
    }

    return true;
  }

  /**
   * TODO: error handling, returning false upon parsing failure
   * TODO: throw GeoJSONException
   * TODO: Allow for altitude in coordinates?
   *
   * @param shape
   * @return true on successfully adding GeometryCollection to MapLayer, false otherwise
   * @throws JSONException
   */
  private static boolean parseLineString(JSONObject shape) throws JSONException {
    // should this be null?
    MapPolyline line = new MapPolyline();
    JSONArray coordinates = shape.getJSONArray("coordinates");
    ArrayList<Geoposition> path = new ArrayList<>();

    for (int i = 0; i < coordinates.length(); i++) {
      double longitude = coordinates.getJSONArray(i).getDouble(0);
      double latitude = coordinates.getJSONArray(i).getDouble(1);
      Geoposition position = new Geoposition(latitude, longitude);
      path.add(position);
    }

    line.setPath(new Geopath(path));
    mLayer.getElements().add(line);

    return true;
  }

  /**
   * TODO: error handling, returning false upon parsing failure
   * TODO: throw GeoJSONException
   * TODO: Allow for altitude in coordinates?
   *
   * @param shape
   * @return true on successfully adding GeometryCollection to MapLayer, false otherwise
   * @throws JSONException
   */
  private static boolean parseMultiLineString(JSONObject shape) throws JSONException {
    // initialize to new MapPolyline?
    MapPolyline line;
    JSONArray coordinates = shape.getJSONArray("coordinates");
    ArrayList<Geoposition> path;

    for (int i = 0; i < coordinates.length(); i++) {
      JSONArray pathArray = coordinates.getJSONArray(i);
      line = new MapPolyline();
      path = new ArrayList<>();

      for (int j = 0; j < pathArray.length(); j++) {
        double longitude = pathArray.getJSONArray(j).getDouble(0);
        double latitude = pathArray.getJSONArray(j).getDouble(1);
        Geoposition position = new Geoposition(latitude, longitude);
        path.add(position);
      }

      line.setPath(new Geopath(path));
      mLayer.getElements().add(line);
    }

    return true;
  }
}
