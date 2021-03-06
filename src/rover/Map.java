package rover;

import lejos.robotics.mapping.OccupancyGridMap;

public class Map {
	public OccupancyGridMap map;
	
	public Map(double res) {
		double freeThreshold = res;
		double occupiedThreshold = res;
		OccupancyGridMap m = new OccupancyGridMap(150,250,freeThreshold,occupiedThreshold,res);
		map = m;
	}
	
	
	
	
	//int[][2] recup_zone;
	//for (int i=0;i<recup_zone.size();i++) {
	//	map.setOccupied(recup_zone[i][0],recup_zone[i][1],1);
	//}

}
