//
//  ViewController.m
//  MSMapsModules
//
//  Created by Elizabeth Bartusiak (t-elbart) on 6/16/20.
//  Copyright © 2020 Microsoft.
//  Licensed under the MIT license.
//

#import "ViewController.h"
#import "MSMapGeoJsonParser.h"
#import <MicrosoftMaps/MicrosoftMaps.h>

@interface ViewController ()
@property(weak, nonatomic) IBOutlet MSMapView *mMapView;
@property(weak, nonatomic) MSMapElementLayer *elementLayer;

@end

@implementation ViewController

- (void)viewDidLoad {
  [super viewDidLoad];

  self.mMapView.credentialsKey =
      @"AtU8jGR_LBjMwjDhkdtTr1_KnN-MA9GI-lgtC2VzOuzgwS5lhPPUCRrSUpSk-Edj";

  MSGeopoint *point = [[MSGeopoint alloc] initWithLatitude:47.609466
                                                 longitude:-122.265185
                                                  altitude:0
                                   altitudeReferenceSystem:4];

  MSMapScene *scene = [MSMapScene sceneWithLocation:point zoomLevel:10];
  [self.mMapView setScene:scene withAnimationKind:MSMapAnimationKindNone];

  NSString *geojson = @"{\"type\": \"Point\", \"coordinates\": [30, 10]}";
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:geojson];
  [self.mMapView.layers addMapLayer:layer];
}

@end
