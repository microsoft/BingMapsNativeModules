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

- (id)init {
  if (self = [super init]) {
    self.fillColor = UIColor.blueColor;
    self.strokeColor = UIColor.blueColor;
    self.strokeDashed = NO;
    self.strokeWidth = 1;

    self.polygonsVisible = YES;
    self.polylinesVisible = YES;
    self.iconsVisible = YES;
  }
  return self;
}

- (void)setFillColor:(UIColor *)newfillColor {
  if (_fillColor != newfillColor) {
    _fillColor = newfillColor;
    for (MSMapElement *element in [super elements]) {
      if ([element isKindOfClass:[MSMapPolygon class]]) {
        MSMapPolygon *polygon = (MSMapPolygon *)element;
        polygon.fillColor = _fillColor;
      }
    }
  }
}

- (void)setStrokeColor:(UIColor *)newStrokeColor {
  if (_strokeColor != newStrokeColor) {
    _strokeColor = newStrokeColor;
    for (MSMapElement *element in [super elements]) {
      if ([element isKindOfClass:[MSMapPolygon class]]) {
        MSMapPolygon *polygon = (MSMapPolygon *)element;
        polygon.strokeColor = newStrokeColor;
      } else if ([element isKindOfClass:[MSMapPolyline class]]) {
        MSMapPolyline *polyline = (MSMapPolyline *)element;
        polyline.strokeColor = newStrokeColor;
      }
    }
  }
}

- (void)setStrokeDashed:(BOOL)newIsStrokeDashed {
  if (_strokeDashed != newIsStrokeDashed) {
    _strokeDashed = newIsStrokeDashed;
    for (MSMapElement *element in [super elements]) {
      if ([element isKindOfClass:[MSMapPolygon class]]) {
        MSMapPolygon *polygon = (MSMapPolygon *)element;
        polygon.strokeDashed = newIsStrokeDashed;
      } else if ([element isKindOfClass:[MSMapPolyline class]]) {
        MSMapPolyline *polyline = (MSMapPolyline *)element;
        polyline.strokeDashed = newIsStrokeDashed;
      }
    }
  }
}

- (void)setStrokeWidth:(int)newStrokeWidth {
  if (_strokeWidth != newStrokeWidth) {
    _strokeWidth = newStrokeWidth;
    for (MSMapElement *element in [super elements]) {
      if ([element isKindOfClass:[MSMapPolygon class]]) {
        MSMapPolygon *polygon = (MSMapPolygon *)element;
        polygon.strokeWidth = newStrokeWidth;
      } else if ([element isKindOfClass:[MSMapPolyline class]]) {
        MSMapPolyline *polyline = (MSMapPolyline *)element;
        polyline.strokeWidth = newStrokeWidth;
      }
    }
  }
}

- (void)setPolygonsVisible:(BOOL)newPolygonsVisible {
  if (_polygonsVisible != newPolygonsVisible) {
    _polygonsVisible = newPolygonsVisible;
    for (MSMapElement *element in [super elements]) {
      if ([element isKindOfClass:[MSMapPolygon class]]) {
        MSMapPolygon *polygon = (MSMapPolygon *)element;
        polygon.visible = _polygonsVisible;
      }
    }
  }
}

- (void)setPolylinesVisible:(BOOL)newPolylinesVisible {
  if (_polylinesVisible != newPolylinesVisible) {
    _polylinesVisible = newPolylinesVisible;
    for (MSMapElement *element in [super elements]) {
      if ([element isKindOfClass:[MSMapPolyline class]]) {
        MSMapPolyline *polyline = (MSMapPolyline *)element;
        polyline.visible = _polylinesVisible;
      }
    }
  }
}

- (void)setIconsVisible:(BOOL)newIconsVisible {
  if (_iconsVisible != newIconsVisible) {
    _iconsVisible = newIconsVisible;
    for (MSMapElement *element in [super elements]) {
      if ([element isKindOfClass:[MSMapIcon class]]) {
        MSMapIcon *icon = (MSMapIcon *)element;
        icon.visible = _iconsVisible;
      }
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

- (void)removeAll:(NSArray<MSMapElement *> *)elementsToRemove {
  for (MSMapElement *element in elementsToRemove) {
    [[super elements] removeMapElement:element];
  }
}

@end
