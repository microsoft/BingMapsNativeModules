# KMLParser API

This API will be used to read [KML](https://en.wikipedia.org/wiki/Keyhole_Markup_Language) data and display these shapes on a map. The API will parse the KML data and return a  [MapElementLayer](https://docs.microsoft.com/en-us/bingmaps/sdk-native/map-control-api/mapelementlayer-class).

**Android**

>```Java
> public class KMLParser
>```

**iOS**

>```objectivec
> @interface MSMapKMLParser : NSObject
> ```

## Method

### Parse

This method takes in a KML formatted String and creates a MapElementLayer from it. The String is parsed for polygons, polylines, and points. All shapes defined in the KML String are added to a single layer.

**Android**

>```Java
> public static MapElementLayer parse(String kml) throws KMLParseException
>```

**iOS**

>```objectivec
> + (MSMapElementLayer * _Nullable)parse:(NSString *)kml 
>                                  error:(NSError * _Nullable * _Nullable)error
> ```

## Examples

Parse the following kml string (called `kml`) and add to map:
```
<kml xmlns=“http://www.opengis.net/kml/2.2”>
<Document>
<Placemark>
    <name>city</name>
    <Point>
        <coordinates>-107.55,43,0</coordinates>
    </Point>
</Placemark>
<Placemark>
    <name>state</name>
    <Polygon>
      <outerBoundaryIs>
        <LinearRing>
          <coordinates>
            -104.05,41,0
            -104.05,45,0
            -111.05,45,0
            -111.05,041,0
            -104.05,41,0
          </coordinates>
        </LinearRing>
      </outerBoundaryIs>
    </Polygon>
</Placemark>
</Document>
</kml>
```

In the Activity's onCreate method:

```Java
// MapView map = ...
MapElementLayer layer = KMLParser.parse(kml);
map.getLayers().add(layer);
```


Result: 
![Default styling](https://github.com/microsoft/BingMapsNativeModules/blob/master/documentation/defaultStyle.png?raw=true)

