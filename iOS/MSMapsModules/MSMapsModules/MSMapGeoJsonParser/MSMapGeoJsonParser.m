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
  bool mDidWarn;
  NSString *mWarning;
};

- (MSMapGeoJsonParser *)init {
  if (self = [super init]) {
    mLayer = [[MSMapElementLayer alloc] init];
    mDidWarn = false;
    mWarning = @"Warning: Unless all positions in a Geometry Object contain an "
               @"altitude coordinate, all altitudes will be set to 0 at "
               @"surface level for that Geometry Object.";
  }
  return self;
}

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
    MSMapAltitudeReferenceSystem system =
        (coordinates.count > 2) ? MSMapAltitudeReferenceSystemEllipsoid
                                : MSMapAltitudeReferenceSystemSurface;
    [self createIconAddToLayer:position
        altitudeReferenceSystem:system
                          error:error];
  } else {
    if ([type isEqualToString:@"Polygon"]) {
      NSArray *rings = [MSMapGeoJsonParser getCoordinates:object error:error];
      if (*error != nil) {
        return;
      }
      [self parsePolygon:rings error:error];
    } else if ([type isEqualToString:@"MultiPoint"]) {
      NSArray *coordinates = [MSMapGeoJsonParser getCoordinates:object
                                                          error:error];
      if (*error != nil) {
        return;
      }
      [self parseMultiPoint:coordinates error:error];
    } else if ([type isEqualToString:@"LineString"]) {
      NSArray *pathArray = [MSMapGeoJsonParser getCoordinates:object
                                                        error:error];
      if (*error != nil) {
        return;
      }
      [self parseLineString:pathArray error:error];
    } else if ([type isEqualToString:@"MultiLineString"]) {
      NSArray *pathArray = [MSMapGeoJsonParser getCoordinates:object
                                                        error:error];
      if (*error != nil) {
        return;
      }
      [self parseMultiLineString:pathArray error:error];
    }
  }
  // TODO: rest of geometry types
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

- (void)createIconAddToLayer:(MSGeoposition *)position
     altitudeReferenceSystem:(MSMapAltitudeReferenceSystem)system
                       error:(NSError **)error {
  MSGeopoint *point = [[MSGeopoint alloc] initWithPosition:position
                                   altitudeReferenceSystem:system];
  MSMapIcon *icon = [[MSMapIcon alloc] init];
  icon.location = point;
  [mLayer.elements addMapElement:icon];
}

- (void)parseMultiPoint:(NSArray *)coordinates error:(NSError **)error {
  MSMapAltitudeReferenceSystem system = MSMapAltitudeReferenceSystemEllipsoid;
  NSArray<MSGeoposition *> *positions = [self parsePositionArray:coordinates
                                         altitudeReferenceSystem:&system
                                                           error:error];
  if (*error != nil) {
    return;
  }
  if (system == MSMapAltitudeReferenceSystemSurface) {
    [MSMapGeoJsonParser setAltitudesZero:positions];
  }
  for (int i = 0; i < positions.count; i++) {
    [self createIconAddToLayer:positions[i]
        altitudeReferenceSystem:system
                          error:error];
    if (*error != nil) {
      return;
    }
  }
}

