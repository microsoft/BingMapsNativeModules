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
  MSMapElementLayer *mLayer;
};

+ (MSMapElementLayer * _Nullable)parse:(NSString *)geojson
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
  MSMapElementLayer *layer = [instance internalParse:geojson error:&localError];
  if (localError != nil) {
    if (error != nil) {
      *error = localError;
    }
    return nil;
  }
  return layer;
}

- (MSMapElementLayer * _Nullable)internalParse:(NSString *)geojson
                                        error:(NSError **)error {
  mLayer = [[MSMapElementLayer alloc] init];
  NSData *jsonData = [geojson dataUsingEncoding:NSUTF8StringEncoding];
  NSError *jsonError;
  NSDictionary *jsonObject =
      [NSJSONSerialization JSONObjectWithData:jsonData
                                      options:kNilOptions
                                        error:&jsonError];

  if (jsonObject == nil) {
    *error = jsonError;
    return nil;
  }

  NSObject *typeObject = [jsonObject objectForKey:@"type"];
  if (typeObject == nil) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:@"GeoJson geomtry object must have \"type\"."];
    return nil;
  }

  NSLog(@"warning: Unless all positions in a Geometry Object contain an "
        @"altitude coordinate, all altitudes will be set to 0 at surface level "
        @"for that Geometry Object.");

  NSString *type = [NSString stringWithFormat:@"%@", typeObject];
  if ([type isEqualToString:@"FeatureCollection"]) {
    // TODO: parseFeatureCollection
  } else {
    if ([type isEqualToString:@"Feature"]) {
      // TODO: feature
    }
    [self switchToType:jsonObject error:error];
  }

  return mLayer;
}

- (void)switchToType:(NSDictionary *)object error:(NSError **)error {
  NSString *type =
      [NSString stringWithFormat:@"%@", [object objectForKey:@"type"]];
  if ([type isEqualToString:@"Polygon"]) {
    NSArray *rings = [MSMapGeoJsonParser getCoordinates:object error:error];
    if (*error != nil) {
      return;
    }
    [self parsePolygon:rings error:error];
  } else if ([type isEqualToString:@"Point"]) {
    NSArray *coordinates = [MSMapGeoJsonParser getCoordinates:object
                                                        error:error];
    if (coordinates == nil) {
      return;
    }
    [self parsePoint:coordinates error:error];
  } else if ([type isEqualToString:@"MultiPoint"]) {
    [self parseMultiPoint:object error:error];
  } else if ([type isEqualToString:@"LineString"]) {
    NSArray *pathArray = [MSMapGeoJsonParser getCoordinates:object error:error];
    if (*error != nil) {
      return;
    }
    [self parseLineString:pathArray error:error];
  } else if ([type isEqualToString:@"MultiLineString"]) {
    [self parseMultiLineString:object error:error];
  }
  // TODO: rest of geometry types
}

+ (NSArray * _Nullable)getCoordinates:(NSDictionary *)object
                               error:(NSError **)error {
  NSArray *coordinates = [object objectForKey:@"coordinates"];
  if (coordinates == nil) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:@"Object must contain \"coordinates\" array."];
    return nil;
  }
  return coordinates;
}

- (void)parsePoint:(NSArray *)coordinates error:(NSError **)error {
  MSGeoposition *position = [MSMapGeoJsonParser parseGeoposition:coordinates
                                                     useAltitude:true
                                                           error:error];

  if (position == nil) {
    return;
  }

  MSGeopoint *point;
  if (coordinates.count > 2) {
    point = [[MSGeopoint alloc]
               initWithPosition:position
        altitudeReferenceSystem:MSMapAltitudeReferenceSystemEllipsoid];
  } else {
    point = [[MSGeopoint alloc]
               initWithPosition:position
        altitudeReferenceSystem:MSMapAltitudeReferenceSystemSurface];
  }
  MSMapIcon *icon = [[MSMapIcon alloc] init];
  icon.location = point;
  [mLayer.elements addMapElement:icon];
}

