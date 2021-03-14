package rover;

import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.robotics.geometry.Point;
import lejos.robotics.navigation.Pose;
import lejos.robotics.navigation.Waypoint;
import tools.Beeper;
import tools.Blinker;
import tools.Order;

/**
 * A simple executable class that wrapps a whole lot of possible tests on the rover.
 * 
 * @author Stevan Antoine
 *
 */
public class Tester {
	
	//###################################################################################################################
	//###################################################################################################################
	//### Tests #########################################################################################################
	//###################################################################################################################
	//###################################################################################################################
	
	//###################################################################################################################
	//### sensors tests #################################################################################################
	//###################################################################################################################
	/**
	 * Test of the behaviour of the ultrasonic sensor.
	 * @param rover the rover to be tested.
	 */
	public static void test_ultrasonic_sensor(Rover rover) {
		rover.logger.println("starting tests on ultra...");
		float dist;
		while (Button.readButtons() != Button.ID_ENTER) {
			dist = rover.ultra.read().getValue();
			System.out.println(dist);
		}
		rover.logger.println("ultra done");
	}
	/** 
	 * Test of the behaviour of the color sensor.
	 * @param rover the rover to be tested.
	 */
	public static void test_color_sensor(Rover rover) {
		rover.logger.println("starting tests on color...");
		float id = -1;
		while (Button.readButtons() != Button.ID_ENTER) {
			LCD.clear();
			id = rover.color.read().getValue();
			System.out.println("id: " + id);
			Button.waitForAnyPress();
		}
		rover.logger.println("color done");	
	}
	
	//###################################################################################################################
	//### motors tests ##################################################################################################
	//###################################################################################################################
	/** 
	 * Test of the behaviour of the motors.
	 * @param rover the rover to be tested.
	 */
	public static void test_motors(Rover rover) {
		rover.logger.println("starting tests on motors...");
		// reseting the tacho counts.
		rover.pliers.motor.device.resetTachoCount();
		rover.right.device.resetTachoCount();
		rover.left.device.resetTachoCount();
		
		Blinker.blink(Blinker.ORANGE, Blinker.FAST, 0); Button.waitForAnyPress(); Beeper.beep();
		
		// pliers
		rover.logger.println("closing pliers..."); rover.logger.println(rover.pliers.getTachoCount());
		rover.pliers.motor.device.setAcceleration(90);
		rover.pliers.grab();
		while (rover.pliers.isMoving()) {
			rover.logger.println(rover.pliers.getTachoCount() +
					            "(" + rover.pliers.motor.device.getRotationSpeed() + ")");
		}
		rover.logger.println("releasing pliers..."); rover.logger.println(rover.pliers.getTachoCount());
		rover.pliers.release();
		while (rover.pliers.isMoving()) {
			rover.logger.println(rover.pliers.getTachoCount() +
					            "(" + rover.pliers.motor.device.getRotationSpeed() + ")");
		}
		rover.logger.println("done");
		Blinker.blink(Blinker.ORANGE, Blinker.SLOW, 0); Button.waitForAnyPress(); Beeper.beep();

		// right track
		rover.logger.println("rotating right..."); rover.logger.println(rover.right.device.getTachoCount());
		rover.right.write(new Order(90, 360));  
		while (rover.right.device.isMoving()) {
			rover.logger.println(rover.right.device.getTachoCount() + "(" + rover.right.device.getRotationSpeed() + ")");
		}
		rover.logger.println("done");
		Blinker.blink(Blinker.GREEN, Blinker.SLOW, 0);  Button.waitForAnyPress(); Beeper.beep();

		// left track
		rover.logger.println("rotating left..."); rover.logger.println(rover.left.device.getTachoCount());
		rover.left.write(new Order(90, 360));  
		while (rover.left.device.isMoving()) {
			rover.logger.println(rover.left.device.getTachoCount() + "(" + rover.left.device.getRotationSpeed() + ")");
		}
		rover.logger.println("done");
		Blinker.blink(Blinker.GREEN, Blinker.STILL, 0); Button.waitForAnyPress(); Beeper.beep();

		// end of tests
		rover.logger.println("motors done");
	}
	
	//###################################################################################################################
	//### Navigator tests ###############################################################################################
	//###################################################################################################################
	/** 
	 * Test of the behaviour of the navigator.
	 * @param rover the rover to be tested.
	 */
	public static void test_navigator(Rover rover) {
		Pose pose = rover.nav.getPose();
		rover.logger.println("pose before: " +	pose.getX() + ", " +
												pose.getY() + ", " +
												rover.right.device.getTachoCount());
		
		rover.logger.println("goTo");
		rover.nav.goTo(new Waypoint(pose.pointAt(100, pose.getHeading()+90)));
		
		rover.logger.println("travel");
		rover.nav.forward();
		rover.nav.travel(10 /1000f);
		rover.logger.println(rover.nav.getPose().toString() + ", " + rover.right.device.getTachoCount());
	}
	
