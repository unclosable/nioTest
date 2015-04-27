package message;

import java.nio.ByteBuffer;
import java.util.Arrays;

import util.ByteUtil;
import util.CommunicationProtocol;
import code.Coder;
import code.DES_Coder;
import code.RSA_privateKey;

public class MessageMaker extends CommunicationProtocol {

	// public MessageMaker(int buffersize) {
	// this.bufferSize = buffersize;
	// setMessageSize();
	// }
	//
	// public MessageMaker() {
	// this.bufferSize = 2048;
	// setMessageSize();
	// }

	// private void setMessageSize() {
	// int messageSize = bufferSize - HEAD.length - END.length - 16/* 总数据的MD5 */
	// - 16/* 分数据的MD5 */- 1/* 数据标志位 */- 4/* 标识此条数据有多长 */- 4/* 标识此数据一共有多长 */
	// - 4/* 标识这是总数据中的第几条 */- 4/* 总数据一共分了多少条发出来 */- 4/* 数据位长度 */;
	// this.messageSize = messageSize;
	// }

	private static ByteBuffer[] getBytebuffer(byte[] message, int type)
			throws Exception {
		int reBBsize = message.length / messageSize + 1;
		ByteBuffer[] re = new ByteBuffer[reBBsize];
		byte[][] messagePackag = packaging(message, reBBsize, getMark(type));
		for (int i = 0; i < reBBsize; i++) {
			re[i] = ByteBuffer.wrap(messagePackag[i]);
		}
		return re;
	}

	/* 明文传递只有一种情况，第一次传递公钥 */
	public static ByteBuffer[] getBytebuffer(String message) throws Exception {
		byte[] messageByte = message.getBytes("utf-16");
		return getBytebuffer(messageByte, 0);
	}

	/* 服务端没有公钥 */
	/* 用公钥加密只有一种情况，传递加密后的公钥确认信息 */
	// public ByteBuffer[] getBytebuffer_publicKey(byte[] message, String key)
	// throws Exception {
	// message = RSA_privateKey.encryptByPublicKey(message, key);
	// return getBytebuffer(message, 0);
	// }

	/* 用公钥加密只有一种情况，传递加密后的公钥确认信息 */
	// public ByteBuffer[] getBytebuffer_publicKey(String message, String key)
	// throws Exception {
	// return getBytebuffer_publicKey(message.getBytes("utf-16"), key);
	// }

	/* 用私钥加密只有一种情况，传递加密后的DES秘钥 */
	public static ByteBuffer[] getBytebuffer_privateKey(byte[] message,
			String key) throws Exception {
		message = RSA_privateKey.encryptByPrivateKey(message, key);
		return getBytebuffer(message, 1);
	}

	/* 用私钥加密只有一种情况，传递加密后的DES秘钥 */
	public static ByteBuffer[] getBytebuffer_privateKey(String message,
			String key) throws Exception {
		return getBytebuffer_privateKey(message.getBytes("utf-16"), key);
	}

	/* 心跳包或者是正式信息，使用DES加密 */
	public static ByteBuffer[] getBytebuffer_DES(byte[] message, String key,
			int type) throws Exception {
		message = DES_Coder.encrypt(message, key);
		return getBytebuffer(message, type);
	}

	/* 心跳包或者是正式信息，使用DES加密 */
	public static ByteBuffer[] getBytebuffer_DES(String message, String key,
			int type) throws Exception {
		return getBytebuffer_DES(message.getBytes("utf-16"), key, type);
	}

	private static byte[][] packaging(byte[] message, int reBBsize, byte[] mark)
			throws Exception {
		int totleLength = message.length;

		byte[][] re = new byte[reBBsize][bufferSize];
		byte[] totalMD5 = Coder.encryptMD5(message);
		int flg = 0;
		for (int i = 0; i < reBBsize; i++) {
			int thisMessageSize = totleLength - flg < messageSize ? totleLength
					- flg : messageSize;
			byte[] aMsg = Arrays.copyOfRange(message, flg, flg
					+ thisMessageSize);
			byte[] defaultMSG = new byte[messageSize - thisMessageSize];
			for (int n = 0; n < defaultMSG.length; n++) {
				defaultMSG[n] = 0;
			}
			byte[] thisMD5 = Coder.encryptMD5(aMsg);
			re[i] = ByteUtil.add(HEAD,// 头标志位
					mark, // 数据类型标志位
					totalMD5,// 总数据的16位检验码
					ByteUtil.intToByteArray1(totleLength),// 总数据长度标志位
					ByteUtil.intToByteArray1(reBBsize),// 总数据一共分了多少条发出来
					thisMD5,// 此条数据16位检验码
					ByteUtil.intToByteArray1(thisMessageSize),// 此条数据长度标志位
					ByteUtil.intToByteArray1(i),// 此条数据是总数据中的第几条
					ByteUtil.intToByteArray1(messageSize),// 数据长度
					aMsg, // 数据
					defaultMSG, END// 结束标识
					);
			flg += messageSize;
		}
		return re;
	}

	private static byte[] getMark(int mark) {
		switch (mark) {
		case 0:
			return RSA_Mark;
		case 1:
			return DES_Mark;
		case 2:
			return MSG_Mark;
		case 3:
			return HB_Mark;
		default:
			return null;
		}
	}
}