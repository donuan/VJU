import processing.core.*; 
import processing.xml.*; 

import SimpleOpenNI.*; 
import oscP5.*; 
import netP5.*; 
import blobDetection.*; 
import java.util.*; 
import processing.opengl.*; 
import java.awt.Polygon; 
import jsyphon.*; 
import javax.media.opengl.*; 
import toxi.geom.*; 
import toxi.processing.*; 
import pbox2d.*; 
import org.jbox2d.collision.shapes.*; 
import org.jbox2d.common.*; 
import org.jbox2d.dynamics.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class sketch_8_Particles2yBlobyFisica_working extends PApplet {


SimpleOpenNI  kinect;


 // blobs

 // opengl

 // Syphon

 // toxiclibs shapes and vectors
 // toxiclibs display
 // shiffman's jbox2d helper library
 // jbox2d
 // jbox2d
 // jbox2d
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
int bgColor, blobColor;
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
public void setup()
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
    theBlobDetection.setThreshold(0.2f);
    
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
 
public void draw()
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
public void updateAndDrawBox2Df() {
  // if frameRate is sufficient, add a polygon and a circle with a random radius
  if (polygons.size()<150) {
    //polygons.add(new CustomShape(kinectWidth/2, -50, -1));
    polygons.add(new CustomShape(kinectWidth/2, -50, random(2.5f, 15)));
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
public void updateAndDrawBox2D() {  
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
public void drawFlowfield() {
  // center and reScale from Kinect to custom dimensions
  translate(0, (height-kinectHeight*reScale)/2);
  scale(reScale);
  // set global variables that influence the particle flow's movement
  globalX = noise(frameCount * 0.01f) * width/2 + width/4;
  globalY = noise(frameCount * 0.005f + 5) * height;
  // update and display all particles in the flow
  for (Particle p : flow) {
    p.updateAndDisplay();
  }
}



 
public void getAndSendOSC(int userID)
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
 
public void getJoint(int userID, int jointID, PVector jointName)
{
  confidence = kinect.getJointPositionSkeleton(userID, jointID, jnt);
  if(confidence == 0.f)
  {
    return;
  }
  jointName.set(jnt); 
}
 
public void drawJoint(int userID, int jointID)
{
 
  PVector joint = new PVector(); 
 
  float confidence = kinect.getJointPositionSkeleton(userID, jointID, joint);
 
  if(confidence < 0.5f)
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

public void onNewUser(int userId)
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

public void onLostUser(int userId)
{
  println("onLostUser - userId: " + userId);
}

public void onExitUser(int userId)
{
  println("onExitUser - userId: " + userId);

}

public void onReEnterUser(int userId)
{
  println("onReEnterUser - userId: " + userId);
  //usuario_lost = 1;
  //skeletonData = new OscMessage("/lost");
  //skeletonData.add(usuario_lost);
  //oscP5.send(skeletonData, MaxAddress);
}

public void onStartCalibration(int userId)
{
  println("onStartCalibration - userId: " + userId);
 // usuario_lost = 1;
  //skeletonData = new OscMessage("/lost");
  //skeletonData.add(usuario_lost);
  //oscP5.send(skeletonData, MaxAddress);
}

public void onEndCalibration(int userId, boolean successfull)
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

public void onStartPose(String pose,int userId)
{
  println("onStartPose - userId: " + userId + ", pose: " + pose);
  println(" stop pose detection");
  
  kinect.stopPoseDetection(userId); 
  kinect.requestCalibrationSkeleton(userId, true);
 
}

public void onEndPose(String pose,int userId)
{
  println("onEndPose - userId: " + userId + ", pose: " + pose);
}

 
public void drawSkeleton(int userID)
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

public void setupFlowfield() {
  // set stroke weight (for particle display) to 2.5
  strokeWeight(2);
  // initialize all particles in the flow
  for(int i=0; i<flow.length; i++) {
    flow[i] = new Particle(i/10000.0f);
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
// usually one would probably make a generic Shape class and subclass different types (circle, polygon), but that
// would mean at least 3 instead of 1 class, so for this tutorial it's a combi-class CustomShape for all types of shapes
// to save some space and keep the code as concise as possible I took a few shortcuts to prevent repeating the same code
class CustomShape {
  // to hold the box2d body
  Body body;
  // to hold the Toxiclibs polygon shape
  Polygon2D toxiPoly;
  // custom color for each shape
  int col = 255;
  // radius (also used to distinguish between circles and polygons in this combi-class
  float r;

  CustomShape(float x, float y, float r) {
    this.r = r;
    // create a body (polygon or circle based on the r)
    makeBody(x, y);
    // get a random color
    //col = getRandomColor();
  }

  public void makeBody(float x, float y) {
    // define a dynamic body positioned at xy in box2d world coordinates,
    // create it and set the initial values for this box2d body's speed and angle
    BodyDef bd = new BodyDef();
    bd.type = BodyType.DYNAMIC;
    bd.position.set(box2d.coordPixelsToWorld(new Vec2(x, y)));
    body = box2d.createBody(bd);
    body.setLinearVelocity(new Vec2(random(-8, 8), random(2, 8)));
    body.setAngularVelocity(random(-5, 5));
    
    // depending on the r this combi-code creates either a box2d polygon or a circle
    if (r == -1) {
      // box2d polygon shape
      PolygonShape sd = new PolygonShape();
      // toxiclibs polygon creator (triangle, square, etc)
      toxiPoly = new Circle(random(5, 20)).toPolygon2D(PApplet.parseInt(random(3, 6)));
      // place the toxiclibs polygon's vertices into a vec2d array
      Vec2[] vertices = new Vec2[toxiPoly.getNumPoints()];
      for (int i=0; i<vertices.length; i++) {
        Vec2D v = toxiPoly.vertices.get(i);
        vertices[i] = box2d.vectorPixelsToWorld(new Vec2(v.x, v.y));
      }
      // put the vertices into the box2d shape
      sd.set(vertices, vertices.length);
      // create the fixture from the shape (deflect things based on the actual polygon shape)
      body.createFixture(sd, 1);
    } else {
      // box2d circle shape of radius r
      CircleShape cs = new CircleShape();
      cs.m_radius = box2d.scalarPixelsToWorld(r);
      // tweak the circle's fixture def a little bit
      FixtureDef fd = new FixtureDef();
      fd.shape = cs;
      fd.density = 1;
      fd.friction = 0.01f;
      fd.restitution = 0.3f;
      // create the fixture from the shape's fixture def (deflect things based on the actual circle shape)
      body.createFixture(fd);
    }
  }

  // method to loosely move shapes outside a person's polygon
  // (alternatively you could allow or remove shapes inside a person's polygon)
  public void update() {
    // get the screen position from this shape (circle of polygon)
    Vec2 posScreen = box2d.getBodyPixelCoord(body);
    // turn it into a toxiclibs Vec2D
    Vec2D toxiScreen = new Vec2D(posScreen.x, posScreen.y);
    // check if this shape's position is inside the person's polygon
    boolean inBody = poly3y2.containsPoint(toxiScreen);
    // if a shape is inside the person
    if (inBody) {
      // find the closest point on the polygon to the current position
      Vec2D closestPoint = toxiScreen;
      float closestDistance = 9999999;
      for (Vec2D v : poly3y2.vertices) {
        float distance = v.distanceTo(toxiScreen);
        if (distance < closestDistance) {
          closestDistance = distance;
          closestPoint = v;
        }
      }
      // create a box2d position from the closest point on the polygon
      Vec2 contourPos = new Vec2(closestPoint.x, closestPoint.y);
      Vec2 posWorld = box2d.coordPixelsToWorld(contourPos);
      float angle = body.getAngle();
      // set the box2d body's position of this CustomShape to the new position (use the current angle)
      body.setTransform(posWorld, angle);
    }
  }

  // display the customShape
  public void display() {
    // get the pixel coordinates of the body
    Vec2 pos = box2d.getBodyPixelCoord(body);
    pushMatrix();
    // translate to the position
    translate(pos.x, pos.y);
    noStroke();
    // use the shape's custom color
    fill(col);
    // depending on the r this combi-code displays either a polygon or a circle
    if (r == -1) {
      // rotate by the body's angle
      float a = body.getAngle();
      rotate(-a); // minus!
      gfx.polygon2D(toxiPoly);
    } else {
      ellipse(0, 0, r*2, r*2);
    }
    popMatrix();
  }

  // if the shape moves off-screen, destroy the box2d body (important!)
  // and return true (which will lead to the removal of this CustomShape object)
  public boolean done() {
    Vec2 posScreen = box2d.getBodyPixelCoord(body);
    boolean offscreen = posScreen.y > height;
    if (offscreen) {
      box2d.destroyBody(body);
      return true;
    }
    return false;
  }
}
// a basic noise-based moving particle
class Particle {
  // unique id, (previous) position, speed
  float id, x, y, xp, yp, s, d;
  int r, g, b;// color
  float linesize;

  Particle(float id) {
    this.id = id;
    s = random(2, 6); // speed
  }
  
  public void updateAndDisplay() {
    // let it flow, end with a new x and y position
    id += 0.01f;
    d = (noise(id, x/globalY, y/globalY)-0.5f)*globalX;
    x += cos(radians(d))*s;
    y += sin(radians(d))*s;
 
    // constrain to boundaries
    if (x<-10) x=xp=kinectWidth+10;
    if (x>kinectWidth+10) x=xp=-10;
    if (y<-10) y=yp=kinectHeight+10;
    if (y>kinectHeight+10) y=yp=-10;
 
    // if there is a polygon (more than 0 points)
    if (poly.npoints > 0) {
      // if this particle is outside the polygon
      if (!poly.contains(x, y)) {
        // while it is outside the polygon
        while(!poly.contains(x, y)) {
          // randomize x and y
          x = random(kinectWidth);
          y = random(kinectHeight);
        }
        // set previous x and y, to this x and y
        xp=x;
        yp=y;
      }
    }
    
      
      if(rightHandXMap < 500 && rightHandYMap > 250 || leftHandXMap < 500 && leftHandYMap > 250){
        r = PApplet.parseInt(random(255));
        g = PApplet.parseInt(random(255));
        b = PApplet.parseInt(random(255)); 
      }else if(rightHandXMap > 500 && rightHandYMap > 250 || leftHandXMap > 500 && leftHandYMap > 250){
        r = 255;
        g = 0;
        b = PApplet.parseInt(rightHandYMap);
      }else{
        r = PApplet.parseInt(leftHandYMap);
        g = 0;
        b = 255;
      }
      
      /*if(rightHandYMap < 250){
        r = int(rightHandYMap);
        g = 0;
        b = 0;
        println("rightHandYMap < 250");
      }
    
      if(leftHandYMap < 250){
        r = 0;
        g = 0;
        b = int(leftHandYMap);
        println("leftHandYMap < 250");
      } */
      
    linesize = map(rightHandYMap,500,0,0,6);
    
    //strokeWeight(4);
    //stroke(0);
    //line(xp, yp, x, y);
    // individual particle color
    //strokeWeight(2.5);
    stroke(r,g,b);
    // line from previous to current position
    line(xp, yp, x+linesize, y+linesize);
    
    
    
    // set previous to current position
    xp=x;
    yp=y;
  }
}
// an extended polygon class with my own customized createPolygon() method (feel free to improve!)
class PolygonBlob extends Polygon {
 
  // took me some time to make this method fully self-sufficient
  // now it works quite well in creating a correct polygon from a person's blob
  // of course many thanks to v3ga, because the library already does a lot of the work
  public void createPolygon() {
    // an arrayList... of arrayLists... of PVectors
    // the arrayLists of PVectors are basically the person's contours (almost but not completely in a polygon-correct order)
    ArrayList<ArrayList<PVector>> contours = new ArrayList<ArrayList<PVector>>();
    // helpful variables to keep track of the selected contour and point (start/end point)
    int selectedContour = 0;
    int selectedPoint = 0;
 
    // create contours from blobs
    // go over all the detected blobs
    for (int n=0 ; n<theBlobDetection.getBlobNb(); n++) {
      Blob b = theBlobDetection.getBlob(n);
      // for each substantial blob...
      if (b != null && b.getEdgeNb() > 100) {
        // create a new contour arrayList of PVectors
        ArrayList<PVector> contour = new ArrayList<PVector>();
        // go over all the edges in the blob
        for (int m=0; m<b.getEdgeNb(); m++) {
          // get the edgeVertices of the edge
          EdgeVertex eA = b.getEdgeVertexA(m);
          EdgeVertex eB = b.getEdgeVertexB(m);
          // if both ain't null...
          if (eA != null && eB != null) {
            // get next and previous edgeVertexA
            EdgeVertex fn = b.getEdgeVertexA((m+1) % b.getEdgeNb());
            EdgeVertex fp = b.getEdgeVertexA((max(0, m-1)));
            // calculate distance between vertexA and next and previous edgeVertexA respectively
            // positions are multiplied by kinect dimensions because the blob library returns normalized values
            float dn = dist(eA.x*kinectWidth, eA.y*kinectHeight, fn.x*kinectWidth, fn.y*kinectHeight);
            float dp = dist(eA.x*kinectWidth, eA.y*kinectHeight, fp.x*kinectWidth, fp.y*kinectHeight);
            // if either distance is bigger than 15
            if (dn > 15 || dp > 15) {
              // if the current contour size is bigger than zero
              if (contour.size() > 0) {
                // add final point
                contour.add(new PVector(eB.x*kinectWidth, eB.y*kinectHeight));
                // add current contour to the arrayList
                contours.add(contour);
                // start a new contour arrayList
                contour = new ArrayList<PVector>();
              // if the current contour size is 0 (aka it's a new list)
              } else {
                // add the point to the list
                contour.add(new PVector(eA.x*kinectWidth, eA.y*kinectHeight));
              }
            // if both distance are smaller than 15 (aka the points are close)  
            } else {
              // add the point to the list
              contour.add(new PVector(eA.x*kinectWidth, eA.y*kinectHeight));
            }
          }
        }
      }
    }
    
    // at this point in the code we have a list of contours (aka an arrayList of arrayLists of PVectors)
    // now we need to sort those contours into a correct polygon. To do this we need two things:
    // 1. The correct order of contours
    // 2. The correct direction of each contour
 
    // as long as there are contours left...    
    while (contours.size() > 0) {
      
      // find next contour
      float distance = 999999999;
      // if there are already points in the polygon
      if (npoints > 0) {
        // use the polygon's last point as a starting point
        PVector lastPoint = new PVector(xpoints[npoints-1], ypoints[npoints-1]);
        // go over all contours
        for (int i=0; i<contours.size(); i++) {
          ArrayList<PVector> c = contours.get(i);
          // get the contour's first point
          PVector fp = c.get(0);
          // get the contour's last point
          PVector lp = c.get(c.size()-1);
          // if the distance between the current contour's first point and the polygon's last point is smaller than distance
          if (fp.dist(lastPoint) < distance) {
            // set distance to this distance
            distance = fp.dist(lastPoint);
            // set this as the selected contour
            selectedContour = i;
            // set selectedPoint to 0 (which signals first point)
            selectedPoint = 0;
          }
          // if the distance between the current contour's last point and the polygon's last point is smaller than distance
          if (lp.dist(lastPoint) < distance) {
            // set distance to this distance
            distance = lp.dist(lastPoint);
            // set this as the selected contour
            selectedContour = i;
            // set selectedPoint to 1 (which signals last point)
            selectedPoint = 1;
          }
        }
      // if the polygon is still empty
      } else {
        // use a starting point in the lower-right
        PVector closestPoint = new PVector(width, height);
        // go over all contours
        for (int i=0; i<contours.size(); i++) {
          ArrayList<PVector> c = contours.get(i);
          // get the contour's first point
          PVector fp = c.get(0);
          // get the contour's last point
          PVector lp = c.get(c.size()-1);
          // if the first point is in the lowest 5 pixels of the (kinect) screen and more to the left than the current closestPoint
          if (fp.y > kinectHeight-5 && fp.x < closestPoint.x) {
            // set closestPoint to first point
            closestPoint = fp;
            // set this as the selected contour
            selectedContour = i;
            // set selectedPoint to 0 (which signals first point)
            selectedPoint = 0;
          }
          // if the last point is in the lowest 5 pixels of the (kinect) screen and more to the left than the current closestPoint
          if (lp.y > kinectHeight-5 && lp.x < closestPoint.y) {
            // set closestPoint to last point
            closestPoint = lp;
            // set this as the selected contour
            selectedContour = i;
            // set selectedPoint to 1 (which signals last point)
            selectedPoint = 1;
          }
        }
      }
 
      // add contour to polygon
      ArrayList<PVector> contour = contours.get(selectedContour);
      // if selectedPoint is bigger than zero (aka last point) then reverse the arrayList of points
      if (selectedPoint > 0) { Collections.reverse(contour); }
      // add all the points in the contour to the polygon
      for (PVector p : contour) {
        addPoint(PApplet.parseInt(p.x), PApplet.parseInt(p.y));
      }
      // remove this contour from the list of contours
      contours.remove(selectedContour);
      // the while loop above makes all of this code loop until the number of contours is zero
      // at that time all the points in all the contours have been added to the polygon... in the correct order (hopefully)
    }
  }
}
// an extended polygon class quite similar to the earlier PolygonBlob class (but extending Toxiclibs' Polygon2D class instead)
// The main difference is that this one is able to create (and destroy) a box2d body from it's own shape
class PolygonBlob3 extends Polygon2D {
  // to hold the box2d body
  Body body;
 
  // the createPolygon() method is nearly identical to the one presented earlier
  // see the Kinect Flow Example for a more detailed description of this method (again, feel free to improve it)
  public void createPolygon() {
    ArrayList<ArrayList<PVector>> contours = new ArrayList<ArrayList<PVector>>();
    int selectedContour = 0;
    int selectedPoint = 0;
 
    // create contours from blobs
    for (int n=0 ; n<theBlobDetection.getBlobNb(); n++) {
      Blob b = theBlobDetection.getBlob(n);
      if (b != null && b.getEdgeNb() > 100) {
        ArrayList<PVector> contour = new ArrayList<PVector>();
        for (int m=0; m<b.getEdgeNb(); m++) {
          EdgeVertex eA = b.getEdgeVertexA(m);
          EdgeVertex eB = b.getEdgeVertexB(m);
          if (eA != null && eB != null) {
            EdgeVertex fn = b.getEdgeVertexA((m+1) % b.getEdgeNb());
            EdgeVertex fp = b.getEdgeVertexA((max(0, m-1)));
            float dn = dist(eA.x*kinectWidth, eA.y*kinectHeight, fn.x*kinectWidth, fn.y*kinectHeight);
            float dp = dist(eA.x*kinectWidth, eA.y*kinectHeight, fp.x*kinectWidth, fp.y*kinectHeight);
            if (dn > 15 || dp > 15) {
              if (contour.size() > 0) {
                contour.add(new PVector(eB.x*kinectWidth, eB.y*kinectHeight));
                contours.add(contour);
                contour = new ArrayList<PVector>();
              } else {
                contour.add(new PVector(eA.x*kinectWidth, eA.y*kinectHeight));
              }
            } else {
              contour.add(new PVector(eA.x*kinectWidth, eA.y*kinectHeight));
            }
          }
        }
      }
    }
    
    while (contours.size() > 0) {
      
      // find next contour
      float distance = 999999999;
      if (getNumPoints() > 0) {
        Vec2D vecLastPoint = vertices.get(getNumPoints()-1);
        PVector lastPoint = new PVector(vecLastPoint.x, vecLastPoint.y);
        for (int i=0; i<contours.size(); i++) {
          ArrayList<PVector> c = contours.get(i);
          PVector fp = c.get(0);
          PVector lp = c.get(c.size()-1);
          if (fp.dist(lastPoint) < distance) { 
            distance = fp.dist(lastPoint); 
            selectedContour = i; 
            selectedPoint = 0;
          }
          if (lp.dist(lastPoint) < distance) { 
            distance = lp.dist(lastPoint); 
            selectedContour = i; 
            selectedPoint = 1;
          }
        }
      } else {
        PVector closestPoint = new PVector(width, height);
        for (int i=0; i<contours.size(); i++) {
          ArrayList<PVector> c = contours.get(i);
          PVector fp = c.get(0);
          PVector lp = c.get(c.size()-1);
          if (fp.y > kinectHeight-5 && fp.x < closestPoint.x) { 
            closestPoint = fp; 
            selectedContour = i; 
            selectedPoint = 0;
          }
          if (lp.y > kinectHeight-5 && lp.x < closestPoint.y) { 
            closestPoint = lp; 
            selectedContour = i; 
            selectedPoint = 1;
          }
        }
      }
 
      // add contour to polygon
      ArrayList<PVector> contour = contours.get(selectedContour);
      if (selectedPoint > 0) { Collections.reverse(contour); }
      for (PVector p : contour) {
        add(new Vec2D(p.x, p.y));
      }
      contours.remove(selectedContour);
    }
  }
 
  // creates a shape-deflecting physics chain in the box2d world from this polygon
  public void createBody() {
    // for stability the body is always created (and later destroyed)
    BodyDef bd = new BodyDef();
    body = box2d.createBody(bd);
    // if there are more than 0 points (aka a person on screen)...
    if (getNumPoints() > 0) {
      // create a vec2d array of vertices in box2d world coordinates from this polygon
      Vec2[] verts = new Vec2[getNumPoints()];
      for (int i=0; i<getNumPoints(); i++) {
        Vec2D v = vertices.get(i);
        verts[i] = box2d.coordPixelsToWorld(v.x, v.y);
      }
      // create a chain from the array of vertices
      ChainShape chain = new ChainShape();
      chain.createChain(verts, verts.length);
      // create fixture in body from the chain (this makes it actually deflect other shapes)
      body.createFixture(chain, 1);
    }
  }
 
  // destroy the box2d body (important!)
  public void destroyBody() {
    box2d.destroyBody(body);
  }
}
class syphon {
  JSyphonServer mySyphon;
  int[] texID;

  public void initSyphon(GL gl, String theName) {
    if (mySyphon!=null) {
      // in case you are using
      //  hint(DISABLE_OPENGL_2X_SMOOTH); or hint(ENABLE_OPENGL_4X_SMOOTH);
      // setup will be called a second or third time and consequently initSyphon(), too.
      // Therefore, in case a Syphon server is running, we stop it here, and
      // inform the listening clients to remove the server from their render list.
      // in the next step then we create a new server.
      mySyphon.stop();
    }
    mySyphon = new JSyphonServer();
    //mySyphon.test();
    mySyphon.initWithName(theName);

    // copy to texture, to send to Syphon.
    texID = new int[1];

    gl.glGenTextures(1, texID, 0);
    gl.glBindTexture(gl.GL_TEXTURE_RECTANGLE_EXT, texID[0]);
    gl.glTexImage2D(gl.GL_TEXTURE_RECTANGLE_EXT, 0, gl.GL_RGBA8, width, height, 0, gl.GL_RGBA, gl.GL_UNSIGNED_BYTE, null);
  } 

  public void renderTexture(GL gl) {
    gl.glBindTexture(gl.GL_TEXTURE_RECTANGLE_EXT, texID[0]);
    gl.glCopyTexSubImage2D(gl.GL_TEXTURE_RECTANGLE_EXT, 0, 0, 0, 0, 0, width, height); 
    mySyphon.publishFrameTexture(texID[0], gl.GL_TEXTURE_RECTANGLE_EXT, 0, 0, width, height, width, height, false);
  }

  public void stop() {
    // processing 1.5 does not override and call stop anymore.
    // looking at the PApplet source code, it says "this [super()] used to shut down the sketch, 
    // but that code has been moved to dispose()", so we use dispose() instead of stop()
    // for procesing pre 1.5 call dispose() from here
    dispose();
  }

  public void dispose() {
    // if a syphon server is not stopped when the sketch is closed, the server would
    // remain visible on the syphon client (Simple Client application) side.

    println("\n\nabout to stop sketch ...");
    println("deleting textures");
    gl.glDeleteTextures(1, texID, 0);
    if (mySyphon!=null) {
      println("stopping the syphon server");
      mySyphon.stop();
    }
    println("sketch stopped, done.");
  }
}
  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#FFFFFF", "sketch_8_Particles2yBlobyFisica_working" });
  }
}
