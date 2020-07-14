//
//  MSMapGeoJsonLayerTest.m
//  MSMapsModulesTests
//
//  Created by Elizabeth Bartusiak (t-elbart) on 7/13/2020.
//  Copyright © 2020 Microsoft Corporation.
//  Licensed under the MIT license.
//

#import "MSMapGeoJsonLayer.h"
#import <XCTest/XCTest.h>

@interface MSMapGeoJsonLayerTest : XCTestCase

@end

@implementation MSMapGeoJsonLayerTest

- (void)testSetFillColorOnePolygon {
  MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
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

- (void)testSetStrokeColor {
  MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
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

- (void)testSetStrokeDashed {
  MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
  XCTAssertNotNil(layer.elements);
  [layer.elements insertMapElement:[[MSMapPolyline alloc] init] atIndex:0];
  [layer.elements insertMapElement:[[MSMapIcon alloc] init] atIndex:1];
  [layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:2];
  XCTAssertEqual(3, layer.elements.count);
  layer.strokeDashed = YES;
  int objectIndex = 0;
  for (MSMapElement *element in layer.elements) {
    if (objectIndex == 0) {
      XCTAssertTrue([element isKindOfClass:[MSMapPolyline class]]);
      MSMapPolyline *polyline = (MSMapPolyline *)element;
      XCTAssertTrue(polyline.strokeDashed);
    } else if (objectIndex == 2) {
      XCTAssertTrue([element isKindOfClass:[MSMapPolygon class]]);
      MSMapPolygon *polygon = (MSMapPolygon *)element;
      XCTAssertTrue(polygon.strokeDashed);
    }
    objectIndex++;
  }
}

- (void)testSetStrokeWidth {
  MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
  XCTAssertNotNil(layer.elements);
  [layer.elements insertMapElement:[[MSMapPolyline alloc] init] atIndex:0];
  [layer.elements insertMapElement:[[MSMapIcon alloc] init] atIndex:1];
  [layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:2];
  XCTAssertEqual(3, layer.elements.count);
  layer.strokeWidth = 5;
  int objectIndex = 0;
  for (MSMapElement *element in layer.elements) {
    if (objectIndex == 0) {
      XCTAssertTrue([element isKindOfClass:[MSMapPolyline class]]);
      MSMapPolyline *polyline = (MSMapPolyline *)element;
      XCTAssertEqual(5, polyline.strokeWidth);
    } else if (objectIndex == 2) {
      XCTAssertTrue([element isKindOfClass:[MSMapPolygon class]]);
      MSMapPolygon *polygon = (MSMapPolygon *)element;
      XCTAssertEqual(5, polygon.strokeWidth);
    }
    objectIndex++;
  }
}

- (void)testSetArePolygonsVisible {
  MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
  XCTAssertNotNil(layer.elements);
  [layer.elements insertMapElement:[[MSMapPolyline alloc] init] atIndex:0];
  [layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:1];
  [layer.elements insertMapElement:[[MSMapIcon alloc] init] atIndex:2];
  [layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:3];
  XCTAssertEqual(4, layer.elements.count);
  layer.polygonsVisible = NO;
  int objectIndex = 0;
  for (MSMapElement *element in layer.elements) {
    if (objectIndex == 0) {
      XCTAssertTrue([element isKindOfClass:[MSMapPolyline class]]);
      MSMapPolyline *polyline = (MSMapPolyline *)element;
      XCTAssertTrue(polyline.visible);
    } else if (objectIndex == 2) {
      XCTAssertTrue([element isKindOfClass:[MSMapIcon class]]);
      MSMapIcon *icon = (MSMapIcon *)element;
      XCTAssertTrue(icon.visible);
    } else {
      XCTAssertTrue([element isKindOfClass:[MSMapPolygon class]]);
      MSMapPolygon *polygon = (MSMapPolygon *)element;
      XCTAssertFalse(polygon.visible);
    }
    objectIndex++;
  }
}

- (void)testSetArePolylinesVisible {
  MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
  XCTAssertNotNil(layer.elements);
  [layer.elements insertMapElement:[[MSMapPolyline alloc] init] atIndex:0];
  [layer.elements insertMapElement:[[MSMapPolyline alloc] init] atIndex:1];
  [layer.elements insertMapElement:[[MSMapIcon alloc] init] atIndex:2];
  [layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:3];
  XCTAssertEqual(4, layer.elements.count);
  layer.polylinesVisible = NO;
  int objectIndex = 0;
  for (MSMapElement *element in layer.elements) {
    if (objectIndex == 3) {
      XCTAssertTrue([element isKindOfClass:[MSMapPolygon class]]);
      MSMapPolygon *polygon = (MSMapPolygon *)element;
      XCTAssertTrue(polygon.visible);
    } else if (objectIndex == 2) {
      XCTAssertTrue([element isKindOfClass:[MSMapIcon class]]);
      MSMapIcon *icon = (MSMapIcon *)element;
      XCTAssertTrue(icon.visible);
    } else {
      XCTAssertTrue([element isKindOfClass:[MSMapPolyline class]]);
      MSMapPolyline *polyline = (MSMapPolyline *)element;
      XCTAssertFalse(polyline.visible);
    }
    objectIndex++;
  }
}

- (void)testSetAreIconsVisible {
  MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
  XCTAssertNotNil(layer.elements);
  [layer.elements insertMapElement:[[MSMapPolyline alloc] init] atIndex:0];
  [layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:1];
  [layer.elements insertMapElement:[[MSMapIcon alloc] init] atIndex:2];
  [layer.elements insertMapElement:[[MSMapIcon alloc] init] atIndex:3];
  XCTAssertEqual(4, layer.elements.count);
  layer.iconsVisible = NO;
  int objectIndex = 0;
  for (MSMapElement *element in layer.elements) {
    if (objectIndex == 0) {
      XCTAssertTrue([element isKindOfClass:[MSMapPolyline class]]);
      MSMapPolyline *polyline = (MSMapPolyline *)element;
      XCTAssertTrue(polyline.visible);
    } else if (objectIndex == 1) {
      XCTAssertTrue([element isKindOfClass:[MSMapPolygon class]]);
      MSMapPolygon *polygon = (MSMapPolygon *)element;
      XCTAssertTrue(polygon.visible);
    } else {
      XCTAssertTrue([element isKindOfClass:[MSMapIcon class]]);
      MSMapIcon *icon = (MSMapIcon *)element;
      XCTAssertFalse(icon.visible);
    }
    objectIndex++;
  }
}

- (void)testRemovePolygons {
  MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
  [layer.elements insertMapElement:[[MSMapPolyline alloc] init] atIndex:0];
  [layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:1];
  [layer.elements insertMapElement:[[MSMapIcon alloc] init] atIndex:2];
  [layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:3];
  XCTAssertNotNil(layer.elements);
  XCTAssertEqual(4, layer.elements.count);
  NSArray *removedElements = [layer removePolygons];
  XCTAssertEqual(2, removedElements.count);
  for (MSMapElement *element in removedElements) {
    XCTAssertTrue([element isKindOfClass:[MSMapPolygon class]]);
  }
  XCTAssertEqual(2, layer.elements.count);
  for (MSMapElement *element in layer.elements) {
    XCTAssertFalse([element isKindOfClass:[MSMapPolygon class]]);
  }
}

- (void)testRemovePolylines {
  MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
  [layer.elements insertMapElement:[[MSMapPolyline alloc] init] atIndex:0];
  [layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:1];
  [layer.elements insertMapElement:[[MSMapIcon alloc] init] atIndex:2];
  [layer.elements insertMapElement:[[MSMapPolyline alloc] init] atIndex:3];
  XCTAssertNotNil(layer.elements);
  XCTAssertEqual(4, layer.elements.count);
  NSArray *removedElements = [layer removePolylines];
  XCTAssertEqual(2, removedElements.count);
  for (MSMapElement *element in removedElements) {
    XCTAssertTrue([element isKindOfClass:[MSMapPolyline class]]);
  }
  XCTAssertEqual(2, layer.elements.count);
  for (MSMapElement *element in layer.elements) {
    XCTAssertFalse([element isKindOfClass:[MSMapPolyline class]]);
  }
}

- (void)testRemoveIcons {
  MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
  [layer.elements insertMapElement:[[MSMapPolyline alloc] init] atIndex:0];
  [layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:1];
  [layer.elements insertMapElement:[[MSMapIcon alloc] init] atIndex:2];
  [layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:3];
  [layer.elements insertMapElement:[[MSMapIcon alloc] init] atIndex:4];
  XCTAssertNotNil(layer.elements);
  XCTAssertEqual(5, layer.elements.count);
  NSArray *removedElements = [layer removeIcons];
  XCTAssertEqual(2, removedElements.count);
  for (MSMapElement *element in removedElements) {
    XCTAssertTrue([element isKindOfClass:[MSMapIcon class]]);
  }
  XCTAssertEqual(3, layer.elements.count);
  for (MSMapElement *element in layer.elements) {
    XCTAssertFalse([element isKindOfClass:[MSMapIcon class]]);
  }
}

@end
