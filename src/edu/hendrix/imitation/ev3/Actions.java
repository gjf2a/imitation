package edu.hendrix.imitation.ev3;

import lejos.hardware.motor.Motor;

public class Actions {
	
	public static void stop() {
		Motor.A.stop(true);
		Motor.D.stop();
	}
	
	public static void spinSpeed() {
		Motor.A.setSpeed(100);
		Motor.D.setSpeed(100);
	}
	
	public static void straightSpeed() {
		Motor.A.setSpeed(360);
		Motor.D.setSpeed(360);
	}
	
	public static void spinLeft() {
		spinSpeed();
		Motor.A.backward();
		Motor.D.forward();
	}
	
	public static void spinRight() {
		spinSpeed();
		Motor.A.forward();
		Motor.D.backward();
	}
	
	public static void forward() {
		straightSpeed();
		Motor.A.forward();
		Motor.D.forward();
	}
	
	public static void veerLeft() {
		Motor.A.setSpeed(80);
		Motor.D.setSpeed(100);
		Motor.A.forward(); 
		Motor.D.forward();
	}
	
	public static void veerRight() {
		Motor.A.setSpeed(100);
		Motor.D.setSpeed(80);
		Motor.A.forward(); 
		Motor.D.forward();
	}
}
