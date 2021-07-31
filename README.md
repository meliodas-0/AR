# AR
Intern project for Homelane.
In this project I have used 3 APIs: 
1. [Maintained Sceneform SDK](https://github.com/ThomasGorisse/sceneform-android-sdk)
2. [JoyStick](https://github.com/controlwear/virtual-joystick-android)
3. [CircleImageView](https://github.com/hdodenhof/CircleImageView) 

## Sceneform
Sceneform is a 3D framework with a physically based renderer that's optimized for mobile devices and that makes it easy for you to build Augmented Reality (AR) apps without requiring OpenGL or Unity.

## JoyStick
The main requirement for the joystick was because the object placed over another object was not moving with the drag gestures. When I tried to move it by gestures, the parent of the object moves with it result in no change in the position relative to the parent which is why I used the Joystick to move the object on the parent according to the camera angle.
<br>By Vector mathematics to find the angle with the x axis : &theta; = <img src="https://render.githubusercontent.com/render/math?math=cos^{-1}\sqrt{\frac{x^2}{x^2%2Bz^2}}">

# Project
Brief guide about the project.
- Here Project has single Activity which is MainActivity which will run on execution of the app.
- For Placing model we need to detect the surface via camera and set depth mode. So for this we need ArFragment in Sceneform SDK which checks for the camera permission and does plane detection.
- For Detection we need to implement 
  <ol>
    <li>
      <strong>
        FragmentOnAttachListener:
      </strong> 
      This interface will attach the fragment with required configurations to the FragmentContainer in the activity_main.xml
    </li>
    <li>
      <strong>
        BaseArFragment.OnTapArPlaneListener: 
       </strong> 
       This interface will be used to detect tap on the plane and return HitResult (which defines the point the real world where our anchor will go), tapped plane and the Motion Event.
     </li>
     <li>
      <strong>
        ArFragment.OnViewCreatedListener: 
      </strong>
      This is invoked when the ARSceneView is created and added to the fragment. You can use it to configure the ARSceneView.
     </li>
     <li>
      <strong>
        BaseArFragment.OnSessionConfigurationListener: 
      </strong>
      This is invoked when we need to change the configuration of the current session.
      </li>
    </ol> 
- To load any model we need to place Anchors which represents the real world coordinates and maintain the position of the object in our app. 
- In Sceneform every object placed in the Session needs to be a Node. So we need to change it to AnchorNode and then we can set the the scene as the parent of the AnchorNode. 
- For a model to be able to scale, rotate and translate through the plane we need the model to be a TransformableNode. 
- This TranformableNode will have the parent(if placing object over object then the parent will be that object otherwise AnchorNode will be the parent) and will have renderable model.
- There are 4 menu items which shows dialog accordingly:
  * Models : This shows all the models available in the app and we can choose which model we need to place.
  * Find Distance : This option finds distance between selected Objects, planes and object-plane;
  * Toggle Placement over Object:  This toggles the placeObjectOverObject boolean.
  * Change Depth Mode: This mode has 3 options which are AUTOMATIC, RAW_DEPTH_ONLY and DISABLED.
- There are 5 Helpers classes: 
  * **TouchHelper** : This class distinguishes between different types of touches.
  * **SnackbarHelper** : Helper to manage the Snackbar. Hides the Android boilerplate code, and exposes simpler methods.
  * **JoystickHelper** : This class moves the object according to the joystick.
  * **DistanceHelper** : Helper to manage the Distances. Hides the Android boilerplate code, and exposes simpler methods. 
  * **ModelHelper** : This Helper contains all the operational methods the model can perform. E.g. Delete Model, PlaceModel, etc.
  
