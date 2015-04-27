package service.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import message.MessageAssemblyPlant;
import message.MessageMaker;
import service.ReadableHandler;
import util.ByteUtil;
import bean.ChannelInfo;
import code.RSA_publicKey;

public class ReadableHandlerImpl implements ReadableHandler {

	@Override
	public void TheFirstAccept(SelectionKey selectionKey) throws Exception {
		SocketChannel sc = (SocketChannel) selectionKey.channel();
		ChannelInfo channelInfo = (ChannelInfo) selectionKey.attachment();
		ByteBuffer buffer = channelInfo.getByteBuffer();
		buffer.clear();
		sc.read(buffer);
		buffer.flip();

		byte[] received = buffer.array();

		channelInfo.accept();
		byte[] punlicKey = MessageAssemblyPlant.getMessage(received);
		System.out.println("得到公钥:" + new String(punlicKey, "UTF-16"));
		channelInfo.setPublicKey(new String(punlicKey, "UTF-16"));
		/* 发送确认公钥的信息 */
		String theMessage = "确认公钥";
		ByteBuffer[] send = MessageMaker.getBytebuffer_publicKey(theMessage,
				channelInfo.getPublicKey());
		for (ByteBuffer byteBuffer : send)
			sc.write(byteBuffer);
		channelInfo.send();
	}

	@Override
	public void TheSecondAccept(SelectionKey selectionKey) throws IOException {
		SocketChannel sc = (SocketChannel) selectionKey.channel();
		ChannelInfo channelInfo = (ChannelInfo) selectionKey.attachment();
		ByteBuffer buffer = channelInfo.getByteBuffer();
		buffer.clear();
		sc.read(buffer);
		buffer.flip();

		byte[] received = buffer.array();
		System.out.println("信息："+ByteUtil.bytesToHexString(received));

		channelInfo.accept();
		byte[] DES_Key = null;
		try {
			DES_Key = RSA_publicKey.decryptByPublicKey(
					MessageAssemblyPlant.getMessage(received),
					channelInfo.getPublicKey());
			System.out.println("得到DES_Key:" + new String(DES_Key, "UTF-16"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		channelInfo.setDESKey(new String(DES_Key, "UTF-16"));
		/* 发送确认公钥的信息 */
		String theMessage = "确认DESKey";
		try {
			ByteBuffer[] send = MessageMaker.getBytebuffer_DES(theMessage,
					channelInfo.getDESKey(), 1);
			for (ByteBuffer byteBuffer : send)
				sc.write(byteBuffer);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		channelInfo.send();
	}

	@Override
	public void TheHeartbeatAccept(SelectionKey selectionKey) {
		// TODO Auto-generated method stub

	}

	@Override
	public void TheInfomationAccept(SelectionKey selectionKey)
			throws IOException {
		SocketChannel sc = (SocketChannel) selectionKey.channel();
		ChannelInfo channelInfo = (ChannelInfo) selectionKey.attachment();
		ByteBuffer buffer = channelInfo.getByteBuffer();
		buffer.clear();
		sc.read(buffer);
		buffer.flip();

		byte[] received = buffer.array();
		System.out.println("信息："+ByteUtil.bytesToHexString(received));
		System.out.println("秘钥："+channelInfo.getDESKey());
		MessageAssemblyPlant.getInstance().accept(received,
				channelInfo.getDESKey());
	}
}
