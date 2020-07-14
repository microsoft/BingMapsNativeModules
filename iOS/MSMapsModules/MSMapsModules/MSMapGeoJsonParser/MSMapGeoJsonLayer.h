//
//  MSMapGeoJsonLayer.h
//  MSMapsModules
//
//  Created by Elizabeth Bartusiak (t-elbart) on 7/13/2020.
//  Copyright © 2020 Microsoft Corporation.
//  Licensed under the MIT license.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <MicrosoftMaps/MicrosoftMaps.h>

NS_ASSUME_NONNULL_BEGIN

@interface MSMapGeoJsonLayer : MSMapElementLayer

@property (nonatomic) UIColor *fillColor;
@property (nonatomic) UIColor *strokeColor;
@property(nonatomic) BOOL strokeDashed;
@property(nonatomic) int strokeWidth;

@property(nonatomic) BOOL polygonsVisible;
@property(nonatomic) BOOL polylinesVisible;
@property(nonatomic) BOOL iconsVisible;

- (id)init;
- (NSArray<MSMapElement *> *)removePolygons;

@end

NS_ASSUME_NONNULL_END
