//
//  MSMapGeoJsonParser.m
//  MSMapsModules
//
//  Created by Elizabeth Bartusiak (t-elbart) on 6/12/20.
//  Copyright © 2020 Microsoft Corporation.
//  Licensed under the MIT license.
//

#import "MSMapGeoJsonParser.h"
#import <MicrosoftMaps/MicrosoftMaps.h>

NS_ASSUME_NONNULL_BEGIN

@implementation MSMapGeoJsonParser {
  MSMapGeoJsonLayer *mLayer;
  bool mDidWarn;
};

- (MSMapGeoJsonParser *)init {
  if (self = [super init]) {
    mLayer = [[MSMapGeoJsonLayer alloc] init];
    mDidWarn = false;
  }
  return self;
}

+ (MSMapGeoJsonLayer * _Nullable)parse:(NSString *)geojson
                                error:(NSError * _Nullable * _Nullable)error {
  if (geojson == nil || geojson.length == 0) {
    if (error != nil) {
      *error = [NSError
          errorWithDomain:
              @"com.microsoft.modules.MSMapsModules.InvalidArgumentError"
                     code:-100
                 userInfo:@{
                   NSLocalizedDescriptionKey : @"Input String cannot be null."
                 }];
    }
    return nil;
  }

  MSMapGeoJsonParser *instance = [[MSMapGeoJsonParser alloc] init];
  NSError *localError;
  MSMapGeoJsonLayer *layer = [instance internalParse:geojson error:&localError];
  if (localError != nil) {
    if (error != nil) {
      *error = localError;
    }
    return nil;
  }
  return layer;
}

- (MSMapGeoJsonLayer * _Nullable)internalParse:(NSString *)geojson
                                        error:(NSError **)error {
  NSData *jsonData = [geojson dataUsingEncoding:NSUTF8StringEncoding];
  NSDictionary *jsonObject = [NSJSONSerialization JSONObjectWithData:jsonData
                                                             options:kNilOptions
                                                               error:error];

  if (*error != nil) {
    return nil;
  }

  NSObject *typeObject = [jsonObject objectForKey:@"type"];
  if (typeObject == nil) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:@"GeoJson geomtry object must have \"type\"."];
    return nil;
  }

  NSString *type = [NSString stringWithFormat:@"%@", typeObject];
  if ([type isEqualToString:@"FeatureCollection"]) {
    [self parseFeatureCollection:jsonObject error:error];
  } else {
    if ([type isEqualToString:@"Feature"]) {
      NSArray<NSString *> *members =
          [[NSArray alloc] initWithObjects:@"features", nil];
      [MSMapGeoJsonParser verifyNoMembers:jsonObject
                                  members:members
                                    error:error];
      if (*error != nil) {
        return nil;
      }
      jsonObject = [jsonObject objectForKey:@"geometry"];
      if (jsonObject == nil) {
        *error = [MSMapGeoJsonParser
            makeGeoJsonError:@"Feature object must have \"geometry\"."];
        return nil;
      }
      if (![jsonObject isKindOfClass:[NSDictionary class]]) {
        *error = [MSMapGeoJsonParser
            makeGeoJsonError:
                [NSString
                    stringWithFormat:@"Feature object \"geometry\" must be a "
                                     @"GeoJSON object. Instead saw: %@",
                                     jsonObject]];
        return nil;
      }
    }
    [self switchToType:jsonObject error:error];
  }

  return mLayer;
}

