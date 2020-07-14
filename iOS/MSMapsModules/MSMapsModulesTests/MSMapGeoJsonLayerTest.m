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

- (void)testGetStrokeDashedDefault {
  MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
  XCTAssertFalse(layer.strokeDashed);
  XCTAssertNotNil(layer.elements);
  [layer.elements insertMapElement:[[MSMapPolyline alloc] init] atIndex:0];
  [layer.elements insertMapElement:[[MSMapIcon alloc] init] atIndex:1];
  [layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:2];
  XCTAssertEqual(3, layer.elements.count);
  int objectIndex = 0;
  for (MSMapElement *element in layer.elements) {
    if (objectIndex == 0) {
      XCTAssertTrue([element isKindOfClass:[MSMapPolyline class]]);
      MSMapPolyline *polyline = (MSMapPolyline *)element;
      XCTAssertFalse(polyline.strokeDashed);
    } else if (objectIndex == 2) {
      XCTAssertTrue([element isKindOfClass:[MSMapPolygon class]]);
      MSMapPolygon *polygon = (MSMapPolygon *)element;
      XCTAssertFalse(polygon.strokeDashed);
    }
    objectIndex++;
  }
}

- (void)testSetStrokeDashedAllShapes {
  MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
  XCTAssertFalse(layer.strokeDashed);
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

- (void)testGetStrokeWidthDefault {
  MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
  XCTAssertEqual(1, layer.strokeWidth);
  XCTAssertNotNil(layer.elements);
  [layer.elements insertMapElement:[[MSMapPolyline alloc] init] atIndex:0];
  [layer.elements insertMapElement:[[MSMapIcon alloc] init] atIndex:1];
  [layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:2];
  XCTAssertEqual(3, layer.elements.count);
  int objectIndex = 0;
  for (MSMapElement *element in layer.elements) {
    if (objectIndex == 0) {
      XCTAssertTrue([element isKindOfClass:[MSMapPolyline class]]);
      MSMapPolyline *polyline = (MSMapPolyline *)element;
      XCTAssertEqual(1, polyline.strokeWidth);
    } else if (objectIndex == 2) {
      XCTAssertTrue([element isKindOfClass:[MSMapPolygon class]]);
      MSMapPolygon *polygon = (MSMapPolygon *)element;
      XCTAssertEqual(1, polygon.strokeWidth);
    }
    objectIndex++;
  }
}

- (void)testSetStrokeWidthAllShapes {
  MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
  XCTAssertEqual(1, layer.strokeWidth);
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

- (void)testGetArePolygonsVisibleDefault {
  MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
  XCTAssertTrue(layer.polygonsVisible);
  XCTAssertNotNil(layer.elements);
  [layer.elements insertMapElement:[[MSMapPolyline alloc] init] atIndex:0];
  [layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:1];
  [layer.elements insertMapElement:[[MSMapIcon alloc] init] atIndex:2];
  [layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:3];
  XCTAssertEqual(4, layer.elements.count);
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
      XCTAssertTrue(polygon.visible);
    }
    objectIndex++;
  }
}

- (void)testSetArePolygonsVisible {
  MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
  XCTAssertTrue(layer.polygonsVisible);
  XCTAssertNotNil(layer.elements);
  [layer.elements insertMapElement:[[MSMapPolyline alloc] init] atIndex:0];
  [layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:1];
  [layer.elements insertMapElement:[[MSMapIcon alloc] init] atIndex:2];
  [layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:3];
  XCTAssertEqual(4, layer.elements.count);
  layer.polygonsVisible = NO;
  XCTAssertFalse(layer.polygonsVisible);
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

- (void)testGetArePolylinesVisibleDefault {
  MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
  XCTAssertTrue(layer.polylinesVisible);
  XCTAssertNotNil(layer.elements);
  [layer.elements insertMapElement:[[MSMapPolyline alloc] init] atIndex:0];
  [layer.elements insertMapElement:[[MSMapPolyline alloc] init] atIndex:1];
  [layer.elements insertMapElement:[[MSMapIcon alloc] init] atIndex:2];
  [layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:3];
  XCTAssertEqual(4, layer.elements.count);
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
      XCTAssertTrue(polyline.visible);
    }
    objectIndex++;
  }
}

- (void)testSetArePolylinesVisible {
  MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
  XCTAssertTrue(layer.polylinesVisible);
  XCTAssertNotNil(layer.elements);
  [layer.elements insertMapElement:[[MSMapPolyline alloc] init] atIndex:0];
  [layer.elements insertMapElement:[[MSMapPolyline alloc] init] atIndex:1];
  [layer.elements insertMapElement:[[MSMapIcon alloc] init] atIndex:2];
  [layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:3];
  XCTAssertEqual(4, layer.elements.count);
  layer.polylinesVisible = NO;
  XCTAssertFalse(layer.polylinesVisible);
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

- (void)testGetAreIconsVisibleDefault {
  MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
  XCTAssertTrue(layer.iconsVisible);
  XCTAssertNotNil(layer.elements);
  [layer.elements insertMapElement:[[MSMapPolyline alloc] init] atIndex:0];
  [layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:1];
  [layer.elements insertMapElement:[[MSMapIcon alloc] init] atIndex:2];
  [layer.elements insertMapElement:[[MSMapIcon alloc] init] atIndex:3];
  XCTAssertEqual(4, layer.elements.count);
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
      XCTAssertTrue(icon.visible);
    }
    objectIndex++;
  }
}

- (void)testSetAreIconsVisible {
  MSMapGeoJsonLayer *layer = [[MSMapGeoJsonLayer alloc] init];
  XCTAssertTrue(layer.iconsVisible);
  XCTAssertNotNil(layer.elements);
  [layer.elements insertMapElement:[[MSMapPolyline alloc] init] atIndex:0];
  [layer.elements insertMapElement:[[MSMapPolygon alloc] init] atIndex:1];
  [layer.elements insertMapElement:[[MSMapIcon alloc] init] atIndex:2];
  [layer.elements insertMapElement:[[MSMapIcon alloc] init] atIndex:3];
  XCTAssertEqual(4, layer.elements.count);
  layer.iconsVisible = NO;
  XCTAssertFalse(layer.iconsVisible);
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

@end
