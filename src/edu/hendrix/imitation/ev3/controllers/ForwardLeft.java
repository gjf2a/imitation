package edu.hendrix.imitation.ev3.controllers;

import java.io.IOException;

import edu.hendrix.imitation.ev3.Control;
import edu.hendrix.imitation.ev3.ImitationLearner;
import lejos.hardware.motor.Motor;

public class ForwardLeft {
	public static void main(String[] args) throws IOException {
		ImitationLearner learner = new ImitationLearner(Control.UP, 16, 8)
				.action(Control.UP, () -> {Motor.A.forward(); Motor.D.forward();})
				.action(Control.LEFT, () -> {Motor.A.backward(); Motor.D.forward();})
				.filename("forwardLeft1.txt");
		
		learner.control();
	}
}
