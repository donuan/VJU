import SimpleOpenNI.*;
SimpleOpenNI  kinect;
import oscP5.*;
import netP5.*;
import blobDetection.*; // blobs
import java.util.*;
import processing.opengl.*; // opengl
import java.awt.Polygon;
import jsyphon.*; // Syphon
import javax.media.opengl.*;
import toxi.geom.*; // toxiclibs shapes and vectors
import toxi.processing.*; // toxiclibs display
import pbox2d.*; // shiffman's jbox2d helper library
import org.jbox2d.collision.shapes.*; // jbox2d
import org.jbox2d.common.*; // jbox2d
import org.jbox2d.dynamics.*; // jbox2d
boolean autoCalib=true;

 
//declarations
Boolean sketch1 = true; //izquierda particles
Boolean sketch2 = false;//centro fisica
Boolean sketch3 = false;//derecha modul8

PVector head = new PVector();
PVector neck = new PVector();
PVector leftShoulder = new PVector();
PVector rightShoulder = new PVector();
PVector leftElbow = new PVector();
PVector rightElbow = new PVector();
PVector leftHand = new PVector();
PVector rightHand = new PVector();
PVector torso = new PVector();
PVector leftHip = new PVector();
PVector rightHip = new PVector();
PVector leftKnee = new PVector();
PVector rightKnee = new PVector();
PVector leftFoot = new PVector();
PVector rightFoot = new PVector();
float leftHandXMap=0;
float leftHandYMap=0;
float rightHandXMap=0;
float rightHandYMap=0;
int usuario_lost = 0; //0 = lost // 1= no lost
 
PVector jnt = new PVector();//temp to test confidence!!!
float confidence;

PImage cam, blobs;
// the kinect's dimensions to be used later on for calculations
int kinectWidth = 640;
int kinectHeight = 480;
// to center and rescale from 640x480 to higher custom resolutions
float reScale;

// background color
color bgColor, blobColor;
// three color palettes (artifact from me storing many interesting color palettes as strings in an external data file ;-)
String[] palettes = {
  "-1117720,-13683658,-8410437,-9998215,-1849945,-5517090,-4250587,-14178341,-5804972,-3498634", 
  "-67879,-9633503,-8858441,-144382,-4996094,-16604779,-588031", 
  "-16711663,-13888933,-9029017,-5213092,-1787063,-11375744,-2167516,-15713402,-5389468,-2064585"
};
 
// an array called flow of 2250 Particle objects (see Particle class)
Particle[] flow = new Particle[500];
// global variables to influence the movement of all particles
float globalX, globalY;

// declare SimpleOpenNI object
SimpleOpenNI context;
// declare BlobDetection object
BlobDetection theBlobDetection;
// declare custom PolygonBlob object (see class for more info)
PolygonBlob poly = new PolygonBlob();
PolygonBlob3 poly3y2;
ToxiclibsSupport gfx;
 
OscP5 oscP5;
 
NetAddress MaxAddress;
OscMessage skeletonData;
 
GL gl;
PGraphicsOpenGL pgl; 
syphon syphon;
PBox2D box2d;

ArrayList<CustomShape> polygons = new ArrayList<CustomShape>(); 
 
/////
void setup()
{
   
  oscP5 = new OscP5(this, 12345);
  MaxAddress = new NetAddress("127.0.0.1", 12348);
 
  kinect = new SimpleOpenNI(this);
  kinect.enableDepth();
  kinect.enableUser(SimpleOpenNI.SKEL_PROFILE_ALL);
  size(1024, 768, OPENGL);
 
   if (!kinect.enableScene()) { 
    // if context.enableScene() returns false
    // then the Kinect is not working correctly
    // make sure the green light is blinking
    println("Kinect not connected!"); 
    exit();
    } else {
    // mirror the image to be more intuitive
    kinect.setMirror(true);
    // calculate the reScale value
    // currently it's rescaled to fill the complete width (cuts of top-bottom)
    // it's also possible to fill the complete height (leaves empty sides)
    reScale = (float) width / kinectWidth;
    // create a smaller blob image for speed and efficiency
    blobs = createImage(kinectWidth/3, kinectHeight/3, RGB);
    // initialize blob detection object to the blob image dimensions
    theBlobDetection = new BlobDetection(blobs.width, blobs.height);
    theBlobDetection.setThreshold(0.2);
    
    ////SKETCH1
      setupFlowfield();
    ////SKETCH3
      gfx = new ToxiclibsSupport(this);
      // setup box2d, create world, set gravity
      box2d = new PBox2D(this);
      box2d.createWorld();
      box2d.setGravity(0, -20);
  }

  hint(ENABLE_OPENGL_4X_SMOOTH);
  hint(DISABLE_OPENGL_2X_SMOOTH);
  pgl = (PGraphicsOpenGL) g;
  gl = pgl.gl;
  gl.glHint (gl.GL_LINE_SMOOTH_HINT, gl.GL_NICEST);
  gl.glEnable (gl.GL_LINE_SMOOTH);

  //SYPHON OBJECT 

  syphon = new syphon();

  //INIT SYPHON. THE SECOND PARAMETERS IS ANY NAME OF THE SERVER

  syphon.initSyphon(gl, "Syphon - Processing");
}
 