- (void)switchToType:(NSDictionary *)object error:(NSError **)error {
  NSString *type =
      [NSString stringWithFormat:@"%@", [object objectForKey:@"type"]];
  if (type == nil) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:@"GeoJSON object must be contain \"type\"."];
    return;
  }
  if ([type isEqualToString:@"Point"]) {
    NSArray *coordinates = [MSMapGeoJsonParser getCoordinates:object
                                                        error:error];
    if (*error != nil) {
      return;
    }
    MSGeoposition *position = [MSMapGeoJsonParser parseGeoposition:coordinates
                                                             error:error];
    if (*error != nil) {
      return;
    }
    MSMapAltitudeReferenceSystem altitudeReferenceSystem =
        (coordinates.count > 2) ? MSMapAltitudeReferenceSystemEllipsoid
                                : MSMapAltitudeReferenceSystemSurface;
    [self createIconAndAddToLayer:position
          altitudeReferenceSystem:altitudeReferenceSystem];
  } else if ([type isEqualToString:@"MultiPoint"]) {
    NSArray *coordinates = [MSMapGeoJsonParser getCoordinates:object
                                                        error:error];
    if (*error != nil) {
      return;
    }
    [self parseMultiPoint:coordinates error:error];
  } else if ([type isEqualToString:@"LineString"]) {
    NSArray *pathArray = [MSMapGeoJsonParser getCoordinates:object error:error];
    if (*error != nil) {
      return;
    }
    [self parseLineString:pathArray error:error];
  } else if ([type isEqualToString:@"MultiLineString"]) {
    NSArray *pathArray = [MSMapGeoJsonParser getCoordinates:object error:error];
    if (*error != nil) {
      return;
    }
    [self parseMultiLineString:pathArray error:error];
  } else if ([type isEqualToString:@"Polygon"]) {
    NSArray *rings = [MSMapGeoJsonParser getCoordinates:object error:error];
    if (*error != nil) {
      return;
    }
    [self parsePolygon:rings error:error];
  } else if ([type isEqualToString:@"MultiPolygon"]) {
    NSArray *polygons = [MSMapGeoJsonParser getCoordinates:object error:error];
    if (*error != nil) {
      return;
    }
    [self parseMultiPolygon:polygons error:error];
  } else if ([type isEqualToString:@"GeometryCollection"]) {
    [self parseGeometryCollection:object error:error];
  } else {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:
            [NSString
                stringWithFormat:@"%@ is not a valid Geometry type.", type]];
    return;
  }
  NSArray<NSString *> *members = [[NSArray alloc]
      initWithObjects:@"geometry", @"properties", @"features", nil];
  [MSMapGeoJsonParser verifyNoMembers:object members:members error:error];
}

+ (NSArray * _Nullable)getCoordinates:(NSDictionary *)object
                               error:(NSError **)error {
  NSObject *obj = [object objectForKey:@"coordinates"];
  if (obj == nil) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:
            @"Object must contain an array for \"coordinates\" value."];
    return nil;
  }
  if (![obj isKindOfClass:[NSArray class]]) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:[@"Object must contain an array for \"coordinates\" "
                          @"value. Instead saw: "
                             stringByAppendingFormat:@"%@", obj]];
  }
  return (NSArray *)obj;
}

- (void)createIconAndAddToLayer:(MSGeoposition *)position
        altitudeReferenceSystem:(MSMapAltitudeReferenceSystem)system {
  MSGeopoint *point = [[MSGeopoint alloc] initWithPosition:position
                                   altitudeReferenceSystem:system];
  MSMapIcon *icon = [[MSMapIcon alloc] init];
  icon.location = point;
  [mLayer.elements addMapElement:icon];
}

- (void)createLineAndAddToLayer:(NSArray<MSGeoposition *> *)positions
        altitudeReferenceSystem:(MSMapAltitudeReferenceSystem)system {
  MSMapPolyline *line = [[MSMapPolyline alloc] init];
  line.path = [[MSGeopath alloc] initWithPositions:positions
                           altitudeReferenceSystem:system];
  [mLayer.elements addMapElement:line];
}

- (void)createPolygonAndAddToLayer:(NSArray *)positionLists
           altitudeReferenceSystem:
               (MSMapAltitudeReferenceSystem)altitudeReferenceSystem {
  NSMutableArray<MSGeopath *> *rings = [[NSMutableArray alloc] init];
  for (NSArray<MSGeoposition *> *ring in positionLists) {
    MSGeopath *path =
        [[MSGeopath alloc] initWithPositions:ring
                     altitudeReferenceSystem:altitudeReferenceSystem];
    [rings addObject:path];
  }
  MSMapPolygon *poly = [[MSMapPolygon alloc] init];
  poly.paths = rings;
  [mLayer.elements addMapElement:poly];
}

