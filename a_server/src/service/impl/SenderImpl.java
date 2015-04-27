package service.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Map;

import message.MessageMaker;
import service.Sender;
import bean.ClientChannelInfo;
import code.DES_Coder;
import code.RSA_privateKey;

public class SenderImpl implements Sender {
	private final int buffersize = 2048;

	@Override
	public void sendRSA_publicKey(SelectionKey key)
			throws UnsupportedEncodingException, IOException {
		SocketChannel clientChannel = ((ServerSocketChannel) key.channel())
				.accept();
		clientChannel.configureBlocking(false);
		Map<String, Object> keyMap = RSA_privateKey.initKey();
		String privateKsy = null;
		try {
			privateKsy = RSA_privateKey.getPrivateKey(keyMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String publicKey = null;
		try {
			publicKey = RSA_privateKey.getPublicKey(keyMap);
			System.out.println("发送公钥:" + publicKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			clientChannel.write(MessageMaker.getBytebuffer(publicKey));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		clientChannel.register(key.selector(), SelectionKey.OP_READ,
				new ClientChannelInfo(buffersize, privateKsy));
	}

	@Override
	public void sendDES_Key(SelectionKey key) {
		SocketChannel clientChannel = (SocketChannel) key.channel();
		ClientChannelInfo channelInfo = (ClientChannelInfo) key.attachment();
		String private_key = channelInfo.getPrivateKey();
		String DES_Key = null;
		try {
			DES_Key = DES_Coder.initKey();
			channelInfo.setEDS_Key(DES_Key);
			System.out.println("send DES_Key:"+DES_Key);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			clientChannel.write(MessageMaker.getBytebuffer_privateKey(DES_Key,
					private_key));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 设置为下一次读取或是写入做准备
		key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
	}

	@Override
	public void sendHeartbeat(SelectionKey key) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendAcceptReturn(SelectionKey key) {
		// TODO Auto-generated method stub

	}

}
