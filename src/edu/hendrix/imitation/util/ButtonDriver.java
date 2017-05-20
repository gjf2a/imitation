package edu.hendrix.imitation.util;

import lejos.hardware.Button;
import lejos.hardware.motor.NXTRegulatedMotor;

public class ButtonDriver {
	private NXTRegulatedMotor left, right;
	private int forwardSpeed, turnSpeed;
	
	public ButtonDriver(NXTRegulatedMotor left, NXTRegulatedMotor right) {
		this(left, right, 360, 120);
	}
	
	public ButtonDriver(NXTRegulatedMotor left, NXTRegulatedMotor right, int forwardSpeed, int turnSpeed) {
		this.left = left;
		this.right = right;		
		this.forwardSpeed = forwardSpeed;
		this.turnSpeed = turnSpeed;
	}
	
	public void drive() {
		drive(() -> {});
	}
	
	public void setSpeeds(int speed) {
		left.setSpeed(speed);
		right.setSpeed(speed);
	}
	
	public void drive(Runnable whenDown) {
		if (Button.UP.isDown()) {
			setSpeeds(forwardSpeed);
			left.forward();
			right.forward();
			whenDown.run();
		} else if (Button.DOWN.isDown()) {
			setSpeeds(forwardSpeed);
			left.backward();
			right.backward();
			whenDown.run();
		} else if (Button.LEFT.isDown()) {
			setSpeeds(turnSpeed);
			left.backward();
			right.forward();
			whenDown.run();
		} else if (Button.RIGHT.isDown()) {
			setSpeeds(turnSpeed);
			left.forward();
			right.backward();
			whenDown.run();
		} else {
			left.stop(true);
			right.stop();
		}
	}
}