void draw()
{
      
  if (torso.x>-700 && torso.x<-183){  //extremo
      sketch2=false;
      sketch3=false;
      sketch1=true;  
      println("dentro");
  }
  else if(torso.x>-183 && torso.x<183){ //centro
      sketch3=false;
      sketch2=true;
      sketch1=false;
      println("dentro");
  }
  else if(torso.x>183 && torso.x<700){ //extremo
      sketch3=true;
      sketch2=false;
      sketch1=false;
      println("dentro"); 
  }else{
      println("fuera");   
  }
    
  /*if (mouseX>500){
      sketch3=true;
      sketch1=false;
    }else{
      sketch3=false;
      sketch1=true;
    }*/
  
    //MAPEANDO DISTANCIAS CUERPO
    //fill(255,0,0);
    float torsoXMap = map(torso.x,-860,720,0,1024);
    //ellipse(torsoXMap, 500, 20, 20);
    
    fill(0);
    leftHandXMap = map(leftHand.x,-1350,950,0,1024);
    leftHandYMap = map(leftHand.y,-400,930,500,0);
    ellipse(leftHandXMap, leftHandYMap, 20, 20);
    
    rightHandXMap = map(rightHand.x,-1100,1250,0,1024);
    rightHandYMap = map(rightHand.y,-400,930,500,0);
    ellipse(rightHandXMap, rightHandYMap, 20, 20);
    //println(leftHand.x);
    //FIN MAPEANDO DISTANCIAS CUERPO
  
  if(sketch1){
    noStroke();
    fill(0,0,0,30);
    rect(0, 0, width, height);
  }
  
  if(sketch2){
    background(0);
  }
  
  if(sketch3){
    background(0,0,0);
  }

  kinect.update();
  
  if(sketch1){
    //image(kinect.depthImage(), 0, 0);////something to send the depth image to max
  int[] userList = kinect.getUsers();
  for(int i=0;i<userList.length;i++)
  {
    if(kinect.isTrackingSkeleton(userList[i]))
     // drawSkeleton(userList[i]);
       getAndSendOSC(userList[i]);
          
  } 
  

    //**
    // put the image into a PImage
    cam = kinect.sceneImage().get();
    // copy the image into the smaller blob image
    blobs.copy(cam, 0, 0, cam.width, cam.height, 0, 0, blobs.width, blobs.height);
    // blur the blob image
    blobs.filter(BLUR);
    // detect the blobs
    theBlobDetection.computeBlobs(blobs.pixels);
    // clear the polygon (original functionality)
    poly.reset();
    // create the polygon from the blobs (custom functionality, see class)
    poly.createPolygon();
    drawFlowfield();
  }
  
  
  if(sketch2){  
    
     //image(kinect.depthImage(), 0, 0);////something to send the depth image to max
  int[] userList = kinect.getUsers();
  for(int i=0;i<userList.length;i++)
  {
    if(kinect.isTrackingSkeleton(userList[i]))
     // drawSkeleton(userList[i]);
       getAndSendOSC(userList[i]);      
  } 
    // put the image into a PImage
    cam = kinect.sceneImage().get();
    // copy the image into the smaller blob image
    blobs.copy(cam, 0, 0, cam.width, cam.height, 0, 0, blobs.width, blobs.height);
    // blur the blob image
    blobs.filter(BLUR, 1);
    // detect the blobs
    theBlobDetection.computeBlobs(blobs.pixels);
    // initialize a new polygon
    poly3y2 = new PolygonBlob3();
    // create the polygon from the blobs (custom functionality, see class)
    poly3y2.createPolygon();
    // create the box2d body from the polygon
    poly3y2.createBody();
    // update and draw everything (see method)
    updateAndDrawBox2Df();
    // destroy the person's body (important!)
    poly3y2.destroyBody();
    // set the colors randomly every 240th frame
  }
  
  
  if(sketch3){  
    
     //image(kinect.depthImage(), 0, 0);////something to send the depth image to max
  int[] userList = kinect.getUsers();
  for(int i=0;i<userList.length;i++)
  {
    if(kinect.isTrackingSkeleton(userList[i]))
     // drawSkeleton(userList[i]);
       getAndSendOSC(userList[i]);
  }
    
    // put the image into a PImage
    cam = kinect.sceneImage().get();
    // copy the image into the smaller blob image
    blobs.copy(cam, 0, 0, cam.width, cam.height, 0, 0, blobs.width, blobs.height);
    // blur the blob image
    blobs.filter(BLUR, 1);
    // detect the blobs
    theBlobDetection.computeBlobs(blobs.pixels);
    // initialize a new polygon
    poly3y2 = new PolygonBlob3();
    // create the polygon from the blobs (custom functionality, see class)
    poly3y2.createPolygon();
    // create the box2d body from the polygon
    poly3y2.createBody();
    // update and draw everything (see method)
    updateAndDrawBox2D();
    // destroy the person's body (important!)
    poly3y2.destroyBody();
    // set the colors randomly every 240th frame
  }
  
  syphon.renderTexture(pgl.gl);
}

