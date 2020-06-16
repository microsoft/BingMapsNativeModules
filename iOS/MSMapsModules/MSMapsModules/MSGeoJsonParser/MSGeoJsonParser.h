//
//  MSGeoJsonParser.h
//  MSMapsModules
//
//  Created by Elizabeth Bartusiak (t-elbart) on 6/12/20.
//  Copyright © 2020 Microsoft.
//  Licensed under the MIT license.
//

#import <MicrosoftMaps/MicrosoftMaps.h>

NS_ASSUME_NONNULL_BEGIN

@interface MSGeoJsonParser : NSObject

- (MSGeoJsonParser *)init NS_UNAVAILABLE;
+ (MSGeoJsonParser *)new NS_UNAVAILABLE;
+ (MSMapElementLayer *)parse:(NSString *)geojson;

@end

NS_ASSUME_NONNULL_END
