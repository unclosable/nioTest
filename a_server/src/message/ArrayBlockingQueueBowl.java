package message;

import bean.ArrayBlockingQueue;

public class ArrayBlockingQueueBowl {
	private static volatile ArrayBlockingQueueBowl arrayBlockingQueueBowl = null;
	private ArrayBlockingQueue<byte[]> arrayBlockingQueue;

	private ArrayBlockingQueueBowl() {
		this.arrayBlockingQueue = new ArrayBlockingQueue<>(128);
	}

	public static ArrayBlockingQueueBowl getInstance() {
		if (arrayBlockingQueueBowl == null)
			synchronized (ArrayBlockingQueueBowl.class) {
				if (arrayBlockingQueueBowl == null) {
					arrayBlockingQueueBowl = new ArrayBlockingQueueBowl();
				}
			}
		return arrayBlockingQueueBowl;
	}

	public ArrayBlockingQueue<byte[]> getArrayBlockingQueue() {
		return arrayBlockingQueue;
	}

	// public void setArrayBlockingQueue(
	// ArrayBlockingQueue<byte[]> arrayBlockingQueue) {
	// this.arrayBlockingQueue = arrayBlockingQueue;
	// }

}
