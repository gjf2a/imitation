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
		Video video = setupVideo();
		timer.start();
		while (Button.ESCAPE.isUp()) {
			video.grabFrame(frame);
			AdaptedYUYVImage input = new AdaptedYUYVImage(frame, Constants.WIDTH, Constants.HEIGHT);
			Optional<Control> pushed = checkButtons();
			learnAndAct(pushed, input);
			timer.bumpCycle();
		}
		finish();
	}
	
	private void finish() {
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
	}
	
	private void learnAndAct(Optional<Control> pushed, AdaptedYUYVImage input) {
		if (isAutonomous) {
			actions.get(bsoc.bestMatchFor(input)).run();
		} else {
			pushed.ifPresent(control -> {
				bsoc.train(input, control);
				actions.get(control).run();
			});
		}
	}
	
	private Optional<Control> checkButtons() {
		Optional<Control> pushed = Optional.empty();
		if (Button.ENTER.isDown()) {
			isAutonomous = true;
		} else {
			pushed = findButton();
			if (pushed.isPresent()) {
				isAutonomous = false;
			}
		}
		return pushed;
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
