/*
 * This file is part of EconomyLite, licensed under the MIT License (MIT). See the LICENSE file at the root of this project for more information.
 */
package io.github.flibio.economylite;

import java.util.concurrent.Semaphore;

public class LockUtils implements AutoCloseable {
	static private Semaphore lock = new Semaphore(1);
	static private long threadID = 0;
	static private int numLock = 0;

	public LockUtils() {
		if (threadID > 0 && Thread.currentThread().getId() == threadID) {
			numLock++;
			return;
		}
		try {
			lock.acquire();
			threadID = Thread.currentThread().getId();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		if (numLock == 0) {
			threadID = 0;
			lock.release();
		} else {
			numLock--;
		}
	}
}
