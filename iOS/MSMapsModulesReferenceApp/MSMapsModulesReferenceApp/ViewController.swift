//
//  ViewController.swift
//  MSMapsModulesReferenceApp
//
//  Created by Elizabeth Bartusiak (t-elbart) on 6/24/20.
//  Copyright © 2020 Microsoft Corporation.
//  Licensed under the MIT license.
//

import MicrosoftMaps
import MSMapsModules
import UIKit

class ViewController: UIViewController {
    @IBOutlet var mMapView: MSMapView!

    override func viewDidLoad() {
        super.viewDidLoad()

        /* Add your credentials key in the following line.
         See
         https://docs.microsoft.com/en-us/bingmaps/getting-started/bing-maps-dev-center-help/getting-a-bing-maps-key
         for instructions on how to get a key.
         */
        mMapView.credentialsKey = "YOUR_KEY_HERE"

        let LOCATION_LAKE_WASHINGTON = MSGeopoint(latitude: 47.61, longitude: -122.27)
        let scene = MSMapScene(location: LOCATION_LAKE_WASHINGTON, zoomLevel: 10)
        mMapView.setScene(scene, with: .none)

        let geojson = "{\"type\": \"Point\", \"coordinates\": [-122.265185, 47.609466]}"
        do {
            let layer = try MSMapGeoJsonParser.parse(geojson)
            mMapView.layers.add(layer)
        } catch {
            print(error.self)
        }
    }
}
