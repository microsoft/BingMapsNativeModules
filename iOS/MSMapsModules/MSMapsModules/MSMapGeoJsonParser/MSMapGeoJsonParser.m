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
    *error = [NSError
        errorWithDomain:@"com.microsoft.modules.MSMapsModules.JSONError"
                   code:-200
               userInfo:@{NSLocalizedDescriptionKey : @"Malformed JSON."}];
    return nil;
  }

  NSObject *typeObject = [jsonObject objectForKey:@"type"];
  if (typeObject == nil) {
    *error = [NSError
        errorWithDomain:
            @"com.microsoft.modules.MSMapsModules.MSMapGeoJsonParseError"
                   code:-500
               userInfo:@{
                 NSLocalizedDescriptionKey :
                     @"GeoJson geomtry object must have \"type\"."
               }];
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
  if ([type isEqualToString:@"Polygon"]) {
    // TODO: parsePolygon
  } else if ([type isEqualToString:@"Point"]) {
    NSArray *coordinates = [MSMapGeoJsonParser getCoordinates:object
                                                        error:error];
    if (coordinates == nil) {
      return;
    }
    [self parsePoint:coordinates error:error];
  } else if ([type isEqualToString:@"MultiPoint"]) {
    [self parseMultiPoint:object error:error];
  }
  // TODO: rest of geometry types
}

+ (NSArray * _Nullable)getCoordinates:(NSDictionary *)object
                               error:(NSError **)error {
  NSArray *coordinates = [object objectForKey:@"coordinates"];
  if (coordinates == nil) {
    *error = [NSError
        errorWithDomain:@"com.microsoft.modules.MSMapsModules.JSONError"
                   code:-200
               userInfo:@{
                 NSLocalizedDescriptionKey : @"Error getting coordinates array."
               }];
    return nil;
  }
  return coordinates;
}

- (void)parsePoint:(NSArray *)coordinates error:(NSError **)error {
  MSGeoposition *position = [MSMapGeoJsonParser parseGeoposition:coordinates
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
    if (error == nil) {
      return;
    }
  }
}

+ (MSGeoposition * _Nullable)parseGeoposition:(NSArray *)coordinates
                                       error:(NSError **)error {
  if ([coordinates isKindOfClass:[NSArray class]] && coordinates.count >= 2) {
    if (![coordinates[0] isKindOfClass:[NSNumber class]]) {
      *error = [NSError
          errorWithDomain:
              @"com.microsoft.modules.MSMapsModules.MSMapGeoJsonParseError"
                     code:-500
                 userInfo:@{
                   NSLocalizedDescriptionKey : @"Longitude must be a number."
                 }];
      return nil;
    }

    double longitude = [coordinates[0] doubleValue];
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

    if (![coordinates[1] isKindOfClass:[NSNumber class]]) {
      *error = [NSError
          errorWithDomain:
              @"com.microsoft.modules.MSMapsModules.MSMapGeoJsonParseError"
                     code:-500
                 userInfo:@{
                   NSLocalizedDescriptionKey : @"Latitude must be a number."
                 }];
      return nil;
    }

    double latitude = [coordinates[1] doubleValue];
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

    double altitude = 0;

    if (coordinates.count > 2) {
      if (![coordinates[2] isKindOfClass:[NSNumber class]]) {
        *error = [NSError
            errorWithDomain:
                @"com.microsoft.modules.MSMapsModules.MSMapGeoJsonParseError"
                       code:-500
                   userInfo:@{
                     NSLocalizedDescriptionKey : @"Altitude must be a number."
                   }];
        return nil;
      }

      NSNumber *altitudeObj = coordinates[2];
      altitude = [altitudeObj doubleValue];
    }
    return [[MSGeoposition alloc] initWithLatitude:latitude
                                         longitude:longitude
                                          altitude:altitude];
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

NS_ASSUME_NONNULL_END
