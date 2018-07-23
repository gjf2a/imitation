package edu.hendrix.imitation.ev3.controllers;

import java.io.IOException;

import edu.hendrix.imitation.ev3.Actions;
import edu.hendrix.imitation.ev3.Control;
import edu.hendrix.imitation.ev3.ImitationLearner;

public class ForwardLeft {
	public static void main(String[] args) throws IOException {
		ImitationLearner learner = new ImitationLearner(Control.UP, 32, 8)
				.action(Control.UP, Actions::forward)
				.action(Control.LEFT, Actions::backLeft)
				.filename("forwardLeft1.txt");
		
		learner.control();
	}
}
