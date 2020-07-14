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

@property(nonatomic) UIColor *fillColor;
@property(nonatomic) UIColor *strokeColor;

- (id)init;

@end

NS_ASSUME_NONNULL_END