- (void)parseMultiPoint:(NSDictionary *)object error:(NSError **)error {
  NSArray *coordinates = [object objectForKey:@"coordinates"];
  for (int i = 0; i < coordinates.count; i++) {
    [self parsePoint:coordinates[i] error:error];
    if (*error != nil) {
      return;
    }
  }
}

- (void)parseLineString:(NSArray *)pathArray error:(NSError **)error {
  if (pathArray.count < 2) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:[@"LineString must contain at least two positions. "
                          @"Instead saw: "
                             stringByAppendingString:
                                 [MSMapGeoJsonParser arrayToString:pathArray]]];
    return;
  }
  MSMapAltitudeReferenceSystem system =
      [MSMapGeoJsonParser getAltitudeReferenceSystem:pathArray error:error];
  if (*error != nil) {
    return;
  }
  MSGeopath *path = [MSMapGeoJsonParser parsePath:pathArray
                          altitudeReferenceSystem:system
                                            error:error];
  if (*error != nil) {
    return;
  }
  MSMapPolyline *line = [[MSMapPolyline alloc] init];
  line.path = path;
  [mLayer.elements addMapElement:line];
}

- (void)parseMultiLineString:(NSDictionary *)shape error:(NSError **)error {
  NSArray *coordinates = [shape valueForKey:@"coordinates"];
  if (![coordinates isKindOfClass:[NSArray class]]) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:[@"coordinates of MultiLineString "
                          @"must be an array. Instead saw: "
                             stringByAppendingFormat:@"%@", coordinates]];
    return;
  }
  for (id obj in coordinates) {
    [self parseLineString:(NSArray *)obj error:error];
    if (*error != nil) {
      return;
    }
  }
}

- (void)parsePolygon:(NSArray *)jsonRings error:(NSError **)error {
  if (![jsonRings isKindOfClass:[NSArray class]]) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:[@"coordinates value must contain "
                          @"array of Polygon rings. Instead saw: "
                             stringByAppendingFormat:@"%@", jsonRings]];
    return;
  }
  if (jsonRings.count == 0) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:[@"coordinates value must contain "
                          @"at least one Polygon ring. Instead saw: "
                             stringByAppendingString:
                                 [MSMapGeoJsonParser arrayToString:jsonRings]]];
    return;
  }
  MSMapAltitudeReferenceSystem system = MSMapAltitudeReferenceSystemEllipsoid;
  for (NSArray *ring in jsonRings) {
    system = [MSMapGeoJsonParser getAltitudeReferenceSystem:ring error:error];
    if (*error != nil) {
      return;
    }
    if (system == MSMapAltitudeReferenceSystemSurface) {
      break;
    }
  }

  NSArray<MSGeopath *> *rings = [[NSArray alloc] init];
  for (id obj in jsonRings) {
    NSArray *pathArray = (NSArray *)obj;
    MSGeopath *path = [MSMapGeoJsonParser parsePath:pathArray
                            altitudeReferenceSystem:system
                                              error:error];
    if (*error != nil) {
      return;
    }
    [MSMapGeoJsonParser verifyPolygonRing:pathArray error:error];
    if (*error != nil) {
      return;
    }
    rings = [rings arrayByAddingObject:path];
  }
  MSMapPolygon *poly = [[MSMapPolygon alloc] init];
  poly.paths = rings;
  [mLayer.elements addMapElement:poly];
}

+ (MSGeoposition * _Nullable)parseGeoposition:(NSArray *)coordinates
                                 useAltitude:(bool)useAltitude
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

    if (useAltitude && coordinates.count > 2) {
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

+ (MSGeopath *)parsePath:(NSArray *)pathArray
    altitudeReferenceSystem:
        (MSMapAltitudeReferenceSystem)altitudeReferenceSystem
                      error:(NSError **)error {
  if (pathArray.count == 0) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:
            [@"Geopath must contain an array of positions. Instead saw: "
                stringByAppendingString:[MSMapGeoJsonParser
                                            arrayToString:pathArray]]];
    return nil;
  }

  NSArray<MSGeoposition *> *path = [[NSArray alloc] init];
  for (id obj in pathArray) {
    NSArray *latLong = (NSArray *)obj;
    bool useAltitude = false;
    if (altitudeReferenceSystem == MSMapAltitudeReferenceSystemEllipsoid) {
      useAltitude = true;
    }
    MSGeoposition *position = [self parseGeoposition:latLong
                                         useAltitude:useAltitude
                                               error:error];
    if (*error != nil) {
      return nil;
    }
    path = [path arrayByAddingObject:position];
  }
  return [[MSGeopath alloc] initWithPositions:path
                      altitudeReferenceSystem:altitudeReferenceSystem];
}

