//
//  ViewController.m
//  MSMapsModules
//
//  Created by Elizabeth Bartusiak (t-elbart) on 6/16/20.
//  Copyright © 2020 Microsoft.
//  Licensed under the MIT license.
//

#import <UIKit/UIKit.h>
#import "AppDelegate.h"

int main(int argc, char * argv[]) {
	NSString * appDelegateClassName;
	@autoreleasepool {
	    // Setup code that might create autoreleased objects goes here.
	    appDelegateClassName = NSStringFromClass([AppDelegate class]);
	}
	return UIApplicationMain(argc, argv, nil, appDelegateClassName);
}
