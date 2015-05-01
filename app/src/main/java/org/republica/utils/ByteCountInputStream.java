package org.republica.utils;

import android.support.annotation.NonNull;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An InputStream which counts the total number of bytes read and notifies a listener.
 *
 * @author Christophe Beyls
 */
public class ByteCountInputStream extends FilterInputStream {

    private final ByteCountListener listener;
    private final int interval;
    private int currentBytes = 0;
    private int nextStepBytes;

    public ByteCountInputStream(InputStream input, ByteCountListener listener, int interval) {
        super(input);
        if (listener == null) {
            throw new IllegalArgumentException("listener must not be null");
        }
        if (interval <= 0) {
            throw new IllegalArgumentException("interval must be at least 1 byte");
        }
        this.listener = listener;
        this.interval = interval;
        nextStepBytes = interval;
        listener.onNewCount(0);
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        addBytes((b == -1) ? -1 : 1);
        return b;
    }

    @Override
    public int read(@NonNull byte[] buffer, int offset, int max) throws IOException {
        int count = super.read(buffer, offset, max);
        addBytes(count);
        return count;
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public synchronized void mark(int readlimit) {
        throw new IllegalStateException();
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new IllegalStateException();
    }

    @Override
    public long skip(long byteCount) throws IOException {
        long count = super.skip(byteCount);
        addBytes((int) count);
        return count;
    }

    private void addBytes(int count) {
        if (count != -1) {
            currentBytes += count;
            if (currentBytes < nextStepBytes) {
                return;
            }
            nextStepBytes = currentBytes + interval;
        }
        listener.onNewCount(currentBytes);
    }

    public interface ByteCountListener {
        void onNewCount(int byteCount);
    }
}
