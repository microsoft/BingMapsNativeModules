Pod::Spec.new do |spec|
  spec.name         = "MSMapsModules"
  spec.version      = "0.0.1"
  spec.summary      = "Library for parsing GeoJSON and adding shapes to a MapGeoJsonLayer."
  spec.description  = <<-DESC
Library for parsing GeoJSON, extracting the shapes, and adding them to a new MapGeoJsonLayer for use with a Microsoft Maps Mapview. 
                   DESC
  spec.homepage     = "https://github.com/microsoft/BingMapsNativeModules"
  spec.license      = { :type => 'MIT'}
  spec.author    = "Microsoft Corporation"
  spec.platform     = :ios, "9.0"
  spec.source       = { :git => 'https://github.com/microsoft/BingMapsNativeModules.git'}
  spec.source_files  = "iOS/MSMapsModules/**/*.h"

end