+ (void)verifyPolygonRing:(NSArray *)path error:(NSError **)error {
  if (path.count < 4) {
    NSString *array = @"[";
    for (int i = 0; i < path.count - 1; i++) {
      array = [array
          stringByAppendingString:[[MSMapGeoJsonParser
                                      arrayToString:[path objectAtIndex:i]]
                                      stringByAppendingString:@", "]];
    }
    array = [array
        stringByAppendingString:[[MSMapGeoJsonParser
                                    arrayToString:[path
                                                      objectAtIndex:path.count -
                                                                    1]]
                                    stringByAppendingString:@"]"]];
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:
            [@"Polygon ring must have at least 4 positions, and the first and "
             @"last position must be the same. Instead saw: "
                stringByAppendingString:array]];
    return;
  }
  NSArray *firstPosition = [path firstObject];
  NSArray *lastPosition = [path lastObject];
  if ((firstPosition.count < 3 && lastPosition.count < 3) ||
      (firstPosition.count > 2 && lastPosition.count > 2)) {

    for (int i = 0; i < MIN(firstPosition.count, lastPosition.count); i++) {
      if ([firstPosition objectAtIndex:i] != [lastPosition objectAtIndex:i]) {
        NSString *stringArray = [@"first: "
            stringByAppendingString:[MSMapGeoJsonParser
                                        arrayToString:firstPosition]];
        stringArray = [stringArray
            stringByAppendingString:
                [@"  last: ["
                    stringByAppendingString:[MSMapGeoJsonParser
                                                arrayToString:lastPosition]]];

        *error = [MSMapGeoJsonParser
            makeGeoJsonError:[@"First and last coordinate pair of each polygon "
                              @"ring must be the same. Instead saw: "
                                 stringByAppendingString:stringArray]];
        return;
      }
    }

  } else {
    NSString *stringArray =
        [@"first: " stringByAppendingString:[MSMapGeoJsonParser
                                                arrayToString:firstPosition]];
    stringArray = [stringArray
        stringByAppendingString:
            [@"  last: ["
                stringByAppendingString:[MSMapGeoJsonParser
                                            arrayToString:lastPosition]]];

    *error = [MSMapGeoJsonParser
        makeGeoJsonError:[@"First and last coordinate pair of each polygon "
                          @"ring must be the same. Instead saw: "
                             stringByAppendingString:stringArray]];
  }
}

+ (MSMapAltitudeReferenceSystem)getAltitudeReferenceSystem:(NSArray *)array
                                                     error:(NSError **)error {
  if (![array isKindOfClass:[NSArray class]]) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:[@"coordinates must contain an array of "
                          @"positions. Instead saw: "
                             stringByAppendingFormat:@"%@", (NSString *)array]];
    return MSMapAltitudeReferenceSystemSurface;
  }
  for (id obj in array) {
    if (![obj isKindOfClass:[NSArray class]]) {
      *error = [MSMapGeoJsonParser
          makeGeoJsonError:[@"coordinates must contain an array of "
                            @"positions. Instead saw: "
                               stringByAppendingFormat:@"%@", (NSString *)obj]];
      return MSMapAltitudeReferenceSystemSurface;
    }
    NSArray *point = (NSArray *)obj;
    if (point.count < 3) {
      return MSMapAltitudeReferenceSystemSurface;
    }
  }
  return MSMapAltitudeReferenceSystemEllipsoid;
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
