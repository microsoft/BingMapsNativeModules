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

@end
