package service.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Date;

import message.MessageAssemblyPlant;
import message.MessageMaker;
import service.Sender;
import service.TCPProtocol;
import util.ByteUtil;
import bean.ClientChannelInfo;
import code.RSA_privateKey;

public class TCPProtocolImpl implements TCPProtocol {
	private Sender sender;

	public TCPProtocolImpl() {
		sender = new SenderImpl();
	}

	public void handleAccept(SelectionKey key) throws IOException {
		sender.sendRSA_publicKey(key);
	}

	public void handleRead(SelectionKey key) throws IOException {
		// 获得与客户端通信的信道
		SocketChannel clientChannel = (SocketChannel) key.channel();

		// 得到并清空缓冲区
		ClientChannelInfo info = (ClientChannelInfo) key.attachment();
		int sendTimes = info.getSendTimes();
		ByteBuffer buffer = info.getByteBuffer();
		buffer.clear();

		// 读取信息获得读取的字节数
		long bytesRead = clientChannel.read(buffer);

		// 将缓冲区准备为数据传出状态
		buffer.flip();
		if (bytesRead == -1) {
			// 没有读取到内容的情况
			clientChannel.close();
		} else {
			byte[] thePrimitiveMessage = buffer.array();
			// System.out.println(ByteUtil.bytesToHexString(thePrimitiveMessage));
			switch (sendTimes) {
			case 0: {
				byte[] message = MessageAssemblyPlant
						.getMessage(thePrimitiveMessage);
				byte[] bytMseeage = null;
				try {
					bytMseeage = RSA_privateKey.decryptByPrivateKey(message,
							info.getPrivateKey());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String strMessage = new String(bytMseeage, "utf-16");
				System.out.println("第一次确认信息：" + strMessage);
				/* 此处可以添加第一次链接公钥校验的判断 */
				if (true) {
					sender.sendDES_Key(key);
				}
				break;
			}

			// case 1:{//
			// System.out.println("1!!!");
			// break;
			// }
			default: {
				// System.out.println("default");
				MessageAssemblyPlant.getInstance().accept(thePrimitiveMessage,
						info.getEDS_Key());
				ClientChannelInfo channelInfo = (ClientChannelInfo) key
						.attachment();
				SocketChannel sc = (SocketChannel) key.channel();
				ByteBuffer[] writeBuffer = null;
				try {
					// System.out.println(message);
					// System.out.println(info.getDESKey());
					writeBuffer = MessageMaker.getBytebuffer_DES("接收到了数据",
							channelInfo.getEDS_Key(), 2);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for (ByteBuffer buffeqr : writeBuffer)
					sc.write(buffeqr);
				break;
			}
			}
			info.send();
			// 设置为下一次读取或是写入做准备
			key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		}
	}

	public void handleWrite(SelectionKey key) throws IOException {
		// do nothing
	}

	public void handleBreak(SelectionKey key) throws IOException {
		key.cancel();
	}
}
