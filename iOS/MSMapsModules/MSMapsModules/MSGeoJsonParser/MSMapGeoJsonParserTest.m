//
//  MSGeoJsonParserTest.m
//  MapsModulesSampleTests
//
//  Created by Elizabeth Bartusiak (t-elbart) on 6/15/20.
//  Copyright © 2020 Microsoft.
//  Licensed under the MIT license.
//

#import "MSMapGeoJsonParser.h"
#import <MicrosoftMaps/MicrosoftMaps.h>
#import <XCTest/XCTest.h>

@interface MSMapGeoJsonParserTest : XCTestCase

@end

@implementation MSMapGeoJsonParserTest

- (void)setUp {
  // Put setup code here. This method is called before the invocation of each
  // test method in the class.
}

- (void)tearDown {
  // Put teardown code here. This method is called after the invocation of each
  // test method in the class.
}

- (void)testParsePoint {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": [30, 10]}";
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:geojson];
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

- (void)testNullInputThrowsException {
  XCTAssertThrowsSpecificNamed([MSMapGeoJsonParser parse:NULL], NSException,
                               NSInvalidArgumentException,
                               @"Input String cannot be null.");
}

- (void)testEmptyInputThrowsException {
  XCTAssertThrows([MSMapGeoJsonParser parse:@""]);
}

- (void)testNoBracketsJsonThrowsException {
  XCTAssertThrows([MSMapGeoJsonParser
      parse:@"\"type\": \"Point\", \"coordinates\": [-122.26, 47.609]"]);
}

- (void)testNoCoordinatesThrowsException {
  NSString *geojson = @"{\"type\": \"Point\"}";
  XCTAssertThrowsSpecificNamed([MSMapGeoJsonParser parse:geojson], NSException,
                               @"JSONException",
                               @"Error getting coordinates array.");
}

- (void)testEmptyCoordinatesThrowsException {
  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": []}";
  XCTAssertThrowsSpecificNamed(
      [MSMapGeoJsonParser parse:geojson], NSException,
      @"MSGeoJsonParseException",
      @"coordinates array must contain at least latitude and longitude.");
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

- (void)testPerformanceExample {
  // This is an example of a performance test case.
  [self measureBlock:^{
      // Put the code you want to measure the time of here.
  }];
}

@end
