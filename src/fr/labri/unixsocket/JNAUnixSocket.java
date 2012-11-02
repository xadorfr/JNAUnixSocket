package fr.labri.unixsocket;

import java.nio.ByteBuffer;
import com.sun.jna.Library;
import com.sun.jna.Structure;

interface JNAUnixSocket extends Library {
	
	static class SockAddr extends Structure {
		public final static int SUN_PATH_SIZE = 108;
		public final static byte[] ZERO_BYTE = new byte[] { 0 };

		public short family = 1;
		public byte[] path = new byte[SUN_PATH_SIZE];

		public void setPath(String sunPath) {
			System.arraycopy(sunPath.getBytes(), 0, this.path, 0, sunPath
					.length());
			System.arraycopy(ZERO_BYTE, 0, this.path, sunPath.length(), 1);
		}
	}

	public int socket(int domain, int type, int protocol);
	public int connect(int sockfd, SockAddr sockaddr, int addrlen);
	public int read(int fd, ByteBuffer buffer, int count);
	public int write(int fd, ByteBuffer buffer, int count);
	public int close(int fd);
	public String strerror(int errno);
}