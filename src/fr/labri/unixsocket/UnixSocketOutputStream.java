package fr.labri.unixsocket;

import java.io.IOException;
import java.io.OutputStream;

public class UnixSocketOutputStream extends OutputStream {
	private UnixSocket unixSocket = null;

	public UnixSocketOutputStream(UnixSocket unixSocket) {
		this.unixSocket = unixSocket;
	}

	@Override
	public void flush() throws IOException {
		//
	}

	@Override
	public void write(byte[] data) throws IOException {
		write(data, 0, data.length);
	}

	@Override
	public void write(int data) throws IOException {
		write(new byte[] { (byte) data }, 0, 1);
	}

	@Override
	public void write(byte[] data, int offset, int length) throws IOException {
		if (offset == 0) {
			int writtenLength = this.unixSocket.write(data, length);
			// System.out.println("Wrote " + writtenLength + "/" + length + ": "
			// + new String(data,0,length));

			if (writtenLength != length) {
				throw new IOException("write(..): length is " + length
						+ " but only wrote " + writtenLength);
			}

		} else {
			throw new IOException("write(..): offset not supported");
		}
	}

	@Override
	public void close() throws IOException {
		//
	}
}