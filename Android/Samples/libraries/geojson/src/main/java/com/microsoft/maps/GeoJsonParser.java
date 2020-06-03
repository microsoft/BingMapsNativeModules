package com.microsoft.maps;

import androidx.annotation.VisibleForTesting;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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

    private static final MapFactories DEFAULT_MAP_FACTORIES = new MapFactories() {
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
    GeoJsonParser(){

    }

    /**
     * TODO: return MapGeoJsonLayer
     *
     * @param geojson String of GeoJSON to parse
     * @return MapElementLayer containing all objects
     * @throws GeoJsonParseException
     */
    public static MapElementLayer parse(String geojson) throws GeoJsonParseException {
        GeoJsonParser instance = new GeoJsonParser();
        return instance.internalParse(geojson, DEFAULT_MAP_FACTORIES);
    }

    @VisibleForTesting
    MapElementLayer internalParse(String geojson, MapFactories factory) throws GeoJsonParseException {
        mLayer = factory.createMapElementLayer();

        JSONObject object;
        String type;
        try {
            object = new JSONObject(geojson);
            type = object.getString("type");
        } catch (JSONException e) {
            throw new GeoJsonParseException(e.getMessage());
        }

        if (type.equals("FeatureCollection")) {
            parseFeatureCollection(object, factory);
        }
        else {
            if (type.equals("Feature")) {
                try {
                    object = object.getJSONObject("geometry");
                } catch (JSONException e) {
                    throw new GeoJsonParseException(e.getMessage());
                }
            }
            switchToType(object, factory);
        }
        return mLayer;
    }

    private void switchToType(JSONObject object, MapFactories factory) throws GeoJsonParseException {
        String type;
        try {
            type = object.getString("type");
        } catch (JSONException e) {
            throw new GeoJsonParseException(e.getMessage());
        }
        switch (type) {
            case "Polygon":
                parsePolygon(getCoordinates(object), factory);
                break;
            case "Point":
                parsePoint(getCoordinates(object), factory);
                break;
            case "MultiPoint":
                parseMultiPoint(object, factory);
                break;
            case "LineString":
                parseLineString(getCoordinates(object), factory);
                break;
            case "MultiLineString":
                parseMultiLineString(object, factory);
                break;
            case "MultiPolygon":
                parseMultiPolygon(object, factory);
                break;
            case "GeometryCollection":
                parseGeometryCollection(object, factory);
                break;
        }
    }

    private JSONArray getCoordinates(JSONObject object) throws GeoJsonParseException{
        try {
            return object.getJSONArray("coordinates");
        } catch (JSONException e) {
            throw new GeoJsonParseException(e.getMessage());
        }
    }

    private void parseGeometryCollection(JSONObject object, MapFactories factory) throws GeoJsonParseException {
        JSONArray array;
        try {
            array = object.getJSONArray("geometries");
        } catch (JSONException e) {
            throw new GeoJsonParseException(e.getMessage());
        }

        for (int i = 0; i < array.length(); i++) {
            JSONObject shape;
            try {
                shape = array.getJSONObject(i);
            } catch (JSONException e) {
                throw new GeoJsonParseException(e.getMessage());
            }
            switchToType(shape, factory);
        }
    }

    private void parseFeatureCollection(JSONObject object, MapFactories factory) throws GeoJsonParseException {
        JSONArray array;
        try {
            array = object.getJSONArray("features");
        } catch (JSONException e) {
            throw new GeoJsonParseException(e.getMessage());
        }

        for (int i = 0; i < array.length(); i++) {
            JSONObject shape = null;
            try {
                shape = array.getJSONObject(i).getJSONObject("geometry");
            } catch (JSONException e) {
                throw new GeoJsonParseException(e.getMessage());
            }
            switchToType(shape, factory);
        }
    }

    private void parsePolygon(JSONArray jsonRings, MapFactories factory) throws GeoJsonParseException {
        ArrayList<Geopath> rings = new ArrayList<>(jsonRings.length());
        for (int i = 0; i < jsonRings.length(); i++) {
            JSONArray pathArray;
            try{
                pathArray = jsonRings.getJSONArray(i);
            } catch (JSONException e) {
                throw new GeoJsonParseException(e.getMessage());
            }
            rings.add(parsePath(pathArray));
        }
        MapPolygon poly = factory.createMapPolygon();
        if (rings != null){
            poly.setPaths(rings);
            mLayer.getElements().add(poly);
        }
    }

    private void parseMultiPolygon(JSONObject shape, MapFactories factory) throws GeoJsonParseException {
        JSONArray coordinates;
        try {
            coordinates = shape.getJSONArray("coordinates");
        } catch (JSONException e) {
            throw new GeoJsonParseException(e.getMessage());
        }

        for (int k = 0; k < coordinates.length(); k++) {
            JSONArray jsonRings;
            try {
                jsonRings = coordinates.getJSONArray(k);
            } catch (JSONException e) {
                throw new GeoJsonParseException(e.getMessage());
            }
            parsePolygon(jsonRings, factory);
        }
    }

    private void parsePoint(JSONArray coordinates, MapFactories factory) throws GeoJsonParseException {
        Geoposition position = parseGeoposition(coordinates);
        if(position != null){
            Geopoint point = new Geopoint(position);
            MapIcon pushpin = factory.createMapIcon();
            pushpin.setLocation(point);
            mLayer.getElements().add(pushpin);
        }
    }

    private void parseMultiPoint(JSONObject shape, MapFactories factory) throws GeoJsonParseException {
        JSONArray coordinates;
        try {
            coordinates = shape.getJSONArray("coordinates");
        } catch (JSONException e) {
            throw new GeoJsonParseException(e.getMessage());
        }

        for (int i = 0; i < coordinates.length(); i++) {
            JSONArray latLong ;
            try {
                latLong = coordinates.getJSONArray(i);
            } catch (JSONException e) {
                throw new GeoJsonParseException(e.getMessage());
            }
            parsePoint(latLong, factory);
        }
    }

    private void parseLineString(JSONArray pathArray, MapFactories factory) throws GeoJsonParseException {
        MapPolyline line = factory.createMapPolyline();
        line.setPath(parsePath(pathArray));
        mLayer.getElements().add(line);
    }

    private void parseMultiLineString(JSONObject shape, MapFactories factory) throws GeoJsonParseException {
        JSONArray coordinates;
        try {
            coordinates = shape.getJSONArray("coordinates");
        } catch (JSONException e) {
            throw new GeoJsonParseException(e.getMessage());
        }

        for (int i = 0; i < coordinates.length(); i++) {
            JSONArray pathArray;
            try{
                pathArray = coordinates.getJSONArray(i);
            } catch (JSONException e){
                throw new GeoJsonParseException(e.getMessage());
            }

            parseLineString(pathArray, factory);
        }
    }

    private Geoposition parseGeoposition(JSONArray coordinates) throws GeoJsonParseException {
        if (coordinates.length() >= 2){
            double longitude, latitude;
            try{
                longitude = coordinates.getDouble(0);
                latitude = coordinates.getDouble(1);
            } catch (JSONException e){
                throw new GeoJsonParseException(e.getMessage());
            }

            if (coordinates.length() > 2){
                double altitude;
                try {
                    altitude = coordinates.getDouble(2);
                } catch (JSONException e) {
                    throw new GeoJsonParseException(e.getMessage());
                }
                return new Geoposition(latitude, longitude, altitude);
            }
            return new Geoposition(latitude, longitude);
        }
        else{
            return null;
        }
    }

    private Geopath parsePath(JSONArray pathArray) throws GeoJsonParseException{
        ArrayList<Geoposition> path = new ArrayList<>(pathArray.length());
        for (int j = 0; j < pathArray.length(); j++) {
            JSONArray latLong;
            try{
                latLong = pathArray.getJSONArray(j);
            } catch (JSONException e) {
                throw new GeoJsonParseException(e.getMessage());
            }

            Geoposition position = parseGeoposition(latLong);
            if(position != null){
                path.add(position);
            }
        }
        return new Geopath(path, AltitudeReferenceSystem.ELLIPSOID);
    }

}