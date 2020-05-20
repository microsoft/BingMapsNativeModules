# GeoJsonLayer API

This API will be used to read [GeoJSON](https://geojson.org/) data and display these shapes on a map. The API will parse the GeoJSON data and automatically add the shapes to the [MapView](https://docs.microsoft.com/en-us/bingmaps/sdk-native/map-control-api/mapview-class) object. Optionally, the overall style of the shapes in the GeoJSON data can be set programmatically (the same style will be applied to all applicable shapes defined in the GeoJSON data).

## Constructor
```Java
GeoJsonLayer(MapView map)
```

## Methods
### AddToMap
This method takes in a GeoJSON formatted String and adds the data to the MapView.

```Java
void addToMap(String jString)
```

### FillColor

The color in ARGB format to use to fill in [polygons](https://docs.microsoft.com/en-us/bingmaps/sdk-native/map-control-api/mappolygon-class). The default value is blue (0xff0000ff).

```Java
int getFillColor()
void setFillColor(int fillColor)
```

### StrokeColor
The color in ARGB format to use to draw [polylines](https://docs.microsoft.com/en-us/bingmaps/sdk-native/map-control-api/mappolyline-class) and the border of [polygons](https://docs.microsoft.com/en-us/bingmaps/sdk-native/map-control-api/mappolygon-class). The default value is blue (0xff0000ff).

```Java
int getStrokeColor()
void setStrokeColor(int strokeColor)
```

### StrokeDashed

Indicates whether the lines are dashed in polylines and polygons.

```Java
boolean isStrokeDashed()
void setStrokeDashed(boolean isDashed)
```

### StrokeWidth

The width of the line to use for polylines and the outside of the polygon. The default value is 1.

```Java
float getStrokeWidth()
void setStrokeWidth(float strokeWidth)
```





## Examples

Parse the following GeoJSON string (called `jstr`) and add to map:
```
{
  "type": "GeometryCollection",
  "geometries": [{
    "type": "Polygon",
    "coordinates": [[
      [-104.05, 41],
      [-104.05, 45],
      [-111.05, 45],
      [-111.05, 41],
      [-104.05, 41]
    ]]},
    {
    "type": "Point",
    "coordinates": [-107.55, 43]
    }
  ]
}
```

In the Activity's onCreate method:
```Java
// MapView map = ...
GeoJasonLayer layer = new GeoJsonLayer(map);
layer.addToMap(jstr);
```
Or to change the style before adding to the map:

```Java
// MapView map = ...
GeoJasonLayer layer = new GeoJsonLayer(map);
layer.setFillColor(Color.RED);
layer.setStrokeColor(Color.GREEN);
layer.setStrokeWidth(3);
layer.setStrokeDashed(true);
layer.addToMap(jstr);
```


