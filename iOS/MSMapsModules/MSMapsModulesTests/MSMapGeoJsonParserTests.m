//
//  MSMapsModulesTests.m
//  MSMapsModulesTests
//
//  Created by Elizabeth Bartusiak (t-elbart) on 6/17/20.
//  Copyright © 2020 Microsoft Corporation.
//  Licensed under the MIT license.
//

#import "MSMapGeoJsonParser.h"
#import <MicrosoftMaps/MicrosoftMaps.h>
#import <XCTest/XCTest.h>

@interface MSMapGeoJsonParserTest : XCTestCase

@end

@implementation MSMapGeoJsonParserTest

- (void)testParsePoint {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": [30, 10]}";
  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:geojson error:&error];
  XCTAssertNil(error);
  MSMapElementCollection *collection = layer.elements;
  XCTAssertNotNil(collection);
  XCTAssertEqual(1, collection.count);

  MSMapIcon *icon;
  for (id obj in collection) {
    icon = (MSMapIcon *)obj;
  }
  XCTAssertNotNil(icon);
  NSArray *expectedPoints =
      [[NSArray alloc] initWithObjects:[NSNumber numberWithDouble:30],
                                       [NSNumber numberWithDouble:10], nil];

  [self checkExpectedPosition:expectedPoints
           withActualPosition:icon.location.position];
}

- (void)testParseMultiPoint {
  NSString *geojson = @"{\"type\": \"MultiPoint\", \"coordinates\": [[10, 40], "
                      @"[40, 30], [20, 20], [30, 10]]}";
  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:geojson error:&error];
  XCTAssertNil(error);
  MSMapElementCollection *collection = layer.elements;
  XCTAssertNotNil(collection);
  XCTAssertEqual(4, collection.count);

  double expected[4][2] = {{10, 40}, {40, 30}, {20, 20}, {30, 10}};
  NSArray *expectedPoints = [[NSArray alloc] init];
  for (int row = 0; row < 4; row++) {
    NSArray *pair = [[NSArray alloc]
        initWithObjects:[NSNumber numberWithDouble:expected[row][0]],
                        [NSNumber numberWithDouble:expected[row][1]], nil];
    expectedPoints = [expectedPoints arrayByAddingObject:pair];
  }

  MSMapIcon *icon;
  int index = 0;
  for (id obj in collection) {
    icon = (MSMapIcon *)obj;
    XCTAssertNotNil(icon);
    [self checkExpectedPosition:expectedPoints[index]
             withActualPosition:icon.location.position];
    index++;
  }
}

- (void)testParsePointWithAltitude {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": [30, 10, 5]}";
  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:geojson error:&error];
  XCTAssertNil(error);
  MSMapElementCollection *collection = layer.elements;
  XCTAssertNotNil(collection);
  XCTAssertEqual(1, collection.count);

  MSMapIcon *icon;
  for (id obj in collection) {
    icon = (MSMapIcon *)obj;
  }
  XCTAssertNotNil(icon);
  NSArray *expectedPoints =
      [[NSArray alloc] initWithObjects:[NSNumber numberWithDouble:30],
                                       [NSNumber numberWithDouble:10],
                                       [NSNumber numberWithDouble:5], nil];

  [self checkExpectedPosition:expectedPoints
           withActualPosition:icon.location.position];
}

- (void)testParsePointManyCoordinates {
  NSString *geojson =
      @"{\"type\": \"Point\", \"coordinates\": [30, 10, 5, 3, 4]}";
  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:geojson error:&error];
  XCTAssertNil(error);
  MSMapElementCollection *collection = layer.elements;
  XCTAssertNotNil(collection);
  XCTAssertEqual(1, collection.count);

  MSMapIcon *icon;
  for (id obj in collection) {
    icon = (MSMapIcon *)obj;
  }
  XCTAssertNotNil(icon);
  NSArray *expectedPoints =
      [[NSArray alloc] initWithObjects:[NSNumber numberWithDouble:30],
                                       [NSNumber numberWithDouble:10],
                                       [NSNumber numberWithDouble:5], nil];

  [self checkExpectedPosition:expectedPoints
           withActualPosition:icon.location.position];
}

- (void)testNullInputGivesError {
  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:NULL error:&error];
  XCTAssertNil(layer);
  XCTAssertEqual(-100, error.code);
}

- (void)testNullInputNullError {
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:NULL error:nil];
  XCTAssertNil(layer);
}

- (void)testEmptyInputGivesError {
  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:@"" error:&error];
  XCTAssertNil(layer);
  XCTAssertEqual(-100, error.code);
}

- (void)testNoBracketsJsonGivesError {
  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser
      parse:@"\"type\": \"Point\", \"coordinates\": [-122.26, 47.609]"
      error:&error];
  XCTAssertNil(layer);
  XCTAssertEqual(-200, error.code);
}

- (void)testNoCoordinatesGivesError {
  NSString *geojson = @"{\"type\": \"Point\"}";
  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:geojson error:&error];
  XCTAssertNil(layer);
  XCTAssertEqual(-200, error.code);
}

- (void)testEmptyCoordinatesGivesError {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": []}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testNoTypeGivesError {
  NSString *geojson = @"{\"coordinates\": [30, 10]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testCoordinateNaNGivesError {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": [NaN, 10]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-200, error.code);
}

- (void)testLongitudeNullGivesError {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": [null, 10]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testLatitudeNullGivesError {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": [30, null]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testAltitudeNullGivesError {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": [30, 10, null]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testLongitudeStringGivesError {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": [\"a\", 10]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testLatitudeStringGivesError {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": [30, \"b\"]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)testAltitudeStringGivesError {
  NSString *geojson =
      @"{\"type\": \"Point\", \"coordinates\": [30, 10, \"c\"]}";
  NSError *error;
  XCTAssertNil([MSMapGeoJsonParser parse:geojson error:&error]);
  XCTAssertEqual(-500, error.code);
}

- (void)checkExpectedPosition:(NSArray *)expectedPoints
           withActualPosition:(MSGeoposition *)position {
  XCTAssertEqual([[expectedPoints objectAtIndex:0] doubleValue],
                 position.longitude);
  XCTAssertEqual([[expectedPoints objectAtIndex:1] doubleValue],
                 position.latitude);
  if (expectedPoints.count > 2) {
    XCTAssertEqual([[expectedPoints objectAtIndex:2] doubleValue],
                   position.altitude);
  } else {
    XCTAssertEqual(0, position.altitude);
  }
}

@end
