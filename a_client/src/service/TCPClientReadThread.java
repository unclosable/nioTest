package service;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import service.impl.ReadableHandlerImpl;
import bean.ChannelInfo;

public class TCPClientReadThread implements Runnable {
	private Selector selector;
	private ReadableHandler readableHandler;

	public TCPClientReadThread(Selector selector) {
		this.selector = selector;
		this.readableHandler=new ReadableHandlerImpl();

		new Thread(this).start();
	}

	public void run() {
		try {
			while (selector.select() > 0) {
				// 遍历每个有可用IO操作Channel对应的SelectionKey
				for (SelectionKey sk : selector.selectedKeys()) {

					// 如果该SelectionKey对应的Channel中有可读的数据
					if (sk.isReadable()) {
						// 使用NIO读取Channel中的数据
						ChannelInfo channelInfo = (ChannelInfo) sk.attachment();
						switch (channelInfo.getAcceptTimes()) {
						case 0:
							try {
								readableHandler.TheFirstAccept(sk);
								sk.interestOps(SelectionKey.OP_READ);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						case 1:
							readableHandler.TheSecondAccept(sk);
							sk.interestOps(SelectionKey.OP_READ|SelectionKey.OP_WRITE);
							break;

						default:
							readableHandler.TheInfomationAccept(sk);
							sk.interestOps(SelectionKey.OP_READ|SelectionKey.OP_WRITE);
							break;
						}

						// 为下一次读取作准备
//						sk.interestOps(SelectionKey.OP_READ|SelectionKey.OP_WRITE);
					}

					// 删除正在处理的SelectionKey
					selector.selectedKeys().remove(sk);
					
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}