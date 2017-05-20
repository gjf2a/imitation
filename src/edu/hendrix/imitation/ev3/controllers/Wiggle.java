package edu.hendrix.imitation.ev3.controllers;

import java.io.IOException;

import edu.hendrix.imitation.ev3.Control;
import edu.hendrix.imitation.ev3.ImitationLearner;
import lejos.hardware.motor.Motor;

public class Wiggle {
	public static void main(String[] args) throws IOException {
		ImitationLearner learner = new ImitationLearner(Control.UP, 16, 8)
				.action(Control.RIGHT, () -> {
					Motor.A.setSpeed(100);
					Motor.D.setSpeed(80);
					Motor.A.forward(); 
					Motor.D.forward();
					})
				.action(Control.LEFT, () -> {
					Motor.A.setSpeed(80);
					Motor.D.setSpeed(100);
					Motor.A.forward(); 
					Motor.D.forward();
					})
				.filename("wiggle1.txt");
		
		learner.control();
	}
}
