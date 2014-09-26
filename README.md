VJU
===

Text taken from "Investigating a User Controlled Narrative through Interactive Technologies Applied to Cinematographic Storytelling" by Hnnes Andersson which can be found here: http://donuan.tv/academic.htm

Interactive dance application using processing, pd extended and modul8, Hannes Andersson, Daniel González-Franco and Rocío Márquez.

#VJU

VJU is an interactive visual installation created in collaboration between Hannes Andersson, Daniel González-Franco and Rocío Márquez. The installation explores the body as an interface for intuitive audio-visual creation, and is constructed using Kinect, Pd Extended, Processing 1.5 , Modul8, Syphon and Mad Mapper as well as Adobe After Effects for the creation of the non-generative graphical content.
The user´s hand movements, as well as general spatial position is used to trigger, control and to mix graphical elements, allowing the user to create a dynamic visual composition through moving his/her body. The installation works best in large environments, where music is present, as this facilitates the interaction with the installation. VJU is constructed in the following way: 

The interaction space consists of a Kinect and a projector, facing a large projection screen. In the presence of a user, Processing uses the Kinect data to generate a digital representation of the user in the form of a silhouette as well as a particle system being affected by, or contained within, this silhouette (Figure 18 & 19). In this version there are three basic states, meaning that three positions correspond with three particle systems. Which particle system is created depends on where the user stands in the interaction area.



The particle system has the purpose of setting the initial visual context and to inspire the user to move. Particle systems that enable direct interaction, such as the one in the picture to the right, helps the user to realise his/her affectability. Having different particle systems generated depending on the user´s position, further helps the user to realise that affectability is not limited to this type of interaction. 
	Processing outputs the generated video to Syphon so that it can be received in real-time in Modul8, where the processing image is placed on the top layer of every layer set. Modulate treats this input as any other video, allowing for real-time modification of the video feed. Other media files where placed on the other Modul8 layers (Figure 20).


The user´s position and movement also indirectly control the Modul8 UI, and selected functions within it, as Modul8 is listening to OSC messages sent by a Pd Extended patch (Figure 21, 22, 23). All built-in functions within the Modul8 UI can be controlled by OSC by sending a numeric value and the name of the function it is to affect, to the OSC port that Modul8 is set to listen to. On/off buttons can only receive 1 (true) or 0 (false), while sliders can receive any value between 1 and 0. i.e. sending the value 0.5 with the message “/md8key/ctrl_layer_rotation_y/8 $1” will result in the 8th layer in the 1st layer set rotating 180 degrees. 



The Pd patch also receives data from the Kinect, and is also constructed to have three basic states, corresponding to controlling three sets of media in Modul8. In order to avoid synchronisation errors, the basic states are not determined by the Kinect data, but by OSC messages sent by Processing, every time the basic state is changed.
	Pd uses the Kinect data to determine the position of the users two hand. Movement of one hand in one axis and is translated to control one action in Modul8, resulting in 6 different actions if moving both hands on the X, Y and Z axis. i.e. movement of one hand in the X axis controls the master crossfade. The Z movement of this hand controls the size of a layer, and the Y movement of this hand controls the same layer rotating in the X axis. Consequently moving this hand forward in a curve could have the effect of a layer to get bigger, to rotate and to fade out.


Modul8 outputs the combined material back to Syphon so that is can be received in Mad Mapper, which in turn outputs the combined material to 2 projectors . One is the projector in the interaction area, projecting the material onto the user (Figure 24), and one is a bigger projection in on another wall, allowing the user to see what s/he is doing
