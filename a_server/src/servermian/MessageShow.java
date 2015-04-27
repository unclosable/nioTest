package servermian;

import java.io.UnsupportedEncodingException;

import message.ArrayBlockingQueueBowl;

public class MessageShow implements Runnable {

	@Override
	public void run() {
		while (true)
			try {
				System.out
						.println(new String(ArrayBlockingQueueBowl
								.getInstance().getArrayBlockingQueue().take(),
								"utf-16"));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
