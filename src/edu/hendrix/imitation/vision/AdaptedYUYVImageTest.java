package edu.hendrix.imitation.vision;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;

import edu.hendrix.imitation.util.Util;

public class AdaptedYUYVImageTest {

	@Test
	public void testIO() throws FileNotFoundException {
		AdaptedYUYVImage img1 = Util.fileToObject(new File("ssd1.txt"), s -> AdaptedYUYVImage.fromString(s));
		String s = img1.toString();
		AdaptedYUYVImage img2 = AdaptedYUYVImage.fromString(s);
		assertEquals(img1, img2);
	}

}
