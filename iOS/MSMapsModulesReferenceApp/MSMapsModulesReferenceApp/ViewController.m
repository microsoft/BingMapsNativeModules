//
//  ViewController.m
//  MSMapsModules
//
//  Created by Elizabeth Bartusiak (t-elbart) on 6/16/20.
//  Copyright © 2020 Microsoft Corporation.
//  Licensed under the MIT license.
//

#import "ViewController.h"
#import "MSMapGeoJsonParser.h"
#import <MSMapsModules/MSMapGeoJsonParser.h>
#import <MicrosoftMaps/MicrosoftMaps.h>

@interface ViewController ()

@property(weak, nonatomic) IBOutlet MSMapView *mMapView;
@property(weak, nonatomic) MSMapElementLayer *elementLayer;

@end

@implementation ViewController

- (void)viewDidLoad {
  [super viewDidLoad];

  /* Add your credentials key in the following line.
   See
   https://docs.microsoft.com/en-us/bingmaps/getting-started/bing-maps-dev-center-help/getting-a-bing-maps-key
   for instructions on how to get a key.
   */
  self.mMapView.credentialsKey = @"YOUR_KEY_HERE";

  MSGeopoint *point = [[MSGeopoint alloc] initWithLatitude:47.609
                                                 longitude:-122.265
                                                  altitude:0
                                   altitudeReferenceSystem:4];

  MSMapScene *scene = [MSMapScene sceneWithLocation:point zoomLevel:10];
  [self.mMapView setScene:scene withAnimationKind:MSMapAnimationKindNone];

  NSString *geojson =
      @"{\"type\": \"Point\", \"coordinates\": [-122.265, 47.609]}";
  NSError *error;
  MSMapElementLayer *layer = [MSMapGeoJsonParser parse:geojson error:&error];
  if (error == nil) {
    [self.mMapView.layers addMapLayer:layer];
  }
}

@end
