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
  
  void updateAndDisplay() {
    // let it flow, end with a new x and y position
    id += 0.01;
    d = (noise(id, x/globalY, y/globalY)-0.5)*globalX;
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
        r = int(random(255));
        g = int(random(255));
        b = int(random(255)); 
      }else if(rightHandXMap > 500 && rightHandYMap > 250 || leftHandXMap > 500 && leftHandYMap > 250){
        r = 255;
        g = 0;
        b = int(rightHandYMap);
      }else{
        r = int(leftHandYMap);
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
