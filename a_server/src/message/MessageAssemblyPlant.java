package message;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import util.ByteUtil;
import util.CommunicationProtocol;
import code.Coder;
import code.DES_Coder;

public class MessageAssemblyPlant extends CommunicationProtocol {
	private static volatile MessageAssemblyPlant messageAssemblyPlant = null;
	private Map<String, MessageParts> msgMap;

	private MessageAssemblyPlant() {
		msgMap = new HashMap<>();
	}

	public static MessageAssemblyPlant getInstance() {
		if (messageAssemblyPlant == null)
			synchronized (MessageAssemblyPlant.class) {
				if (messageAssemblyPlant == null) {
					messageAssemblyPlant = new MessageAssemblyPlant();
				}
			}
		return messageAssemblyPlant;
	}

	public void accept(byte[] message, String DESKey) {
		if (checkHeadAndEnd(message)) {
			MessageParts messageParts = msgMap.get(ByteUtil
					.bytesToHexString(getTotleMD5(message)));
			if (messageParts == null) {
				messageParts = new MessageParts(getMessageLength(message),
						getTotleMD5(message), getMessageSize(message),
						DESKey);
			}
			if (checkThisMD5(message)) {
				messageParts.write(getMessage(message),
						getEveryMessageLength(message) * getMessageNo(message));
				msgMap.put(ByteUtil.bytesToHexString(getTotleMD5(message)),
						messageParts);
			} else {
				System.out.println(" 数据被篡改");
				/* 数据被篡改 */
			}
		} else {
			System.out.println("未经过头尾校验");
			/* 未经过头尾校验 */
		}
		check();
	}

	/* 检查帧头和帧尾 */
	public static boolean checkHeadAndEnd(byte[] message) {
		boolean head = Arrays.equals(HEAD, Arrays.copyOf(message, HEAD.length));
		if(!head)
			for(byte b:Arrays.copyOf(message, HEAD.length))
				System.out.println(b);
		boolean end = Arrays.equals(END, Arrays.copyOfRange(message,
				message.length - END.length, message.length));
		return head && end;
	}

	/* 得到数据帧的类别 */
	public static byte getMessageMark(byte[] message) {
		byte[] mark = Arrays.copyOfRange(message, HEAD.length, HEAD.length + 1);
		return mark[0];
	}

	/* 总数据的16位MD5校验码 */
	public static byte[] getTotleMD5(byte[] message) {
		byte[] totleMD5 = Arrays.copyOfRange(message, HEAD.length + 1,
				HEAD.length + 17);
		return totleMD5;
	}

	/* 总数据长度 */
	public static int getMessageLength(byte[] message) {
		byte[] length = Arrays.copyOfRange(message, HEAD.length + 17,
				HEAD.length + 21);
		int theLength = ByteUtil.byteArrayToInt(length);
		return theLength;
	}

	/* 总数据条数 */
	public static int getMessageSize(byte[] message) {
		byte[] size = Arrays.copyOfRange(message, HEAD.length + 21,
				HEAD.length + 25);
		int theSize = ByteUtil.byteArrayToInt(size);
		return theSize;
	}

	/* 得到本条数据的16位MD5校验码 */
	public static byte[] getThisMD5(byte[] message) {
		byte[] thisMD5 = Arrays.copyOfRange(message, HEAD.length + 25,
				HEAD.length + 41);
		return thisMD5;
	}

	/* 获得本条数据有多长 */
	public static int getThisMessageLength(byte[] message) {
		byte[] length = Arrays.copyOfRange(message, HEAD.length + 41,
				HEAD.length + 45);
		int theLength = ByteUtil.byteArrayToInt(length);
		return theLength;
	}

	/* 获得本条数据为总数据中的位置 */
	public static int getMessageNo(byte[] message) {
		byte[] no = Arrays.copyOfRange(message, HEAD.length + 45,
				HEAD.length + 49);
		int theno = ByteUtil.byteArrayToInt(no);
		return theno;
	}

	/* 获得数据分长度 */
	public static int getEveryMessageLength(byte[] message) {
		byte[] length = Arrays.copyOfRange(message, HEAD.length + 49,
				HEAD.length + 53);
		int theno = ByteUtil.byteArrayToInt(length);
		return theno;
	}

	/* 获得本条数据 */
	public static byte[] getMessage(byte[] message) {
		int messageLength = getThisMessageLength(message);
		byte[] msg = Arrays.copyOfRange(message, HEAD.length + 53, HEAD.length
				+ 53 + messageLength);
		return msg;
	}

	public static boolean checkThisMD5(byte[] message) {
		byte[] thisMD5 = getThisMD5(message);
		byte[] msg = getMessage(message);
		try {
			return Arrays.equals(thisMD5, Coder.encryptMD5(msg));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void check() {
		Iterator<MessageParts> iterator = msgMap.values().iterator();
		while (iterator.hasNext()) {
			MessageParts messageParts = iterator.next();
			if (messageParts.isOuttime(10 * 60 * 1000)) {
				msgMap.remove(ByteUtil.bytesToHexString(messageParts.getMD5Checker()));
			} else {
				if (messageParts.isFinished()) {// 所有数据已经全部接收完成
					if (messageParts.MD5Chekc()) {// 数据校验准确
						try {
							ArrayBlockingQueueBowl
									.getInstance()
									.getArrayBlockingQueue()
									.put(DES_Coder.decrypt(
											messageParts.getMessage(),
											messageParts.getDESKey()));
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						// 数据校验不准确（省缺）
					}
					msgMap.remove(ByteUtil.bytesToHexString(messageParts.getMD5Checker()));
				} else {
					// 未完成且不超时的数据
				}
			}
		}
	}

}
