//
//  ViewController.swift
//  MSMapsModulesTestApp
//
//  Created by Elizabeth Bartusiak (t-elbart) on 6/24/20.
//  Copyright © 2020 Microsoft Corporation.
//  Licensed under the MIT license.
//

import MicrosoftMaps
import UIKit

class ViewController: UIViewController {
    @IBOutlet var mMapView: MSMapView!

    override func viewDidLoad() {
        super.viewDidLoad()

        /* Add your credential key in a keys.plist file. */
        var keys: NSDictionary?
        if let path = Bundle.main.path(forResource: "keys", ofType: "plist") {
            keys = NSDictionary(contentsOfFile: path)
        }
        mMapView.credentialsKey = keys?.object(forKey: "credentialsKey") as! String

        let LOCATION = MSGeopoint(latitude: 10, longitude: 30)
        let scene = MSMapScene(location: LOCATION, zoomLevel: 10)
        mMapView.setScene(scene, with: .none)

        let geojson = "{\"type\": \"Polygon\", \"coordinates\": [[[30, 10], [40, 40], [20, 40],[10, 20], [30, 10]]]}"

        do {
            let layer: MSMapGeoJsonLayer = try MSMapGeoJsonParser.parse(geojson)
            layer.strokeColor = UIColor.green
            layer.fillColor = UIColor.orange
            mMapView.layers.add(layer)
        } catch {
            print(error.self)
        }
    }
}