- (void)parseMultiPoint:(NSArray *)coordinates error:(NSError **)error {
  MSMapAltitudeReferenceSystem altitudeReferenceSystem =
      MSMapAltitudeReferenceSystemEllipsoid;
  NSArray<MSGeoposition *> *positions =
      [self parsePositionArray:coordinates
          altitudeReferenceSystem:&altitudeReferenceSystem
                            error:error];
  if (*error != nil) {
    return;
  }
  [MSMapGeoJsonParser setAltitudesToZeroIfAtSurface:positions
                            altitudeReferenceSystem:altitudeReferenceSystem];
  for (int i = 0; i < positions.count; i++) {
    [self createIconAndAddToLayer:positions[i]
          altitudeReferenceSystem:altitudeReferenceSystem];
  }
}

- (NSArray<MSGeoposition *> * _Nullable)parseLineArray:(NSArray *)pathArray
                              altitudeReferenceSystem:
                                  (MSMapAltitudeReferenceSystem *)system
                                                error:(NSError **)error {
  if (pathArray.count < 2) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:[@"Line must contain at least two positions. "
                          @"Instead saw: "
                             stringByAppendingString:
                                 [MSMapGeoJsonParser arrayToString:pathArray]]];
    return nil;
  }
  return [self parsePositionArray:pathArray
          altitudeReferenceSystem:system
                            error:error];
}

- (void)parseLineString:(NSArray *)pathArray error:(NSError **)error {
  MSMapAltitudeReferenceSystem altitudeReferenceSystem =
      MSMapAltitudeReferenceSystemEllipsoid;
  NSArray<MSGeoposition *> *validatedArray =
      [self parseLineArray:pathArray
          altitudeReferenceSystem:&altitudeReferenceSystem
                            error:error];
  if (*error != nil) {
    return;
  }
  [MSMapGeoJsonParser setAltitudesToZeroIfAtSurface:validatedArray
                            altitudeReferenceSystem:altitudeReferenceSystem];
  [self createLineAndAddToLayer:validatedArray
        altitudeReferenceSystem:altitudeReferenceSystem];
}

- (void)parseMultiLineString:(NSArray *)coordinates error:(NSError **)error {
  NSMutableArray *lines = [[NSMutableArray alloc] init];
  MSMapAltitudeReferenceSystem altitudeReferenceSystem =
      MSMapAltitudeReferenceSystemEllipsoid;
  for (id obj in coordinates) {
    if (![obj isKindOfClass:[NSArray class]]) {
      *error = [MSMapGeoJsonParser
          makeGeoJsonError:[@"MultiLineString coordinates must be an array of "
                            @"line arrays. Instead saw: "
                               stringByAppendingFormat:@"%@", obj]];
      return;
    }
    NSArray<MSGeoposition *> *parsedLine =
        [self parseLineArray:(NSArray *)obj
            altitudeReferenceSystem:&altitudeReferenceSystem
                              error:error];
    if (*error != nil) {
      return;
    }
    [lines addObject:parsedLine];
  }
  for (NSMutableArray<MSGeoposition *> *line in lines) {
    [MSMapGeoJsonParser setAltitudesToZeroIfAtSurface:line
                              altitudeReferenceSystem:altitudeReferenceSystem];
    [self createLineAndAddToLayer:line
          altitudeReferenceSystem:altitudeReferenceSystem];
  }
}

- (NSArray * _Nullable)parsePolygonRings:(NSArray *)jsonRings
                altitudeReferenceSystem:
                    (MSMapAltitudeReferenceSystem *)altitudeReferenceSystem
                                  error:(NSError **)error {
  if (jsonRings.count == 0) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:[@"coordinates value must contain "
                          @"at least one Polygon ring. Instead saw: "
                             stringByAppendingString:
                                 [MSMapGeoJsonParser arrayToString:jsonRings]]];
    return nil;
  }
  NSMutableArray *parsedRings = [[NSMutableArray alloc] init];
  for (id obj in jsonRings) {
    if (![obj isKindOfClass:[NSArray class]]) {
      *error = [MSMapGeoJsonParser
          makeGeoJsonError:
              [@"Polygon rings must be an array of positions. Instead saw: "
                  stringByAppendingFormat:@"%@", obj]];
      return nil;
    }
    NSArray<MSGeoposition *> *parsedArray =
        [self parsePositionArray:(NSArray *)obj
            altitudeReferenceSystem:altitudeReferenceSystem
                              error:error];
    if (*error != nil) {
      return nil;
    }
    [MSMapGeoJsonParser verifyPolygonRing:parsedArray error:error];
    if (*error != nil) {
      return nil;
    }
    [parsedRings addObject:parsedArray];
  }
  return parsedRings;
}

