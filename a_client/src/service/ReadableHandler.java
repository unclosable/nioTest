package service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.SelectionKey;

public interface ReadableHandler {
	// 首次链接，传递RSA公钥
	public void TheFirstAccept(SelectionKey selectionKey)
			throws UnsupportedEncodingException, IOException, Exception;

	// 二次链接，传递DES秘钥
	public void TheSecondAccept(SelectionKey selectionKey) throws IOException;

	// 心跳包的处理
	public void TheHeartbeatAccept(SelectionKey selectionKey);
	
	//信息包
	public void TheInfomationAccept(SelectionKey selectionKey) throws IOException;

}