/////SKETCH 2
void updateAndDrawBox2Df() {
  // if frameRate is sufficient, add a polygon and a circle with a random radius
  if (polygons.size()<150) {
    //polygons.add(new CustomShape(kinectWidth/2, -50, -1));
    polygons.add(new CustomShape(kinectWidth/2, -50, random(2.5, 15)));
  }
  // take one step in the box2d physics world
  box2d.step();
 
  // center and reScale from Kinect to custom dimensions
  translate(0, (height-kinectHeight*reScale)/2);
  scale(reScale);
 
  // display the person's polygon  
  noStroke();
  fill(255,255,255);
  gfx.polygon2D(poly3y2);
 
  // display all the shapes (circles, polygons)
  // go backwards to allow removal of shapes
  for (int i=polygons.size()-1; i>=0; i--) {
    CustomShape cs = polygons.get(i);
    // if the shape is off-screen remove it (see class for more info)
    if (cs.done()) {
      polygons.remove(i);
    // otherwise update (keep shape outside person) and display (circle or polygon)
    } else {
      cs.update();
      cs.display();
    }
  }
}
/////END SKETCH 2


/////SKETCH 3
void updateAndDrawBox2D() {  
  // take one step in the box2d physics world
  box2d.step();
 
  // center and reScale from Kinect to custom dimensions
  translate(0, (height-kinectHeight*reScale)/2);
  scale(reScale);
 
  // display the person's polygon  
  noStroke();
  fill(255,255,255);
  gfx.polygon2D(poly3y2);
}
/////END SKETCH 3


