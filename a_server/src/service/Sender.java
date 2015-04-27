package service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.SelectionKey;

public interface Sender {
	// 在第一次连接的时候发送公钥
	public void sendRSA_publicKey(SelectionKey key)
			throws UnsupportedEncodingException, IOException;

	//
	public void sendDES_Key(SelectionKey key);

	// 心跳包
	public void sendHeartbeat(SelectionKey key);

	public void sendAcceptReturn(SelectionKey key);

}
