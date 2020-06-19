//
//  MSMapGeoJsonParser.h
//  MSMapsModules
//
//  Created by Elizabeth Bartusiak (t-elbart) on 6/17/20.
//  Copyright © 2020 Microsoft Corporation.
//  Licensed under the MIT license.
//

#import <Foundation/Foundation.h>
#import <MicrosoftMaps/MicrosoftMaps.h>

NS_ASSUME_NONNULL_BEGIN

@interface MSMapGeoJsonParser : NSObject

- (MSMapGeoJsonParser *)init NS_UNAVAILABLE;
+ (MSMapGeoJsonParser *)new NS_UNAVAILABLE;
+ (MSMapElementLayer * _Nullable)parse:(NSString *)geojson
                                error:(NSError * _Nullable * _Nullable)error;

@end

NS_ASSUME_NONNULL_END