/////SKETCH 1
void drawFlowfield() {
  // center and reScale from Kinect to custom dimensions
  translate(0, (height-kinectHeight*reScale)/2);
  scale(reScale);
  // set global variables that influence the particle flow's movement
  globalX = noise(frameCount * 0.01) * width/2 + width/4;
  globalY = noise(frameCount * 0.005 + 5) * height;
  // update and display all particles in the flow
  for (Particle p : flow) {
    p.updateAndDisplay();
  }
}



 
void getAndSendOSC(int userID)
{
  getJoint(userID, SimpleOpenNI.SKEL_HEAD, head);
  getJoint(userID, SimpleOpenNI.SKEL_NECK, neck);
  getJoint(userID, SimpleOpenNI.SKEL_LEFT_SHOULDER, leftShoulder);
  getJoint(userID, SimpleOpenNI.SKEL_LEFT_ELBOW, leftElbow);
  getJoint(userID, SimpleOpenNI.SKEL_RIGHT_SHOULDER, rightShoulder);
  getJoint(userID, SimpleOpenNI.SKEL_RIGHT_ELBOW, rightElbow);
  getJoint(userID, SimpleOpenNI.SKEL_TORSO, torso);
  getJoint(userID, SimpleOpenNI.SKEL_LEFT_KNEE, leftKnee);
  getJoint(userID, SimpleOpenNI.SKEL_RIGHT_HIP, rightHip);
  getJoint(userID, SimpleOpenNI.SKEL_LEFT_FOOT, leftFoot);
  getJoint(userID, SimpleOpenNI.SKEL_RIGHT_KNEE, leftKnee);
  getJoint(userID, SimpleOpenNI.SKEL_LEFT_HIP, leftHip);
  getJoint(userID, SimpleOpenNI.SKEL_RIGHT_FOOT, rightFoot);
  getJoint(userID, SimpleOpenNI.SKEL_RIGHT_HAND, rightHand);
  getJoint(userID, SimpleOpenNI.SKEL_LEFT_HAND, leftHand);
   
  skeletonData = new OscMessage("/head");
  skeletonData.add(head.x);
  skeletonData.add(head.y);
  skeletonData.add(head.z);
  oscP5.send(skeletonData, MaxAddress);
   
  skeletonData = new OscMessage("/neck");
  skeletonData.add(neck.x);
  skeletonData.add(neck.y);
  skeletonData.add(neck.z);
  oscP5.send(skeletonData, MaxAddress);
   
  skeletonData = new OscMessage("/rightShoulder");
  skeletonData.add(rightShoulder.x);
  skeletonData.add(rightShoulder.y);
  skeletonData.add(rightShoulder.z);
  oscP5.send(skeletonData, MaxAddress);
   
  skeletonData = new OscMessage("/leftShoulder");
  skeletonData.add(leftShoulder.x);
  skeletonData.add(leftShoulder.y);
  skeletonData.add(leftShoulder.z);
  oscP5.send(skeletonData, MaxAddress);
   
  skeletonData = new OscMessage("/rightElbow");
  skeletonData.add(rightElbow.x);
  skeletonData.add(rightElbow.y);
  skeletonData.add(rightElbow.z);
  oscP5.send(skeletonData, MaxAddress);
   
  skeletonData = new OscMessage("/leftElbow");
  skeletonData.add(leftElbow.x);
  skeletonData.add(leftElbow.y);
  skeletonData.add(leftElbow.z);
  oscP5.send(skeletonData, MaxAddress);
   
  skeletonData = new OscMessage("/rightHand");
  skeletonData.add(rightHand.x);
  skeletonData.add(rightHand.y);
  //skeletonData.add(rightHandXMap);
  //skeletonData.add(rightHandYMap);
  skeletonData.add(rightHand.z);
  oscP5.send(skeletonData, MaxAddress);
  //println(rightHand.x);
   
  skeletonData = new OscMessage("/leftHand");
  skeletonData.add(leftHand.x);
  skeletonData.add(leftHand.y);
  //skeletonData.add(leftHandXMap);
  //skeletonData.add(leftHandYMap);
  skeletonData.add(leftHand.z);
  oscP5.send(skeletonData, MaxAddress);
   
  skeletonData = new OscMessage("/torso");
  skeletonData.add(torso.x);
  skeletonData.add(torso.y);
  skeletonData.add(torso.z);
  oscP5.send(skeletonData, MaxAddress);
   
  skeletonData = new OscMessage("/leftHip");
  skeletonData.add(leftHip.x);
  skeletonData.add(leftHip.y);
  skeletonData.add(leftHip.z);
  oscP5.send(skeletonData, MaxAddress);
   
  skeletonData = new OscMessage("/rightHip");
  skeletonData.add(rightHip.x);
  skeletonData.add(rightHip.y);
  skeletonData.add(rightHip.z);
  oscP5.send(skeletonData, MaxAddress);
   
  skeletonData = new OscMessage("/leftKnee");
  skeletonData.add(leftKnee.x);
  skeletonData.add(leftKnee.y);
  skeletonData.add(leftKnee.z);
  oscP5.send(skeletonData, MaxAddress);
   
  skeletonData = new OscMessage("/rightKnee");
  skeletonData.add(rightKnee.x);
  skeletonData.add(rightKnee.y);
  skeletonData.add(rightKnee.z);
  oscP5.send(skeletonData, MaxAddress);
   
  skeletonData = new OscMessage("/leftFoot");
  skeletonData.add(leftFoot.x);
  skeletonData.add(leftFoot.y);
  skeletonData.add(leftFoot.z);
  oscP5.send(skeletonData, MaxAddress);
   
  skeletonData = new OscMessage("/rightFoot");
  skeletonData.add(rightFoot.x);
  skeletonData.add(rightFoot.y);
  skeletonData.add(rightFoot.z);
  oscP5.send(skeletonData, MaxAddress);
   
}
 
