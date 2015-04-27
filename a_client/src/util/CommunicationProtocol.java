package util;

public class CommunicationProtocol {
	public final static byte[] HEAD = { 2, -1, 94, 39, 89, 52 };
	public final static byte[] END = { 2, -1, 92, 62 };
	public final static byte[] RSA_Mark = { 0 };
	public final static byte[] DES_Mark = { 1 };
	public final static byte[] MSG_Mark = { 2 };
	public final static byte[] HB_Mark = { 3 };
	public final static  int bufferSize=2048;
	public final static  int messageSize=bufferSize - HEAD.length - END.length - 16/* 总数据的MD5 */
	- 16/* 分数据的MD5 */- 1/* 数据标志位 */- 4/* 标识此条数据有多长 */- 4/* 标识此数据一共有多长 */
	- 4/* 标识这是总数据中的第几条 */- 4/* 总数据一共分了多少条发出来 */- 4/* 数据位长度 */;;

}
