package com.microsoft.maps;

import org.json.JSONException;

public class GeoJsonParseException extends Exception {
    public GeoJsonParseException(String errorMessage) {
        super(errorMessage);
    }
}