void getJoint(int userID, int jointID, PVector jointName)
{
  confidence = kinect.getJointPositionSkeleton(userID, jointID, jnt);
  if(confidence == 0.)
  {
    return;
  }
  jointName.set(jnt); 
}
 
void drawJoint(int userID, int jointID)
{
 
  PVector joint = new PVector(); 
 
  float confidence = kinect.getJointPositionSkeleton(userID, jointID, joint);
 
  if(confidence < 0.5)
  {
 
    return;
 
  }
   
  PVector convertedJoint = new PVector();
 
  kinect.convertRealWorldToProjective(joint, convertedJoint);
 
  ellipse(convertedJoint.x, convertedJoint.y, 5, 5);
 
}
 
// user-tracking callbacks!
// -----------------------------------------------------------------
// SimpleOpenNI events

void onNewUser(int userId)
{
  println("onNewUser - userId: " + userId);
  println("  start pose detection");
  //usuario_lost = 1;
  //skeletonData.add(usuario_lost);
 // oscP5.send(skeletonData, MaxAddress);
  if(autoCalib)
    kinect.requestCalibrationSkeleton(userId,true);
  else    
    kinect.startPoseDetection("Psi",userId);
}

void onLostUser(int userId)
{
  println("onLostUser - userId: " + userId);
}

void onExitUser(int userId)
{
  println("onExitUser - userId: " + userId);

}

void onReEnterUser(int userId)
{
  println("onReEnterUser - userId: " + userId);
  //usuario_lost = 1;
  //skeletonData = new OscMessage("/lost");
  //skeletonData.add(usuario_lost);
  //oscP5.send(skeletonData, MaxAddress);
}

void onStartCalibration(int userId)
{
  println("onStartCalibration - userId: " + userId);
 // usuario_lost = 1;
  //skeletonData = new OscMessage("/lost");
  //skeletonData.add(usuario_lost);
  //oscP5.send(skeletonData, MaxAddress);
}

void onEndCalibration(int userId, boolean successfull)
{
  println("onEndCalibration - userId: " + userId + ", successfull: " + successfull);
  
  if (successfull) 
  { 
    println("  User calibrated !!!");
    kinect.startTrackingSkeleton(userId);
    //usuario_lost = 1; 
    //skeletonData = new OscMessage("/lost");
    //skeletonData.add(usuario_lost);
    //oscP5.send(skeletonData, MaxAddress);
  } 
  else 
  { 
    println("  Failed to calibrate user !!!");
    println("  Start pose detection");
    kinect.startPoseDetection("Psi",userId);
   //     usuario_lost = 0; 
   // skeletonData = new OscMessage("/lost");
   // skeletonData.add(usuario_lost);
   // oscP5.send(skeletonData, MaxAddress);
  }
}

void onStartPose(String pose,int userId)
{
  println("onStartPose - userId: " + userId + ", pose: " + pose);
  println(" stop pose detection");
  
  kinect.stopPoseDetection(userId); 
  kinect.requestCalibrationSkeleton(userId, true);
 
}