- (void)parsePolygon:(NSArray *)jsonRings error:(NSError **)error {
  MSMapAltitudeReferenceSystem altitudeReferenceSystem =
      MSMapAltitudeReferenceSystemEllipsoid;
  NSArray *rings = [self parsePolygonRings:jsonRings
                   altitudeReferenceSystem:&altitudeReferenceSystem
                                     error:error];
  if (*error != nil) {
    return;
  }
  for (NSArray<MSGeoposition *> *positions in rings) {
    [MSMapGeoJsonParser setAltitudesToZeroIfAtSurface:positions
                              altitudeReferenceSystem:altitudeReferenceSystem];
  }
  [self createPolygonAndAddToLayer:rings
           altitudeReferenceSystem:altitudeReferenceSystem];
}

- (void)parseMultiPolygon:(NSArray *)jsonArray error:(NSError **)error {
  NSMutableArray *polygons = [[NSMutableArray alloc] init];
  MSMapAltitudeReferenceSystem altitudeReferenceSystem =
      MSMapAltitudeReferenceSystemEllipsoid;
  for (id obj in jsonArray) {
    if (![obj isKindOfClass:[NSArray class]]) {
      *error = [MSMapGeoJsonParser
          makeGeoJsonError:[@"MultiPolygon coordinates must be an array of "
                            @"Polygons. Instead saw: "
                               stringByAppendingFormat:@"%@", obj]];
      return;
    }
    NSArray *polygon = [self parsePolygonRings:(NSArray *)obj
                       altitudeReferenceSystem:&altitudeReferenceSystem
                                         error:error];
    if (*error != nil) {
      return;
    }
    [polygons addObject:polygon];
  }
  for (NSArray *polygonRings in polygons) {
    for (NSArray<MSGeoposition *> *ring in polygonRings) {
      [MSMapGeoJsonParser
          setAltitudesToZeroIfAtSurface:ring
                altitudeReferenceSystem:altitudeReferenceSystem];
    }
    [self createPolygonAndAddToLayer:polygonRings
             altitudeReferenceSystem:altitudeReferenceSystem];
  }
}

+ (void)verifyPolygonRing:(NSArray *)path error:(NSError **)error {
  if (path.count < 4) {
    NSString *array = [MSMapGeoJsonParser arrayToString:path];
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:
            [@"Polygon ring must have at least 4 positions, and the first and "
             @"last position must be the same. Instead saw: "
                stringByAppendingString:array]];
    return;
  }
  MSGeoposition *firstPosition = [path firstObject];
  MSGeoposition *lastPosition = [path lastObject];
  if (firstPosition.latitude != lastPosition.latitude ||
      firstPosition.longitude != lastPosition.longitude ||
      firstPosition.altitude != lastPosition.altitude) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:
            [NSString stringWithFormat:
                          @"First and last coordinate pair of each polygon "
                          @"ring must be the same. Instead saw Geopositions: "
                          @"first: [%f, %f, %f] last: [%f, %f, %f]",
                          firstPosition.latitude, firstPosition.longitude,
                          firstPosition.altitude, lastPosition.latitude,
                          lastPosition.longitude, lastPosition.altitude]];
  }
}

