package bean;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ArrayBlockingQueue<E> extends AbstractQueue<E> implements
		BlockingQueue<E>, java.io.Serializable {
	private static final long serialVersionUID = 1L;
	/** 队列元素 数组 */
	private final E[] items;
	/** 获取、删除元素时的索引（take, poll 或 remove操作） */
	private int takeIndex;
	/** 添加元素时的索引（put, offer或 add操作） */
	private int putIndex;
	/** 队列元素的数目 */
	private int count;
	/** 锁 */
	private final ReentrantLock lock;
	/** 获取操作时的条件 */
	private final Condition notEmpty;
	/** 插入操作时的条件 */
	private final Condition notFull;

	// 超出数组长度时，重设为0
	final int inc(int i) {
		return (++i == items.length) ? 0 : i;
	}

	/**
	 * 插入元素（在获得锁的情况下才调用）
	 */
	private void insert(E x) {
		items[putIndex] = x;
		putIndex = inc(putIndex);
		++count;
		notEmpty.signal();
	}

	/**
	 * 获取并移除元素（在获得锁的情况下才调用）
	 */
	private E extract() {
		final E[] items = this.items;
		E x = items[takeIndex];
		items[takeIndex] = null;
		takeIndex = inc(takeIndex);// 移到下一个位置
		--count;
		notFull.signal();
		return x;
	}

	/**
	 * 删除i位置的元素
	 */
	void removeAt(int i) {
		final E[] items = this.items;
		// if removing front item, just advance
		if (i == takeIndex) {
			items[takeIndex] = null;
			takeIndex = inc(takeIndex);
		} else {
			// 把i后面的直到putIndex的元素都向前移动一个位置
			for (;;) {
				int nexti = inc(i);
				if (nexti != putIndex) {
					items[i] = items[nexti];
					i = nexti;
				} else {
					items[i] = null;
					putIndex = i;
					break;
				}
			}
		}
		--count;
		notFull.signal();
	}

	/**
	 * 构造方法，指定容量，默认策略（不是按照FIFO的顺序访问）
	 */
	public ArrayBlockingQueue(int capacity) {
		this(capacity, false);
	}

	/**
	 * 构造方法，指定容量及策略
	 */
	public ArrayBlockingQueue(int capacity, boolean fair) {
		if (capacity <= 0)
			throw new IllegalArgumentException();
		this.items = (E[]) new Object[capacity];
		lock = new ReentrantLock(fair);
		notEmpty = lock.newCondition();
		notFull = lock.newCondition();
	}

	/**
	 * 通过集合构造
	 */
	public ArrayBlockingQueue(int capacity, boolean fair,
			Collection<? extends E> c) {
		this(capacity, fair);
		if (capacity < c.size())
			throw new IllegalArgumentException();
		for (Iterator<? extends E> it = c.iterator(); it.hasNext();)
			add(it.next());
	}

	/**
	 * 插入元素到队尾（super调用offer方法） public boolean add(E e) { if (offer(e)) return
	 * true; else throw new IllegalStateException("Queue full"); }
	 * 将指定的元素插入到此队列的尾部（如果立即可行且不会超过该队列的容量）， 在成功时返回 true，如果此队列已满，则抛出
	 * IllegalStateException。
	 */
	public boolean add(E e) {
		return super.add(e);
	}

	/**
	 * 将指定的元素插入到此队列的尾部（如果立即可行且不会超过该队列的容量）， 在成功时返回 true，如果此队列已满，则返回 false。
	 */
	public boolean offer(E e) {
		if (e == null)
			throw new NullPointerException();
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			if (count == items.length)
				return false;
			else {
				insert(e);
				return true;
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 将指定的元素插入此队列的尾部，如果该队列已满，则等待可用的空间。
	 */
	public void put(E e) throws InterruptedException {
		if (e == null)
			throw new NullPointerException();
		final E[] items = this.items;
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			try {
				while (count == items.length)
					notFull.await();
			} catch (InterruptedException ie) {
				notFull.signal(); // propagate to non-interrupted thread
				throw ie;
			}
			insert(e);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 将指定的元素插入此队列的尾部，如果该队列已满，则在到达指定的等待时间之前等待可用的空间。
	 */
	public boolean offer(E e, long timeout, TimeUnit unit)
			throws InterruptedException {
		if (e == null)
			throw new NullPointerException();
		long nanos = unit.toNanos(timeout);
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			for (;;) {
				if (count != items.length) {
					insert(e);
					return true;
				}
				if (nanos <= 0)// 如果时间到了就返回
					return false;
				try {
					nanos = notFull.awaitNanos(nanos);
				} catch (InterruptedException ie) {
					notFull.signal(); // propagate to non-interrupted thread
					throw ie;
				}
			}
		} finally {
			lock.unlock();
		}
	}

	// 获取并移除此队列的头，如果此队列为空，则返回 null。
	public E poll() {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			if (count == 0)
				return null;
			E x = extract();
			return x;
		} finally {
			lock.unlock();
		}
	}

	// 获取并移除此队列的头部，在元素变得可用之前一直等待（如果有必要）。
	public E take() throws InterruptedException {
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			try {
				while (count == 0)
					notEmpty.await();
			} catch (InterruptedException ie) {
				notEmpty.signal(); // propagate to non-interrupted thread
				throw ie;
			}
			E x = extract();
			return x;
		} finally {
			lock.unlock();
		}
	}

	// 获取并移除此队列的头部，在指定的等待时间前等待可用的元素（如果有必要）。
	public E poll(long timeout, TimeUnit unit) throws InterruptedException {
		long nanos = unit.toNanos(timeout);
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			for (;;) {
				if (count != 0) {
					E x = extract();
					return x;
				}
				if (nanos <= 0)
					return null;
				try {
					nanos = notEmpty.awaitNanos(nanos);
				} catch (InterruptedException ie) {
					notEmpty.signal(); // propagate to non-interrupted thread
					throw ie;
				}
			}
		} finally {
			lock.unlock();
		}
	}

	// 获取但不移除此队列的头；如果此队列为空，则返回 null。
	public E peek() {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			return (count == 0) ? null : items[takeIndex];
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 返回此队列中元素的数量。
	 */
	public int size() {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			return count;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 返回在无阻塞的理想情况下（不存在内存或资源约束）此队列能接受的其他元素数量。
	 */
	public int remainingCapacity() {
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			return items.length - count;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 从此队列中移除指定元素的单个实例（如果存在）。
	 */
	public boolean remove(Object o) {
		if (o == null)
			return false;
		final E[] items = this.items;
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			int i = takeIndex;
			int k = 0;
			for (;;) {
				if (k++ >= count)
					return false;
				if (o.equals(items[i])) {
					removeAt(i);
					return true;
				}
				i = inc(i);
			}
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 如果此队列包含指定的元素，则返回 true。
	 */
	public boolean contains(Object o) {
		if (o == null)
			return false;
		final E[] items = this.items;
		final ReentrantLock lock = this.lock;
		lock.lock();
		try {
			int i = takeIndex;
			int k = 0;
			while (k++ < count) {
				if (o.equals(items[i]))
					return true;
				i = inc(i);
			}
			return false;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Iterator<E> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int drainTo(Collection<? super E> arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int drainTo(Collection<? super E> arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}
}
