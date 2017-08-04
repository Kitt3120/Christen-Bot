package de.vanillekeks.christenbot.modules.modules.audio;

public class QueueSizeTooSmallException extends Exception {

	public QueueSizeTooSmallException() {
		super();
	}

	public QueueSizeTooSmallException(String message) {
		super(message);
	}

	public QueueSizeTooSmallException(Throwable cause) {
		super(cause);
	}

	public QueueSizeTooSmallException(String message, Throwable cause) {
		super(message, cause);
	}

}
