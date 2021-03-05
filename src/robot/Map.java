package robot;

import lejos.robotics.mapping.OccupancyGridMap;

public class Map {
	double resolution = 25;
	double freeThreshold = resolution;
	double occupiedThreshold = resolution;
	OccupancyGridMap map = new OccupancyGridMap(150,250,freeThreshold,occupiedThreshold,resolution);
	
	//int[][2] recup_zone;
	//for (int i=0;i<recup_zone.size();i++) {
	//	map.setOccupied(recup_zone[i][0],recup_zone[i][1],1);
	//}

}
