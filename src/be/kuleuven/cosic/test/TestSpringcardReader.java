package be.kuleuven.cosic.test;

import java.io.IOException;

import be.kuleuven.cosic.util.springcardreader.PersistentSpringcardReader;
import be.kuleuven.cosic.util.springcardreader.ReaderEvent;
import be.kuleuven.cosic.util.springcardreader.SpringcardEvent;
import be.kuleuven.cosic.util.springcardreader.Springcard;
import be.kuleuven.cosic.util.springcardreader.SpringcardEventListener;
import be.kuleuven.cosic.util.springcardreader.SpringcardReaderListener;

/**
 * 
 */
public class TestSpringcardReader implements SpringcardEventListener, SpringcardReaderListener {

	public static void main(String[] args) throws IOException, InterruptedException {
		PersistentSpringcardReader reader = new PersistentSpringcardReader(500);
		TestSpringcardReader test = new TestSpringcardReader();
		reader.addListener(test);
		reader.addReaderListener(test);
		reader.run();		
	}

	@Override
	public void springcardEvent(SpringcardEvent event) {
		System.out.println("SpringcardReader " + System.currentTimeMillis() + " " + event.getData());

		try {
			//event.getOrigin().setBuzzer((short) 300);
			event.getOrigin().setLeds(Springcard.Led.OFF, Springcard.Led.OFF, Springcard.Led.ON);
			try {
				Thread.sleep(700);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			event.getOrigin().setLedsT(Springcard.Led.OFF, Springcard.Led.ON, Springcard.Led.OFF, (short) 1500);
			//Thread.sleep(400);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void readerEvent(ReaderEvent event) {
		System.err.println(event.getType());
		
	}

}
