//
//  MSGeoJsonParser.m
//  MapsModulesSample
//
//  Created by Elizabeth Bartusiak (t-elbart) on 6/12/20.
//  Copyright © 2020 Microsoft.
//  Licensed under the MIT license.
//

#import "MSMapGeoJsonParser.h"
#import <MicrosoftMaps/MicrosoftMaps.h>

@implementation MSMapGeoJsonParser {
  MSMapElementLayer *layer;
};

+ (MSMapElementLayer *)parse:(NSString *)geojson {
  if (geojson == (id)[NSNull null] || geojson.length == 0) {
    @throw [NSException exceptionWithName:NSInvalidArgumentException
                                   reason:@"Input cannot be null."
                                 userInfo:nil];
    ;
  }

  MSMapGeoJsonParser *instance = [[MSMapGeoJsonParser alloc] init];
  return [instance internalParse:geojson];
}

- (MSMapElementLayer *)internalParse:(NSString *)geojson {
  layer = [[MSMapElementLayer alloc] init];
  NSData *jsonData = [geojson dataUsingEncoding:NSUTF8StringEncoding];
  NSError *jsonError;
  NSDictionary *jsonObject =
      [NSJSONSerialization JSONObjectWithData:jsonData
                                      options:kNilOptions
                                        error:&jsonError];

  if (jsonObject == nil) {
    @throw [NSException exceptionWithName:jsonError.domain
                                   reason:jsonError.debugDescription
                                 userInfo:nil];
    ;
  }

  NSString *type =
      [NSString stringWithFormat:@"%@", [jsonObject objectForKey:@"type"]];
  if ([type isEqualToString:@"FeatureCollection"]) {
    // TODO: parseFeatureCollection
  } else {
    if ([type isEqualToString:@"Feature"]) {
      // TODO: feature
    }
    [self switchToType:jsonObject];
  }

  return layer;
}

- (void)switchToType:(NSDictionary *)object {
  NSString *type =
      [NSString stringWithFormat:@"%@", [object objectForKey:@"type"]];
  if ([type isEqualToString:@"Polygon"]) {
    // TODO: parsePolygon
  } else if ([type isEqualToString:@"Point"]) {
    [self parsePoint:[self getCoordinates:object]];
  }
  // TODO: rest of geometry types
}

- (NSArray *)getCoordinates:(NSDictionary *)object {
  NSArray *coordinates = [object objectForKey:@"coordinates"];
  if (coordinates == nil) {
    @throw [NSException exceptionWithName:@"JSONException"
                                   reason:@"Error getting coordinates array."
                                 userInfo:nil];
    ;
  } else {
    return coordinates;
  }
}

- (void)parsePoint:(NSArray *)coordinates {
  MSGeoposition *position = [self parseGeoposition:coordinates];
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

- (MSGeoposition *)parseGeoposition:(NSArray *)coordinates {
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
    @throw [NSException exceptionWithName:@"MSGeoJsonParseException"
                                   reason:@"coordinates array must contain at "
                                          @"least latitude and longitude."
                                 userInfo:nil];
    ;
  }
}

@end
