package bean;

import java.nio.ByteBuffer;

public class ChannelInfo {
	private ByteBuffer byteBuffer;
	private String publicKey;
	private String DESKey;
	private int sendTimes;
	private int acceptTimes;
	private int bufferSize;

	public ChannelInfo(int bufferSize) {
		this.byteBuffer = ByteBuffer.allocate(bufferSize);
		this.sendTimes = 0;
		this.acceptTimes = 0;
		this.bufferSize = bufferSize;
	}

	public void send() {
		sendTimes++;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public void accept() {
		acceptTimes++;
	}

	public int getSendTimes() {
		return sendTimes;
	}

	public int getAcceptTimes() {
		return acceptTimes;
	}

	public ByteBuffer getByteBuffer() {
		return byteBuffer;
	}

	public void setByteBuffer(ByteBuffer byteBuffer) {
		this.byteBuffer = byteBuffer;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getDESKey() {
		return DESKey;
	}

	public void setDESKey(String dESKey) {
		DESKey = dESKey;
	}

	public void setSendTimes(int sendTimes) {
		this.sendTimes = sendTimes;
	}

	public void setAcceptTimes(int acceptTimes) {
		this.acceptTimes = acceptTimes;
	}

}
