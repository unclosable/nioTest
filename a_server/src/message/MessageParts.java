package message;

import java.util.Arrays;
import java.util.Date;

import util.ByteUtil;
import code.Coder;

public class MessageParts {
	private byte[] message;
	private byte[] MD5Checker;
	private int totleTimes;
	private int totleMessageLength;
	private static int writeTimes;
	private long bornTime;
	private String DESKey;

	public MessageParts(int totleMessageLength, byte[] MD5, int times,
			String DESKey) {
		this.totleMessageLength = totleMessageLength;
		this.MD5Checker = MD5;
		this.totleTimes = times;
		this.DESKey = DESKey;
		message = new byte[this.totleMessageLength];
		bornTime = new Date().getTime();
		writeTimes = 0;
	}

	public void write(byte[] theMessage, int sflg) {
		System.arraycopy(theMessage, 0, message, sflg, theMessage.length);
		writeTimes++;
	}

	public boolean isFinished() {
		return writeTimes == totleTimes;
	}

	public boolean isOuttime(long outTime) {
		return outTime < new Date().getTime() - bornTime;
	}

	public boolean MD5Chekc() {
		try {
			return Arrays.equals(MD5Checker, Coder.encryptMD5(message));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public byte[] getMD5Checker() {
		return MD5Checker;
	}

	public void setMD5Checker(byte[] mD5Checker) {
		MD5Checker = mD5Checker;
	}

	public String getDESKey() {
		return DESKey;
	}

	public void setDESKey(String dESKey) {
		DESKey = dESKey;
	}

	public byte[] getMessage() {
		return message;
	}

	public void setMessage(byte[] message) {
		this.message = message;
	}

}
