//
//  MSGeoJsonParser.h
//  MSMapsModules
//
//  Created by Elizabeth Bartusiak (t-elbart) on 6/12/20.
//  Copyright © 2020 Microsoft Corporation.
//  Licensed under the MIT license.
//

#import "MSMapGeoJsonParser.h"
#import <MicrosoftMaps/MicrosoftMaps.h>

@implementation MSMapGeoJsonParser {
  MSMapElementLayer *layer;
};

+ (MSMapElementLayer *)parse:(NSString *)geojson error:(NSError **)error {
  if (geojson == (id)[NSNull null] || geojson.length == 0) {
    *error = [NSError
        errorWithDomain:
            @"com.microsoft.modules.MSMapsModules.InvalidArgumentError"
                   code:-100
               userInfo:@{
                 NSLocalizedDescriptionKey : @"Input String cannot be null."
               }];
    return nil;
  }

  MSMapGeoJsonParser *instance = [[MSMapGeoJsonParser alloc] init];
  return [instance internalParse:geojson error:error];
}

- (MSMapElementLayer *)internalParse:(NSString *)geojson
                               error:(NSError **)error {
  layer = [[MSMapElementLayer alloc] init];
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

  return layer;
}

- (void)switchToType:(NSDictionary *)object error:(NSError **)error {
  NSString *type =
      [NSString stringWithFormat:@"%@", [object objectForKey:@"type"]];
  if ([type isEqualToString:@"Polygon"]) {
    // TODO: parsePolygon
  } else if ([type isEqualToString:@"Point"]) {
    NSArray *coordinates = [self getCoordinates:object error:error];
    if (coordinates != nil) {
      [self parsePoint:coordinates error:error];
    } else {
      layer = nil;
    }
  }
  // TODO: rest of geometry types
}

- (NSArray *)getCoordinates:(NSDictionary *)object error:(NSError **)error {
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

- (void)parsePoint:(NSArray *)coordinates error:(NSError **)error {
  MSGeoposition *position = [self parseGeoposition:coordinates error:error];

  if (position == nil) {
    layer = nil;
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
  [layer.elements addMapElement:icon];
}

- (MSGeoposition *)parseGeoposition:(NSArray *)coordinates
                              error:(NSError **)error {
  if (coordinates.count >= 2) {
    NSNumber *longitudeObj = coordinates[0];
    double longitude = [longitudeObj doubleValue];
    if (longitude < -180 || longitude > 180) {
      // TODO: throw exception
      return nil;
    }

    NSNumber *latitudeObj = coordinates[1];
    double latitude = [latitudeObj doubleValue];
    if (latitude < -90 || latitude > 90) {
      // TODO: throw exception
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
            @"com.microsoft.modules.MSMapsModules.MSGeoJsonParseError"
                   code:-500
               userInfo:@{
                 NSLocalizedDescriptionKey : @"coordinates array must contain "
                                             @"at least latitude and longitude."
               }];
    return nil;
  }
}

@end
