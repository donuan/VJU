class syphon {
  JSyphonServer mySyphon;
  int[] texID;

  void initSyphon(GL gl, String theName) {
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

  void renderTexture(GL gl) {
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

  void dispose() {
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