void onEndPose(String pose,int userId)
{
  println("onEndPose - userId: " + userId + ", pose: " + pose);
}

 
void drawSkeleton(int userID)
{
  stroke(0);
  strokeWeight(5);
  kinect.drawLimb(userID, SimpleOpenNI.SKEL_HEAD, SimpleOpenNI.SKEL_NECK);
  kinect.drawLimb(userID, SimpleOpenNI.SKEL_NECK, SimpleOpenNI.SKEL_LEFT_SHOULDER);
  kinect.drawLimb(userID, SimpleOpenNI.SKEL_LEFT_SHOULDER, SimpleOpenNI.SKEL_LEFT_ELBOW);
  kinect.drawLimb(userID, SimpleOpenNI.SKEL_LEFT_ELBOW, SimpleOpenNI.SKEL_LEFT_HAND);
  kinect.drawLimb(userID, SimpleOpenNI.SKEL_NECK, SimpleOpenNI.SKEL_RIGHT_SHOULDER);
  kinect.drawLimb(userID, SimpleOpenNI.SKEL_RIGHT_SHOULDER, SimpleOpenNI.SKEL_RIGHT_ELBOW);
  kinect.drawLimb(userID, SimpleOpenNI.SKEL_RIGHT_ELBOW, SimpleOpenNI.SKEL_RIGHT_HAND);
  kinect.drawLimb(userID, SimpleOpenNI.SKEL_LEFT_SHOULDER, SimpleOpenNI.SKEL_TORSO);
  kinect.drawLimb(userID, SimpleOpenNI.SKEL_RIGHT_SHOULDER, SimpleOpenNI.SKEL_TORSO);
  kinect.drawLimb(userID, SimpleOpenNI.SKEL_TORSO, SimpleOpenNI.SKEL_LEFT_HIP);
  kinect.drawLimb(userID, SimpleOpenNI.SKEL_LEFT_HIP, SimpleOpenNI.SKEL_LEFT_KNEE);
  kinect.drawLimb(userID, SimpleOpenNI.SKEL_LEFT_KNEE, SimpleOpenNI.SKEL_LEFT_FOOT);
  kinect.drawLimb(userID, SimpleOpenNI.SKEL_TORSO, SimpleOpenNI.SKEL_RIGHT_HIP);
  kinect.drawLimb(userID, SimpleOpenNI.SKEL_RIGHT_HIP, SimpleOpenNI.SKEL_RIGHT_KNEE);
  kinect.drawLimb(userID, SimpleOpenNI.SKEL_RIGHT_KNEE, SimpleOpenNI.SKEL_RIGHT_FOOT);
  kinect.drawLimb(userID, SimpleOpenNI.SKEL_RIGHT_HIP, SimpleOpenNI.SKEL_LEFT_HIP);
 
  noStroke();
  fill(255,0,0);
  drawJoint(userID, SimpleOpenNI.SKEL_HEAD);
  drawJoint(userID, SimpleOpenNI.SKEL_NECK);
  drawJoint(userID, SimpleOpenNI.SKEL_LEFT_SHOULDER);
  drawJoint(userID, SimpleOpenNI.SKEL_LEFT_ELBOW);
  drawJoint(userID, SimpleOpenNI.SKEL_RIGHT_SHOULDER);
  drawJoint(userID, SimpleOpenNI.SKEL_RIGHT_ELBOW);
  drawJoint(userID, SimpleOpenNI.SKEL_TORSO);
  drawJoint(userID, SimpleOpenNI.SKEL_LEFT_KNEE);
  drawJoint(userID, SimpleOpenNI.SKEL_RIGHT_HIP);
  drawJoint(userID, SimpleOpenNI.SKEL_LEFT_FOOT);
  drawJoint(userID, SimpleOpenNI.SKEL_RIGHT_KNEE);
  drawJoint(userID, SimpleOpenNI.SKEL_LEFT_HIP);
  drawJoint(userID, SimpleOpenNI.SKEL_RIGHT_FOOT);
  drawJoint(userID, SimpleOpenNI.SKEL_RIGHT_HAND);
  drawJoint(userID, SimpleOpenNI.SKEL_LEFT_HAND);
}

void setupFlowfield() {
  // set stroke weight (for particle display) to 2.5
  strokeWeight(2);
  // initialize all particles in the flow
  for(int i=0; i<flow.length; i++) {
    flow[i] = new Particle(i/10000.0);
  }
  // set all colors randomly now
  //setRandomColors(1);
}


//void setRandomColors(int nthFrame) {
//  if (frameCount % nthFrame == 0) {
//    // turn a palette into a series of strings
//    String[] paletteStrings = split(palettes[int(random(palettes.length))], ",");
//    // turn strings into colors
//    color[] colorPalette = new color[paletteStrings.length];
//    for (int i=0; i<paletteStrings.length; i++) {
//      colorPalette[i] = int(paletteStrings[i]);
//    }
//    // set background color to first color from palette
//    //bgColor = colorPalette[0];//blanco, negro y rosa
//    bgColor = #00ff00;
//    // set all particle colors randomly to color from palette (excluding first aka background color)
//    for (int i=0; i<flow.length; i++) {
//      flow[i].col = colorPalette[int(random(1, colorPalette.length))];
//    }
//  }
//}
