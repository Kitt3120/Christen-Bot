package de.vanillekeks.christenbot.modules.modules.audio;

public class QueueEmptyException extends Exception {

	public QueueEmptyException() {
		super();
	}

	public QueueEmptyException(String message) {
		super(message);
	}

	public QueueEmptyException(Throwable cause) {
		super(cause);
	}

	public QueueEmptyException(String message, Throwable cause) {
		super(message, cause);
	}

}
