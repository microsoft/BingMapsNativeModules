//
//  MSMapGeoJsonLayerTest.m
//  MSMapsModulesTests
//
//  Created by Elizabeth Bartusiak (t-elbart) on 7/13/2020.
//  Copyright © 2020 Microsoft Corporation.
//  Licensed under the MIT license.
//

#import <XCTest/XCTest.h>
#import "MSMapGeoJsonLayer.h"

@interface MSMapGeoJsonLayerTest : XCTestCase

@end

@implementation MSMapGeoJsonLayerTest

- (void)testGetFillColorDefault {
	MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
	XCTAssertEqual(UIColor.blueColor, layer.fillColor);
	XCTAssertNotNil(layer.elements);
	[layer.elements addMapElement:[[MSMapPolygon alloc] init]];
	XCTAssertEqual(1, layer.elements.count);
	for (MSMapElement *element in layer.elements) {
		XCTAssertTrue([element isKindOfClass:[MSMapPolygon class]]);
		MSMapPolygon *polygon = (MSMapPolygon *)element;
		XCTAssertTrue([polygon.fillColor isEqual:UIColor.blueColor]);
	}
}

- (void)testSetFillColorOnePolygon {
	MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
	XCTAssertEqual(UIColor.blueColor, layer.fillColor);
	XCTAssertNotNil(layer.elements);
	[layer.elements insertMapElement:[[MSMapPolyline alloc] init] atIndex:0];
	[layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:1];
	
	XCTAssertEqual(2, layer.elements.count);
	layer.fillColor = UIColor.yellowColor;
	int objectIndex = 0;
	for (MSMapElement *element in layer.elements) {
		if (objectIndex == 1) {
			XCTAssertTrue([element isKindOfClass:[MSMapPolygon class]]);
			MSMapPolygon *polygon = (MSMapPolygon *)element;
			XCTAssertTrue([polygon.fillColor isEqual:UIColor.yellowColor]);
		}
		objectIndex++;
	}
}

- (void)testSetFillColorMultiplePolygon {
	MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
	XCTAssertEqual(UIColor.blueColor, layer.fillColor);
	XCTAssertNotNil(layer.elements);
	[layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:0];
	[layer.elements insertMapElement:[[MSMapPolyline alloc] init] atIndex:1];
	[layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:2];
	[layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:3];
	XCTAssertEqual(4, layer.elements.count);
	layer.fillColor = UIColor.yellowColor;
	int objectIndex = 0;
	for (MSMapElement *element in layer.elements) {
		if (objectIndex != 1) {
			XCTAssertTrue([element isKindOfClass:[MSMapPolygon class]]);
			MSMapPolygon *polygon = (MSMapPolygon *)element;
			XCTAssertTrue([polygon.fillColor isEqual:UIColor.yellowColor]);
		}
		objectIndex++;
	}
}

- (void)testGetStrokeColorDefault {
	MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
	XCTAssertEqual(UIColor.blueColor, layer.strokeColor);
	XCTAssertNotNil(layer.elements);
	[layer.elements insertMapElement:[[MSMapPolyline alloc] init] atIndex:0];
	[layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:1];
	XCTAssertEqual(2, layer.elements.count);
	layer.strokeColor = UIColor.greenColor;
	int objectIndex = 0;
	for (MSMapElement *element in layer.elements) {
		if (objectIndex == 0) {
			XCTAssertTrue([element isKindOfClass:[MSMapPolyline class]]);
			MSMapPolyline *polyline = (MSMapPolyline *)element;
			XCTAssertTrue([polyline.strokeColor isEqual:UIColor.greenColor]);
		} else {
			XCTAssertTrue([element isKindOfClass:[MSMapPolygon class]]);
			MSMapPolygon *polygon = (MSMapPolygon *)element;
			XCTAssertTrue([polygon.strokeColor isEqual:UIColor.greenColor]);
		}
		objectIndex++;
	}
}

- (void)testSetStrokeColorAllShapes {
	MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
	XCTAssertEqual(UIColor.blueColor, layer.fillColor);
	XCTAssertNotNil(layer.elements);
	[layer.elements insertMapElement:[[MSMapPolyline alloc] init] atIndex:0];
	[layer.elements insertMapElement:[[MSMapIcon alloc] init] atIndex:1];
	[layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:2];
	XCTAssertEqual(3, layer.elements.count);
	layer.strokeColor = UIColor.yellowColor;
	int objectIndex = 0;
	for (MSMapElement *element in layer.elements) {
		if (objectIndex == 0) {
			XCTAssertTrue([element isKindOfClass:[MSMapPolyline class]]);
			MSMapPolyline *polyline = (MSMapPolyline *)element;
			XCTAssertTrue([polyline.strokeColor isEqual:UIColor.yellowColor]);
		} else if (objectIndex == 2) {
			XCTAssertTrue([element isKindOfClass:[MSMapPolygon class]]);
			MSMapPolygon *polygon = (MSMapPolygon *)element;
			XCTAssertTrue([polygon.strokeColor isEqual:UIColor.yellowColor]);
		}
		objectIndex++;
	}
}

@end
