package bean;

import java.nio.ByteBuffer;

public class ClientChannelInfo {
	private ByteBuffer byteBuffer;
	private boolean isSendKey;
	private boolean isChecked;
	private String privateKey;
	private String EDS_Key;
	private int sendTimes;

	public ClientChannelInfo(int bufferSize, String privateKey) {
		this.byteBuffer = ByteBuffer.allocate(bufferSize);
		this.isChecked = false;
		this.isSendKey = false;
		this.privateKey = privateKey;
		this.sendTimes = 0;
	}

	public void send() {
		sendTimes++;
	}

	public int getSendTimes() {
		return sendTimes;
	}

//	public void setSendTimes(int sendTimes) {
//		this.sendTimes = sendTimes;
//	}

	public void sendPublicKey() {
		this.isSendKey = true;
	}

	public boolean isSendPublicKey() {
		return isSendKey;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public ByteBuffer getByteBuffer() {
		return byteBuffer;
	}

	public void setByteBuffer(ByteBuffer byteBuffer) {
		this.byteBuffer = byteBuffer;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

	public boolean isSendKey() {
		return isSendKey;
	}

	public void setSendKey(boolean isSendKey) {
		this.isSendKey = isSendKey;
	}

	public String getEDS_Key() {
		return EDS_Key;
	}

	public void setEDS_Key(String eDS_Key) {
		EDS_Key = eDS_Key;
	}

}
