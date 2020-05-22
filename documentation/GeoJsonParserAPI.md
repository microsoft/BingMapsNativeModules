

# GeoJsonParser API

This API will be used to read [GeoJSON](https://geojson.org/) data and display these shapes on a map. The API will parse the GeoJSON data and return a MapGeoJsonLayer.

```Java
public class GeoJsonParser
```
## Method

### Parse

This method takes in a GeoJSON formatted String and creates a MapGeoJsonLayer from it. The String is parsed for polygons, polylines, and points. All shapes defined in the GeoJSON String are added to a single layer. Throws an IOException for an invalid GeoJSON input string.

```Java
static MapGeoJsonLayer parse(String geojson) throws IOException
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

