//package rover;
//
//import lejos.robotics.geometry.Point;
//
//public class VisitedZone extends MapZone {
//	Point center;
//	static final double diameter = 2.50;
//	int angle;
//	int start_angle;
//	
//	public VisitedZone(Point center, int angle, int start_angle) {
//		this.center = center;
//		this.angle = angle;
//		this.start_angle = start_angle;
//	}
//	
//	public boolean inside(Point p) {		
//		double alpha = 0; //TODO angle between start_angle and center point vector 
//		return alpha<angle;
//	}
//}
