package edu.hendrix.imitation.ev3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.Optional;

import edu.hendrix.imitation.vision.AdaptedYUYVImage;
import edu.hendrix.imitation.vision.ShrinkingLabeledBSOC;
import edu.hendrix.imitation.util.CycleTimer;
import edu.hendrix.imitation.util.Util;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.video.Video;

public class ImitationLearner {
	private EnumMap<Control,Runnable> actions = new EnumMap<>(Control.class);
	private ShrinkingLabeledBSOC<Control> bsoc;
	private CycleTimer timer = new CycleTimer();
	private boolean isAutonomous = false;
	private byte[] frame;
	private Optional<String> filename = Optional.empty();
	private int autoCycles = 0, trainCycles = 0;
	
	private Video setupVideo() throws IOException {
		Video wc = BrickFinder.getDefault().getVideo();
		wc.open(Constants.WIDTH, Constants.HEIGHT);
		frame = wc.createFrame();
		return wc;
	}
	
	public ImitationLearner(Control startLabel, int maxNodes, int shrink) {
		bsoc = new ShrinkingLabeledBSOC<>(Control.class, maxNodes, startLabel, shrink);
	}
	
	public ImitationLearner action(Control label, Runnable act) {
		actions.put(label, act);
		return this;
	}
	
	public ImitationLearner filename(String name) throws FileNotFoundException {
		filename = Optional.of(name);
		File f = new File(name);
		if (f.exists()) {
			bsoc = Util.fileToObject(f, s -> new ShrinkingLabeledBSOC<>(Control.class, s));
		}
		return this;
	}
	
	public void control() throws IOException {
		LCD.drawString("Setting up", 0, 0);
		Video video = setupVideo();
		timer.start();
		LCD.drawString("Ready     ", 0, 0);
		while (Button.ESCAPE.isUp()) {
			video.grabFrame(frame);
			AdaptedYUYVImage input = new AdaptedYUYVImage(frame, Constants.WIDTH, Constants.HEIGHT);
			Optional<Control> pushed = checkButtons();
			learnAndAct(pushed, input);
			timer.bumpCycle();
			if (isAutonomous) {autoCycles += 1;} else {trainCycles += 1;}
		}
		finish();
	}
	
	private void finish() {
		Actions.stop();
		LCD.drawString(String.format("%4.2f hz", timer.cyclesPerSecond()), 0, 3);
		filename.ifPresent(name -> {
			try {
				LCD.drawString("Saving " + name, 0, 0);
				File f = new File(name);
				Util.objectToFile(f, bsoc);
				LCD.drawString("Saved", 0, 1);
			} catch (IOException exc) {
				LCD.drawString("Save failed", 0, 1);
			}
		});
		LCD.drawString(String.format("Train: %d", trainCycles), 0, 4);
		LCD.drawString(String.format("Auto:  %d", autoCycles), 0, 5);
		while (Button.ESCAPE.isDown());
		while (Button.ESCAPE.isUp());
	}
	
	private void learnAndAct(Optional<Control> pushed, AdaptedYUYVImage input) {
		if (bsoc.isTrained()) {
			Control chosen = bsoc.bestMatchFor(input);
			LCD.drawString(String.format("Auto:  %s       ", chosen), 0, 1);
			if (isAutonomous) {
				LCD.drawString("                  ", 0, 2);
				actions.get(chosen).run();
			}
		}
		if (pushed.isPresent()) {
			Control control = pushed.get();
			LCD.drawString(String.format("Human: %s       ", control), 0, 2);
			bsoc.train(input, control);
			actions.get(control).run();
		} else if (!isAutonomous) {
			Actions.stop();
		}
	}
	
	private Optional<Control> checkButtons() {
		Optional<Control> pushed = Optional.empty();
		if (Button.ENTER.isDown()) {
			Actions.stop();
			while (Button.ENTER.isDown()) {}
			isAutonomous = !isAutonomous;
			if (isAutonomous) {
				autoMsg();
			} else {
				trainMsg();
			}
			LCD.drawString("Auto      ", 0, 0);
		} else {
			pushed = findButton();
			if (pushed.isPresent()) {
				trainMsg();
				isAutonomous = false;
			}
		}
		return pushed;
	}
	
	private void trainMsg() {
		LCD.drawString("Train     ", 0, 0);
	}

	private void autoMsg() {
		LCD.drawString("Auto      ", 0, 0);
	}
	
	private Optional<Control> findButton() {
		for (Entry<Control, Runnable> control: actions.entrySet()) {
			if (control.getKey().isDown()) {
				return Optional.of(control.getKey());
			}
		}
		return Optional.empty();
	}
}
