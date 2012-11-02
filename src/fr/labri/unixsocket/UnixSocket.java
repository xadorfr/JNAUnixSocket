package fr.labri.unixsocket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;


import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
import com.sun.jna.Platform;

import fr.labri.unixsocket.JNAUnixSocket.SockAddr;

/**
 * Adapted from the projet syslog4j
 * URL : www.syslog4j.org
 * 
 */
public class UnixSocket extends Socket {
	public static final String LIBC = "c";
	private static final int AF_UNIX = 1;
	private static final int SOCK_STREAM = 1;
	private static final int PROTOCOL = 0;

	private JNAUnixSocket library = null;
	private int sockfd = -1;
	private InputStream is = null;
	private OutputStream os = null;

	public UnixSocket(String path) throws SocketException {
		if (Platform.isWindows() || Platform.isWindowsCE()) {
			throw new SocketException(
					"loadLibrary(): Unix sockets are not supported on Windows platforms");
		}

		library = (JNAUnixSocket) Native.loadLibrary(LIBC, JNAUnixSocket.class);
		this.sockfd = socket(AF_UNIX, SOCK_STREAM, PROTOCOL);

		try {
			SockAddr sockAddr = new SockAddr();
			sockAddr.setPath(path);
			int i = this.connect(sockAddr, sockAddr.size());

			if (i != 0) {
				new SocketException(
						"UnixSocket(..): could not connect to socket");
			}

		} catch (LastErrorException lee) {
			throwSocketException("UnixSocket(..): could not connect to socket",
					lee);
		}

		this.is = new BufferedInputStream(new UnixSocketInputStream(this));
		this.os = new BufferedOutputStream(new UnixSocketOutputStream(this));
	}

	
	private void throwIOException(String prefixMessage, LastErrorException lee)
			throws IOException {
		String strerror = library.strerror(lee.getErrorCode());

		throw new IOException(prefixMessage + ": " + strerror);
	}

	private void throwSocketException(String prefixMessage,
			LastErrorException lee) throws SocketException {
		String strerror = library.strerror(lee.getErrorCode());

		throw new SocketException(prefixMessage + ": " + strerror);
	}

	private int socket(int domain, int type, int protocol)
			throws SocketException {
		try {
			int sockfd = library.socket(domain, type, protocol);

			return sockfd;

		} catch (LastErrorException lee) {
			throwSocketException("socket(..): could not open socket", lee);
			return -1;
		}
	}

	private int connect(SockAddr sockaddr, int addrlen) throws SocketException {
		try {
			int result = this.library.connect(this.sockfd, sockaddr, addrlen);

			return result;

		} catch (LastErrorException lee) {
			throwSocketException("connect(..): could not connect to socket",
					lee);
			return -1;
		}
	}
	
	/*
	 * used by UnixInputStream
	 */
	int read(byte[] buf, int count) throws IOException {
		try {
			ByteBuffer buffer = ByteBuffer.wrap(buf);

			int length = this.library.read(this.sockfd, buffer, count);

			return length;

		} catch (LastErrorException lee) {
			throwIOException("read(..): could not read from socket", lee);
			return -1;
		}
	}

	/* 
	 * Used by UnixOutputStream
	 */
	int write(byte[] buf, int count) throws IOException {
		try {
			ByteBuffer buffer = ByteBuffer.wrap(buf);

			int length = this.library.write(this.sockfd, buffer, count);

			return length;

		} catch (LastErrorException lee) {
			throwIOException("write(..): could not write to socket", lee);
			return -1;
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return is;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return os;
	}

	@Override
	public void shutdownInput() throws IOException {
		is = null;
	}

	@Override
	public void shutdownOutput() throws IOException {
		os = null;
	}

	@Override
	public synchronized void close() throws IOException {
		try {
			shutdownInput();
			shutdownOutput();
			this.library.close(this.sockfd);

		} catch (LastErrorException lee) {
			throwIOException("close(..): could not close socket", lee);
		}
	}
}