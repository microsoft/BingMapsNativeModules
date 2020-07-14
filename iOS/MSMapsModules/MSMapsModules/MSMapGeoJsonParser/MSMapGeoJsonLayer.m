//
//  MSMapGeoJsonLayer.m
//  MSMapsModules
//
//  Created by Elizabeth Bartusiak (t-elbart) on 7/13/2020.
//  Copyright © 2020 Microsoft Corporation.
//  Licensed under the MIT license.
//

#import "MSMapGeoJsonLayer.h"

@implementation MSMapGeoJsonLayer

- (void)setFillColor:(UIColor *)fillColor {
  for (MSMapElement *element in [super elements]) {
    if ([element isKindOfClass:[MSMapPolygon class]]) {
      MSMapPolygon *polygon = (MSMapPolygon *)element;
      polygon.fillColor = fillColor;
    }
  }
}

- (void)setStrokeColor:(UIColor *)strokeColor {
  for (MSMapElement *element in [super elements]) {
    if ([element isKindOfClass:[MSMapPolygon class]]) {
      MSMapPolygon *polygon = (MSMapPolygon *)element;
      polygon.strokeColor = strokeColor;
    } else if ([element isKindOfClass:[MSMapPolyline class]]) {
      MSMapPolyline *polyline = (MSMapPolyline *)element;
      polyline.strokeColor = strokeColor;
    }
  }
}

- (void)setStrokeDashed:(BOOL)strokeDashed {
  for (MSMapElement *element in [super elements]) {
    if ([element isKindOfClass:[MSMapPolygon class]]) {
      MSMapPolygon *polygon = (MSMapPolygon *)element;
      polygon.strokeDashed = strokeDashed;
    } else if ([element isKindOfClass:[MSMapPolyline class]]) {
      MSMapPolyline *polyline = (MSMapPolyline *)element;
      polyline.strokeDashed = strokeDashed;
    }
  }
}

- (void)setStrokeWidth:(int)strokeWidth {
  for (MSMapElement *element in [super elements]) {
    if ([element isKindOfClass:[MSMapPolygon class]]) {
      MSMapPolygon *polygon = (MSMapPolygon *)element;
      polygon.strokeWidth = strokeWidth;
    } else if ([element isKindOfClass:[MSMapPolyline class]]) {
      MSMapPolyline *polyline = (MSMapPolyline *)element;
      polyline.strokeWidth = strokeWidth;
    }
  }
}

- (void)setPolygonsVisible:(BOOL)polygonsVisible {
  for (MSMapElement *element in [super elements]) {
    if ([element isKindOfClass:[MSMapPolygon class]]) {
      MSMapPolygon *polygon = (MSMapPolygon *)element;
      polygon.visible = polygonsVisible;
    }
  }
}

- (void)setPolylinesVisible:(BOOL)polylinesVisible {
  for (MSMapElement *element in [super elements]) {
    if ([element isKindOfClass:[MSMapPolyline class]]) {
      MSMapPolyline *polyline = (MSMapPolyline *)element;
      polyline.visible = polylinesVisible;
    }
  }
}

- (void)setIconsVisible:(BOOL)iconsVisible {
  for (MSMapElement *element in [super elements]) {
    if ([element isKindOfClass:[MSMapIcon class]]) {
      MSMapIcon *icon = (MSMapIcon *)element;
      icon.visible = iconsVisible;
    }
  }
}

- (NSArray<MSMapElement *> *)removePolygons {
  NSMutableArray<MSMapElement *> *elementsToRemove =
      [[NSMutableArray alloc] init];
  for (MSMapElement *element in [super elements]) {
    if ([element isKindOfClass:[MSMapPolygon class]]) {
      [elementsToRemove addObject:element];
    }
  }
  [self removeAll:elementsToRemove];
  return elementsToRemove;
}

- (NSArray<MSMapElement *> *)removePolylines {
  NSMutableArray<MSMapElement *> *elementsToRemove =
      [[NSMutableArray alloc] init];
  for (MSMapElement *element in [super elements]) {
    if ([element isKindOfClass:[MSMapPolyline class]]) {
      [elementsToRemove addObject:element];
    }
  }
  [self removeAll:elementsToRemove];
  return elementsToRemove;
}

- (NSArray<MSMapElement *> *)removeIcons {
  NSMutableArray<MSMapElement *> *elementsToRemove =
      [[NSMutableArray alloc] init];
  for (MSMapElement *element in [super elements]) {
    if ([element isKindOfClass:[MSMapIcon class]]) {
      [elementsToRemove addObject:element];
    }
  }
  [self removeAll:elementsToRemove];
  return elementsToRemove;
}

- (void)removeAll:(NSArray<MSMapElement *> *)elementsToRemove {
  for (MSMapElement *element in elementsToRemove) {
    [[super elements] removeMapElement:element];
  }
}

@end