- (void)parseFeatureCollection:(NSDictionary *)object error:(NSError **)error {
  NSArray<NSString *> *members =
      [[NSArray alloc] initWithObjects:@"geometry", @"properties",
                                       @"coordinates", @"geometries", nil];
  [MSMapGeoJsonParser verifyNoMembers:object members:members error:error];
  if (*error != nil) {
    return;
  }
  NSArray *array = [object objectForKey:@"features"];
  if (array == nil) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:@"FeatureCollection must contain \"features\"."];
    return;
  }
  if (![array isKindOfClass:[NSArray class]]) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:
            [NSString
                stringWithFormat:@"\"features\" from \"FeatureCollection\" "
                                 @"must contain an array. Instead saw: %@",
                                 array]];
    return;
  }
  if (array.count == 0) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:@"\"features\" from \"FeatureCollection\" must "
                         @"contain an array with at least one Feature object."];
    return;
  }
  for (id obj in array) {
    if (![obj isKindOfClass:[NSDictionary class]]) {
      *error = [MSMapGeoJsonParser
          makeGeoJsonError:
              [NSString stringWithFormat:
                            @"\"features\" from \"FeatureCollection\" must be "
                            @"an array of geometry objects. Instead saw: %@",
                            obj]];
      return;
    }
    NSDictionary *element = (NSDictionary *)obj;
    NSString *type = [element objectForKey:@"type"];
    if (![type isEqualToString:@"Feature"]) {
      *error = [MSMapGeoJsonParser
          makeGeoJsonError:
              [NSString stringWithFormat:@"GeoJSON Features must have type "
                                         @"\"Feature\" instead saw: %@",
                                         type]];
      return;
    }
    NSArray<NSString *> *members =
        [[NSArray alloc] initWithObjects:@"features", nil];
    [MSMapGeoJsonParser verifyNoMembers:obj members:members error:error];
    NSDictionary *shape = [element objectForKey:@"geometry"];
    if (shape == nil) {
      *error = [MSMapGeoJsonParser
          makeGeoJsonError:@"Feature must contain \"geometry\"."];
      return;
    }
    if (![shape isKindOfClass:[NSDictionary class]]) {
      *error = [MSMapGeoJsonParser
          makeGeoJsonError:
              [NSString stringWithFormat:@"\"geometry\" of Feature must be a "
                                         @"GeoJSON object. Instead saw: %@",
                                         shape]];
      return;
    }
    [self switchToType:shape error:error];
    if (*error != nil) {
      return;
    }
  }
}

- (void)parseGeometryCollection:(NSDictionary *)object error:(NSError **)error {
  NSArray *array = [object objectForKey:@"geometries"];
  if (array == nil) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:@"GeometryCollection must contain \"geometries\"."];
    return;
  }
  if (![array isKindOfClass:[NSArray class]]) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:
            [NSString
                stringWithFormat:@"GeometryCollection \"geometries\" must be "
                                 @"an array of GeoJSON object. Instead saw: %@",
                                 array]];
    return;
  }
  for (id obj in array) {
    if (![obj isKindOfClass:[NSDictionary class]]) {
      *error = [MSMapGeoJsonParser
          makeGeoJsonError:
              [NSString
                  stringWithFormat:@"GeometryCollection must contain an array "
                                   @"of GeoJSON objects. Instead saw: %@",
                                   obj]];
      return;
    }
    [self switchToType:(NSDictionary *)obj error:error];
    if(*error != nil){
      return;
    }
  }
}

/*
 * altitudeReferenceSystem is set by caller to
 * MSMapAltitudeReferenceSystemEllipsoid, and if a point is found without a
 * specified alititude, the altitudeReferenceSystem is set to
 * MSMapAltitudeReferenceSystemSurface.
 */
- (NSArray<MSGeoposition *> * _Nullable)parsePositionArray:(NSArray *)array
                                  altitudeReferenceSystem:
                                      (MSMapAltitudeReferenceSystem *)system
                                                    error:(NSError **)error {
  if (array.count == 0) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:
            [@"Geopath must contain an array of positions. Instead saw: "
                stringByAppendingString:[MSMapGeoJsonParser
                                            arrayToString:array]]];
    return nil;
  }

  NSMutableArray<MSGeoposition *> *validatedArray =
      [[NSMutableArray alloc] init];
  for (id obj in array) {
    if (![obj isKindOfClass:[NSArray class]]) {
      *error = [MSMapGeoJsonParser
          makeGeoJsonError:[@"coordinates must contain an array of "
                            @"positions. Instead saw: "
                               stringByAppendingFormat:@"%@", (NSString *)obj]];
      return nil;
    }
    NSArray *latLong = (NSArray *)obj;
    if (latLong.count < 3) {
      *system = MSMapAltitudeReferenceSystemSurface;
      if (!mDidWarn) {
        NSLog(@"Warning: Unless all positions in a Geometry Object contain an "
              @"altitude coordinate, all altitudes will be set to 0 at surface "
              @"level for that Geometry Object.");
        mDidWarn = true;
      }
    }
    MSGeoposition *position = [MSMapGeoJsonParser parseGeoposition:latLong
                                                             error:error];
    if (*error != nil) {
      return nil;
    }
    [validatedArray addObject:position];
  }
  return validatedArray;
}

