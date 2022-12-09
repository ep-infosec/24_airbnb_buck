#import <UIKit/UIKit.h>
#import "TestHostApp.h"

@interface TestHostApp () <UIApplicationDelegate>

@end

@implementation TestHostApp

@synthesize window;

- (BOOL)           application:(UIApplication *)application
willFinishLaunchingWithOptions:(NSDictionary *)options {
  NSLog(@"Test host app launched, test should pass");
  UIView *rootView =
    [[[UIView alloc] initWithFrame:[[UIScreen mainScreen] bounds]] autorelease];
  rootView.backgroundColor = [UIColor greenColor];
  UILabel *label = [[[UILabel alloc] initWithFrame:rootView.bounds] autorelease];
  label.text = @"Testing rocks!";
  label.textAlignment = NSTextAlignmentCenter;
  label.font = [UIFont systemFontOfSize:36];
  label.textColor = [UIColor whiteColor];
  label.numberOfLines = 0;
  label.opaque = NO;
  [rootView addSubview:label];
  UIViewController *rootViewController =
    [[[UIViewController alloc] initWithNibName:nil bundle:nil] autorelease];
  rootViewController.view = rootView;
  UIWindow *newWindow =
    [[[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]] autorelease];
  newWindow.rootViewController = rootViewController;
  self.window = newWindow;
  [self.window makeKeyAndVisible];
  return YES;
}

- (int)magicValue {
  return 42;
}

@end

int main(int argc, char **argv) {
  return UIApplicationMain(argc, argv, nil, NSStringFromClass([TestHostApp class]));
}
