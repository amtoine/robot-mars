# robot-mars

To generate the javadoc:
	- execute javadoc.xml (e.g. as Ant build in eclipse environment)

If the doc is already generated (the presence of files in ./doc/ might indicate that the doc is properly generated. it could be an older version, so one should generate the doc when one opens the project for the first time)
To run the doc:
	- open the ./doc/index.html inside an IDE, your web browser or inside a terminal.

To run the project, the easiest way is the following :
	- open the project in eclipse (make sure the leJOS plugin is installed on your machine)
	- open the ./deployment/RoverDeploy.java file. It the executable main class of the project.
	- you can play with commented instructions in the main method or leave it as it is.
	- to run the project, make sure you EV3 is connected, right click on RoverDeploy.java in the file hierarchy on the left of your screen (if there is no such hierarchy, enable it in the settings or you can directly right click the code of RoverDeploy.java) and then Run as leJOS EV3 Program.
