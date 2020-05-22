
# MapGeoJsonLayer API

Contains shapes to display on the map, as defined in a GeoJson string. Optionally, the overall style of the shapes in the layer can be set programmatically (the same style will be applied to all applicable shapes).

```Java
class MapGeoJsonLayer extends MapElementLayer
```

## Properties

### FillColor

The color in ARGB format to use to fill in [polygons](https://docs.microsoft.com/en-us/bingmaps/sdk-native/map-control-api/mappolygon-class). The default color is blue (0xff0000ff).

```Java
int getFillColor()
void setFillColor(int fillColor)
```

### PointsVisible

Whether the points are rendered or not.

```Java
boolean getPointsVisible()
void setPointsVisible(boolean visible)
```

### PolygonsVisible

Whether the polygons are rendered or not.

```Java
boolean getPolygonsVisible()
void setPolygonsVisible(boolean visible)
```

### PolylinesVisible

Whether the [polylines](https://docs.microsoft.com/en-us/bingmaps/sdk-native/map-control-api/mappolyline-class) are rendered or not.

```Java
boolean getPolylinesVisible()
void setPolylinesVisible(boolean visible)
```

### StrokeColor
The color in ARGB format to use to draw polylines and the border of polygons. The default color is blue (0xff0000ff).

```Java
int getStrokeColor()
void setStrokeColor(int strokeColor)
```

### StrokeDashed

Indicates whether the lines are dashed in polylines and polygons.

```Java
boolean getStrokeDashed()
void setStrokeDashed(boolean isDashed)
```

### StrokeWidth

The width of the line to use for polylines and the outside of the polygon. The default width is 1.

```Java
float getStrokeWidth()
void setStrokeWidth(float strokeWidth)
```


## Examples

Parse the following GeoJSON string (called `geojson`) and add to map:
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
MapGeoJsonLayer layer = GeoJsonParser.parse(geojson);
map.getLayers().add(layer);
```


Result: 
![Default styling](https://github.com/microsoft/BingMapsNativeModules/blob/t-elbart/APIspecs/documentation/defaultStyle.png?raw=true)

Or to change the style before adding to the map:

```Java
// MapView map = ...
MapGeoJsonLayer layer = GeoJsonParser.parse(geojson);
layer.setFillColor(Color.RED);
layer.setStrokeColor(Color.GREEN);
layer.setStrokeWidth(3);
layer.setStrokeDashed(true);
map.getLayers().add(layer);
```

Result:
![New styling](https://github.com/microsoft/BingMapsNativeModules/blob/t-elbart/APIspecs/documentation/withStyle.png?raw=true)


Filter out points:

```Java
// MapView map = ...
MapGeoJsonLayer layer = GeoJsonParser.parse(geojson);
layer.setPointsVisible(false);
map.getLayers().add(layer);
```

Result:

![Filter points](https://github.com/microsoft/BingMapsNativeModules/blob/t-elbart/APIspecs/documentation/filterPoints.png?raw=true)


## See Also
- [MapElementLayer](https://docs.microsoft.com/en-us/bingmaps/sdk-native/map-control-api/mapelementlayer-class)
- [MapLayer](https://docs.microsoft.com/en-us/bingmaps/sdk-native/map-control-api/maplayer-class)
- [MapElementCollection](https://docs.microsoft.com/en-us/bingmaps/sdk-native/map-control-api/mapelementcollection-class)

