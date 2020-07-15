

# MapGeoJsonLayer API

Contains shapes to display on the map, as defined in a GeoJson string. Optionally, the overall style of the shapes in the layer can be set programmatically (the same style will be applied to all applicable shapes). _Note: the style-updating methods for the MapGeoJsonLayer only apply to existing elements in the layer. 

**Android**

>```Java
> class MapGeoJsonLayer extends MapElementLayer
>```

**iOS**

>```objectivec
> @interface MSMapGeoJsonLayer : MSMapElementLayer
> ```

## Methods

### RemoveIcons

Removes all icons from the layer and returns them in a list of MapElements.

**Java**

>```Java
> List<MapElement> removeIcons()
>```

**iOS**

>```objectivec
> - (NSArray<MSMapElement *> *)removeIcons
> ```

### RemovePolylines

Removes all polylines from the layer and returns them in a list of MapElements.

**Java**

>```Java
> List<MapElement> removePolylines()
>```

**iOS**

>```objectivec
> - (NSArray<MSMapElement *> *)removePolylines
> ```

### RemovePolygons

Removes all polygons from the layer and returns them in a list of MapElements.

**Java**

>```Java
> List<MapElement> removePolygons()
>```

**iOS**

>```objectivec
> - (NSArray<MSMapElement *> *)removePolygons
> ```

### SetFillColor

The color in ARGB format to use to fill in [polygons](https://docs.microsoft.com/en-us/bingmaps/sdk-native/map-control-api/mappolygon-class). The default color is blue (0xff0000ff).

**Java**

>```Java
> void setFillColor(int fillColor)
>```

**iOS**

>```objectivec
> - (void)setFillColor:(UIColor *)fillColor
> ```

### SetIconsVisible

Whether the icons are rendered or not.

**Java**

>```Java
> void setIconsVisible(boolean visible)
> ```

**iOS**

>```objectivec
> - (void)setIconsVisible:(BOOL)iconsVisible
> ```

### SetPolygonsVisible

Whether the polygons are rendered or not.

**Java**

>```Java
> void setPolygonsVisible(boolean visible)
>```

**iOS**

>```objectivec
> - (void)setPolygonsVisible:(BOOL)polygonsVisible
> ```

### SetPolylinesVisible

Whether the [polylines](https://docs.microsoft.com/en-us/bingmaps/sdk-native/map-control-api/mappolyline-class) are rendered or not.

**Java**

>```Java
> void setPolylinesVisible(boolean visible)
>```

**iOS**

>```objectivec
> - (void)setPolylinesVisible:(BOOL)polylinesVisible
> ```

### SetStrokeColor
The color in ARGB format to use to draw polylines and the border of polygons. The default color is blue (0xff0000ff).

**Java**

>```Java
> void setStrokeColor(int strokeColor)
>```

**iOS**

>```objectivec
> - (void)setStrokeColor:(UIColor *)strokeColor
> ```

### SetStrokeDashed

Indicates whether the lines are dashed in polylines and polygons.

**Java**

>```Java
> void setStrokeDashed(boolean isDashed)
>```

**iOS**

>```objectivec
> - (void)setStrokeDashed:(BOOL)strokeDashed
> ```

### SetStrokeWidth

The width of the line to use for polylines and the outside of the polygon. The default width is 1.

**Java**

>```Java
> void setStrokeWidth(float strokeWidth)
>```

**iOS**

>```objectivec
> - (void)setStrokeWidth:(int)strokeWidth
> ```

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

**Java**

>```Java
> // MapView map = ...
> MapGeoJsonLayer layer = GeoJsonParser.parse(geojson);
> map.getLayers().add(layer);
>```


Result: 
![Default styling](https://github.com/microsoft/BingMapsNativeModules/blob/master/documentation/defaultStyle.png?raw=true)

Or to change the style before adding to the map:

**Java**

>```Java
> // MapView map = ...
> MapGeoJsonLayer layer = GeoJsonParser.parse(geojson);
> layer.setFillColor(Color.RED);
> layer.setStrokeColor(Color.GREEN);
> layer.setStrokeWidth(3);
> map.getLayers().add(layer);
>```

Result:
![New styling](https://github.com/microsoft/BingMapsNativeModules/blob/master/documentation/withStyle.png?raw=true)


Filter out points:

**Java**

>```Java
> // MapView map = ...
> MapGeoJsonLayer layer = GeoJsonParser.parse(geojson);
> layer.setPointsVisible(false);
> map.getLayers().add(layer);
>```

Result:

![Filter points](https://github.com/microsoft/BingMapsNativeModules/blob/master/documentation/filterPoints.png?raw=true)


## See Also
- [MapElementLayer](https://docs.microsoft.com/en-us/bingmaps/sdk-native/map-control-api/mapelementlayer-class)
- [MapLayer](https://docs.microsoft.com/en-us/bingmaps/sdk-native/map-control-api/maplayer-class)
- [MapElementCollection](https://docs.microsoft.com/en-us/bingmaps/sdk-native/map-control-api/mapelementcollection-class)

