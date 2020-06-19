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

@implementation MSMapGeoJsonParser {
  MSMapElementLayer *mLayer;
};

+ (nullable MSMapElementLayer *)parse:(nonnull NSString *)geojson
                                error:(NSError * _Nullable *)error {
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

- (nullable MSMapElementLayer *)internalParse:(nonnull NSString *)geojson
                                        error:(NSError * _Nullable *)error {
  mLayer = [[MSMapElementLayer alloc] init];
  NSData *jsonData = [geojson dataUsingEncoding:NSUTF8StringEncoding];
  NSError *jsonError;
  NSDictionary *jsonObject =
      [NSJSONSerialization JSONObjectWithData:jsonData
                                      options:kNilOptions
                                        error:&jsonError];

  if (jsonObject == nil) {
    *error = [NSError
        errorWithDomain:@"com.microsoft.modules.MSMapsModules.JSONError"
                   code:-200
               userInfo:@{NSLocalizedDescriptionKey : @"Malformed JSON."}];
    return nil;
  }

  NSString *type =
      [NSString stringWithFormat:@"%@", [jsonObject objectForKey:@"type"]];
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

- (void)switchToType:(nonnull NSDictionary *)object
               error:(NSError * _Nullable *)error {
  NSString *type =
      [NSString stringWithFormat:@"%@", [object objectForKey:@"type"]];
  if ([type isEqualToString:@"Polygon"]) {
    // TODO: parsePolygon
  } else if ([type isEqualToString:@"Point"]) {
    NSArray *coordinates = [self getCoordinates:object error:error];
    if (coordinates != nil) {
      [self parsePoint:coordinates error:error];
    } else {
      mLayer = nil;
    }
  }
  // TODO: rest of geometry types
}

- (nullable NSArray *)getCoordinates:(nonnull NSDictionary *)object
                               error:(NSError * _Nullable *)error {
  NSArray *coordinates = [object objectForKey:@"coordinates"];
  if (coordinates == nil) {
    *error = [NSError
        errorWithDomain:@"com.microsoft.modules.MSMapsModules.JSONError"
                   code:-200
               userInfo:@{
                 NSLocalizedDescriptionKey : @"Error getting coordinates array."
               }];
    return nil;
  } else {
    return coordinates;
  }
}

- (void)parsePoint:(nonnull NSArray *)coordinates
             error:(NSError * _Nullable *)error {
  MSGeoposition *position = [self parseGeoposition:coordinates error:error];

  if (position == nil) {
    mLayer = nil;
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

- (nullable MSGeoposition *)parseGeoposition:(nonnull NSArray *)coordinates
                                       error:(NSError * _Nullable *)error {
  if (coordinates.count >= 2) {
    NSNumber *longitudeObj = coordinates[0];
    double longitude = [longitudeObj doubleValue];
    if (longitude < -180 || longitude > 180) {
      *error = [NSError
          errorWithDomain:
              @"com.microsoft.modules.MSMapsModules.MSMapGeoJsonParseError"
                     code:-500
                 userInfo:@{
                   NSLocalizedDescriptionKey :
                       @"Longitude must be in range [-180, 180]."
                 }];
    }

    NSNumber *latitudeObj = coordinates[1];
    double latitude = [latitudeObj doubleValue];
    if (latitude < -90 || latitude > 90) {
      *error = [NSError
          errorWithDomain:
              @"com.microsoft.modules.MSMapsModules.MSMapGeoJsonParseError"
                     code:-500
                 userInfo:@{
                   NSLocalizedDescriptionKey :
                       @"Latitude must be in range [-90, 90]."
                 }];
      return nil;
    }

    if (coordinates.count > 2) {
      NSNumber *altitudeObj = coordinates[2];
      double altitude = [altitudeObj doubleValue];
      return [[MSGeoposition alloc] initWithLatitude:latitude
                                           longitude:longitude
                                            altitude:altitude];
    }
    return [[MSGeoposition alloc] initWithLatitude:latitude
                                         longitude:longitude
                                          altitude:0];
  } else {
    *error = [NSError
        errorWithDomain:
            @"com.microsoft.modules.MSMapsModules.MSMapGeoJsonParseError"
                   code:-500
               userInfo:@{
                 NSLocalizedDescriptionKey : @"coordinates array must contain "
                                             @"at least latitude and longitude."
               }];
    return nil;
  }
}

@end