- (void)parseLineString:(NSArray *)pathArray error:(NSError **)error {
  MSMapAltitudeReferenceSystem system = MSMapAltitudeReferenceSystemEllipsoid;
  NSArray<MSGeoposition *> *validatedArray = [self parseLineArray:pathArray
                                          altitudeReferenceSystem:&system
                                                            error:error];
  if (*error != nil) {
    return;
  }
  if (system == MSMapAltitudeReferenceSystemSurface) {
    [MSMapGeoJsonParser setAltitudesZero:validatedArray];
  }
  [self createLineAddToLayer:validatedArray altitudeReferenceSystem:system];
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

- (void)createLineAddToLayer:(NSArray<MSGeoposition *> *)positions
     altitudeReferenceSystem:(MSMapAltitudeReferenceSystem)system {
  MSMapPolyline *line = [[MSMapPolyline alloc] init];
  line.path = [[MSGeopath alloc] initWithPositions:positions
                           altitudeReferenceSystem:system];
  [mLayer.elements addMapElement:line];
}

- (void)parseMultiLineString:(NSArray *)coordinates error:(NSError **)error {
  NSMutableArray *lines = [[NSMutableArray alloc] init];
  MSMapAltitudeReferenceSystem system = MSMapAltitudeReferenceSystemEllipsoid;
  for (id obj in coordinates) {
    if (![obj isKindOfClass:[NSArray class]]) {
      *error = [MSMapGeoJsonParser
          makeGeoJsonError:[@"MultiLineString coordinates must be an array of "
                            @"line arrays. Instead saw: "
                               stringByAppendingFormat:@"%@", obj]];
    }
    NSArray<MSGeoposition *> *parsedLine = [self parseLineArray:(NSArray *)obj
                                        altitudeReferenceSystem:&system
                                                          error:error];
    if (*error != nil) {
      return;
    }
    [lines addObject:parsedLine];
  }
  for (NSMutableArray<MSGeoposition *> *line in lines) {
    if (system == MSMapAltitudeReferenceSystemSurface) {
      [MSMapGeoJsonParser setAltitudesZero:line];
    }
    [self createLineAddToLayer:line altitudeReferenceSystem:system];
  }
}

- (void)parsePolygon:(NSArray *)jsonRings error:(NSError **)error {
  if (jsonRings.count == 0) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:[@"coordinates value must contain "
                          @"at least one Polygon ring. Instead saw: "
                             stringByAppendingString:
                                 [MSMapGeoJsonParser arrayToString:jsonRings]]];
    return;
  }
  NSMutableArray *parsedRings = [[NSMutableArray alloc] init];
  MSMapAltitudeReferenceSystem system = MSMapAltitudeReferenceSystemEllipsoid;
  for (id obj in jsonRings) {
    if (![obj isKindOfClass:[NSArray class]]) {
      *error = [MSMapGeoJsonParser
          makeGeoJsonError:
              [@"Polygon rings must be an array of positions. Instead saw: "
                  stringByAppendingFormat:@"%@", obj]];
      return;
    }
    NSArray *pathArray = (NSArray *)obj;
    [MSMapGeoJsonParser verifyPolygonRing:pathArray error:error];
    if (*error != nil) {
      return;
    }
    NSArray<MSGeoposition *> *parsedArray = [self parsePositionArray:pathArray
                                             altitudeReferenceSystem:&system
                                                               error:error];
    if (*error != nil) {
      return;
    }
    [parsedRings addObject:parsedArray];
  }

  NSMutableArray<MSGeopath *> *rings = [[NSMutableArray alloc] init];
  for (NSArray<MSGeoposition *> *positions in parsedRings) {
    if (system == MSMapAltitudeReferenceSystemSurface) {
      [MSMapGeoJsonParser setAltitudesZero:positions];
    }
    MSGeopath *path = [[MSGeopath alloc] initWithPositions:positions
                                   altitudeReferenceSystem:system];
    [rings addObject:path];
  }
  MSMapPolygon *poly = [[MSMapPolygon alloc] init];
  poly.paths = rings;
  [mLayer.elements addMapElement:poly];
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

+ (void)verifyPolygonRing:(NSArray *)path error:(NSError **)error {
  if (![path isKindOfClass:[NSArray class]]) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:
            [@"Polygon rings must be an array of positions. Instead saw: "
                stringByAppendingFormat:@"%@", path]];
    return;
  }
  if (path.count < 4) {
    NSString *array = [MSMapGeoJsonParser arrayToString:path];
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:
            [@"Polygon ring must have at least 4 positions, and the first and "
             @"last position must be the same. Instead saw: "
                stringByAppendingString:array]];
    return;
  }
  NSArray *firstPosition = [path firstObject];
  NSArray *lastPosition = [path lastObject];
  if (firstPosition.count != lastPosition.count) {
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

  } else {
    for (int i = 0; i < firstPosition.count; i++) {
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
  }
}

- (NSArray<MSGeoposition *> * _Nullable)parsePositionArray:(NSArray *)array
                                  altitudeReferenceSystem:
                                      (MSMapAltitudeReferenceSystem *)system
                                                    error:(NSError **)error {
  if (![array isKindOfClass:[NSArray class]]) {
    *error = [MSMapGeoJsonParser
        makeGeoJsonError:[@"coordinates must contain an array of "
                          @"positions. Instead saw: "
                             stringByAppendingFormat:@"%@", (NSString *)array]];
    return nil;
  }
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
  bool useAltitude = true;
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
      useAltitude = false;
      *system = MSMapAltitudeReferenceSystemSurface;
      if (!mDidWarn) {
        NSLog(@"%@", mWarning);
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

+ (void)setAltitudesZero:(NSArray<MSGeoposition *> *)array {
  for (MSGeoposition *position in array) {
    position.altitude = 0;
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