+ (MSGeoposition * _Nullable)parseGeoposition:(NSArray *)coordinates
                                       error:(NSError **)error {
  if (![coordinates isKindOfClass:[NSArray class]]) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:[@"position array must contain "
                          @"at least latitude and longitude. Instead saw: "
                             stringByAppendingFormat:@"%@", coordinates]];
    return nil;
  }
  if (coordinates.count >= 2) {
    if (![coordinates[0] isKindOfClass:[NSNumber class]]) {
      *error = [MSMapGeoJsonParser
          makeGeoJsonError:
              [@"Longitude must be a number. Instead saw array: "
                  stringByAppendingString:[MSMapGeoJsonParser
                                              arrayToString:coordinates]]];
      return nil;
    }

    double longitude = [coordinates[0] doubleValue];
    if (longitude < -180 || longitude > 180) {
      *error = [MSMapGeoJsonParser
          makeGeoJsonError:
              [@"Longitude must be in range [-180, 180]. Instead saw: "
                  stringByAppendingFormat:@"%f", longitude]];
    }

    if (![coordinates[1] isKindOfClass:[NSNumber class]]) {
      *error = [MSMapGeoJsonParser
          makeGeoJsonError:
              [@"Latitude must be a number. Instead saw array: "
                  stringByAppendingString:[MSMapGeoJsonParser
                                              arrayToString:coordinates]]];
      return nil;
    }

    double latitude = [coordinates[1] doubleValue];
    if (latitude < -90 || latitude > 90) {
      *error = [MSMapGeoJsonParser
          makeGeoJsonError:
              [@"Latitude must be in range [-90, 90]. Instead saw: "
                  stringByAppendingFormat:@"%f", latitude]];
      return nil;
    }

    double altitude = 0;

    if (coordinates.count > 2) {
      if (![coordinates[2] isKindOfClass:[NSNumber class]]) {
        *error = [MSMapGeoJsonParser
            makeGeoJsonError:
                [@"Altitude must be a number. Instead saw array: "
                    stringByAppendingString:[MSMapGeoJsonParser
                                                arrayToString:coordinates]]];
        return nil;
      }

      NSNumber *altitudeObj = coordinates[2];
      altitude = [altitudeObj doubleValue];
    }
    return [[MSGeoposition alloc] initWithLatitude:latitude
                                         longitude:longitude
                                          altitude:altitude];
  } else {

    *error = [MSMapGeoJsonParser
        makeGeoJsonError:
            [@"position array must contain "
             @"at least latitude and longitude. Instead saw: "
                stringByAppendingString:[MSMapGeoJsonParser
                                            arrayToString:coordinates]]];
    return nil;
  }
}

+ (void)setAltitudesToZeroIfAtSurface:(NSArray<MSGeoposition *> *)array
              altitudeReferenceSystem:
                  (MSMapAltitudeReferenceSystem)altitudeReferenceSystem {
  if (altitudeReferenceSystem == MSMapAltitudeReferenceSystemSurface) {
    for (MSGeoposition *position in array) {
      position.altitude = 0;
    }
  }
}

+ (void)verifyNoMembers:(NSDictionary *)object
                members:(NSArray<NSString *> *)members
                  error:(NSError **)error {
  for (NSString *str in members) {
    if ([object objectForKey:str] != nil) {
      NSString *objectType = [object objectForKey:@"type"];
      *error = [MSMapGeoJsonParser
          makeGeoJsonError:
              [NSString stringWithFormat:@"%@ cannot have a \"%@\" member.",
                                         objectType, str]];
      return;
    }
  }
}

+ (NSError *)makeGeoJsonError:(NSString *)message {
  return
      [NSError errorWithDomain:
                   @"com.microsoft.modules.MSMapsModules.MSMapGeoJsonParseError"
                          code:-500
                      userInfo:@{NSLocalizedDescriptionKey : message}];
}

+ (NSString *)arrayToString:(NSArray *)array {
  if (array.count == 0) {
    return @"[]";
  }
  NSString *string = @"[";
  for (int i = 0; i < array.count - 1; i++) {
    string = [string stringByAppendingFormat:@"%@, ", [array objectAtIndex:i]];
  }
  return [string
      stringByAppendingFormat:@"%@]", [array objectAtIndex:array.count - 1]];
}

@end

NS_ASSUME_NONNULL_END
