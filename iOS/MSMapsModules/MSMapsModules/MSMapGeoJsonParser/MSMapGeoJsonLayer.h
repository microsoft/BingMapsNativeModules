//
//  MSMapGeoJsonLayer.h
//  MSMapsModules
//
//  Created by Elizabeth Bartusiak (t-elbart) on 7/13/2020.
//  Copyright © 2020 Microsoft Corporation.
//  Licensed under the MIT license.
//

#import <Foundation/Foundation.h>
#import <MicrosoftMaps/MicrosoftMaps.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface MSMapGeoJsonLayer : MSMapElementLayer

/* Setter methods set the style of all elements existing in the layer. The
 * setter methods do not apply to new elements added afterwards. */
- (void)setFillColor:(UIColor * _Nonnull)fillColor;
- (void)setStrokeColor:(UIColor * _Nonnull)strokeColor;
- (void)setStrokeDashed:(BOOL)strokeDashed;
- (void)setStrokeWidth:(int)strokeWidth;
- (void)setPolygonsVisible:(BOOL)polygonsVisible;
- (void)setPolylinesVisible:(BOOL)polylinesVisible;
- (void)setIconsVisible:(BOOL)iconsVisible;

- (NSArray<MSMapElement *> *)removePolygons;
- (NSArray<MSMapElement *> *)removePolylines;
- (NSArray<MSMapElement *> *)removeIcons;

@end

NS_ASSUME_NONNULL_END