	/** 
	 * Let the navigator perform a square to test distances and angles.
	 * @param rover the rover to be tested.
	 */
	public static void test_navigator_square_antoine(Rover rover) {
		for (int i = 0; i < 0; i++) {
			rover.logger.println("travel");
			rover.nav.travel(200 /1000f);
			rover.logger.println("rotate");
			rover.nav.rotate(90);
		}
		
		rover.logger.println("pose: " + rover.nav.getPose());
		Button.waitForAnyPress();
		
		Point waypoints[] = new Point[4];
		waypoints[0] = MapZone.initial_pose.getLocation();
		Point dir = new Point(200, 0);
		for (int i = 1; i < waypoints.length; i++) {
			waypoints[i] = waypoints[i-1].add(dir);
			dir = dir.leftOrth();
		}
		
		for (int i = 0; i < 4; i++) {
			rover.logger.println(waypoints[i].toString());
			rover.nav.goTo(waypoints[i]);
			rover.nav.rotate(90);
		}
	}

	/**
	 * Simplyfied version of the exploration strategy introduced in ./report/report-martian-rover.pdf.
	 * @param rover the rover to be tested.
	 */
	public static void test_navigator_sweep_antoine(Rover rover) {
		rover.logger.println("pose before: " +	rover.nav.getPose().getX() + ", " +
												rover.nav.getPose().getY() + ", " +
												rover.nav.getPose().getHeading());
		Button.waitForAnyPress();
		rover.nav.setup_travel(200 /1000f);
		while (rover.nav.isMoving()) {
			Beeper.beep();
			System.out.println(rover.ultra.read().getValue());
		}
		rover.nav.compute_new_location();
		rover.logger.println("pose travel: " +	rover.nav.getPose().getX() + ", " +
												rover.nav.getPose().getY() + ", " +
												rover.nav.getPose().getHeading());
		Button.waitForAnyPress();
		
		rover.logger.println("pose before: "+rover.nav.getPose().toString());
		rover.nav.setup_rotate(90);
		while (rover.nav.isMoving()) {
			System.out.println(rover.ultra.read().getValue());
			Beeper.beep();
		}
		rover.nav.compute_new_heading();
		rover.logger.println("pose travel: " +	rover.nav.getPose().getX() + ", " +
												rover.nav.getPose().getY() + ", " +
												rover.nav.getPose().getHeading());
		Button.waitForAnyPress();
	}
	
	/**
	 * Test of the navigator's travel method, to tweak wheels diameter.
	 * @param rover the rover to be tested.
	 */
	public static void test_travel_antoine(Rover rover) {
		while (Button.readButtons() != Button.ID_ENTER) {
			rover.nav.travel(500 /1000f);
			Button.waitForAnyPress();
		}
	}
	/**
	 * Test of the navigator's rotate method, to tweak distance between wheels.
	 * @param rover the rover to be tested.
	 */
	public static void test_rotate_antoine(Rover rover) {
		while (Button.readButtons() != Button.ID_ENTER) {
			rover.logger.println("travel");
			rover.nav.rotate(90);
			Button.waitForAnyPress();
		}
	}
	
	//###################################################################################################################
	//### Grabber tests #################################################################################################
	//###################################################################################################################
	/**
	 * Test of the grabber.
	 * @param rover the rover to be tested.
	 */
	public static void test_grabber_antoine(Rover rover) {
		rover.harvest(new Point(0.75f, 0.75f));
	}
	
	/**
	 * Main method to test the rover in its environment.
	 * 
	 * @param args java arguments for main methods.
	 */
	public static void main(String[] args) {
		//###################################################################################################################
		//### several tests #################################################################################################
		//###################################################################################################################
		Rover rover = Rover.build(SensorPort.S4, SensorPort.S1,
                MotorPort.A, MotorPort.B, MotorPort.C);
		
		Tester.test_ultrasonic_sensor(rover);
		Tester.test_color_sensor(rover);
		Tester.test_motors(rover);
		Tester.test_navigator(rover);
		Tester.test_navigator_square_antoine(rover);
		Tester.test_navigator_sweep_antoine(rover);

		Tester.test_travel_antoine(rover);
		Tester.test_rotate_antoine(rover);
		Tester.test_grabber_antoine(rover);
		Button.waitForAnyPress();
	}

}
