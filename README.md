## Basic Java APA102/LPD8806/LOCAL led-strip drivers and controllers

A couple of years back I wanted to control some APA102 and LPD8806 led-strips from java, but I couldn't find drivers for it so I created them myself by following the technical papers.
I was planning on making them public, but I forgot. Now, looking through what repositories I had around my accounts, I discovered this and thought it might help someone.
It has a lot of junk laying around and the code quality is far from decent, but I've done this on the fly  for fun a couple of years back.


Basically it is a Spring Boot application that can run on a Raspberry Pi.
My current setup is RPI and a APA102 led-strip with 144 leds. the jar runs at startup and from there on I simply 

This is configurable. For example one of my led-strips broke, I had to cut it in half. I simply reduced the number of leds in application.properties to 72 and it worked.

For local testing purposes I created a Java Swing graphical thingy to test effects.

You can change the <ledStrip.name> property inside of application.properties to either LOCAL, APA102 or LPD8806.

The default led effect is the moving segments, as it creates some cool shades because of the distanced light sources.
You can play around with the led effects.

For further questions or if you need help, just contact me at grecuSorinEugen@gmail.com

  