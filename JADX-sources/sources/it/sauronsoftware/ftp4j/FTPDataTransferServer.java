package it.sauronsoftware.ftp4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.StringTokenizer;

/* loaded from: classes.dex */
class FTPDataTransferServer implements FTPDataTransferConnectionProvider, Runnable {
    private IOException exception;
    private ServerSocket serverSocket;
    private Socket socket;
    private Thread thread;

    public FTPDataTransferServer() throws FTPDataTransferException {
        int v1;
        int v2;
        this.serverSocket = null;
        boolean useRange = false;
        String aux = System.getProperty(FTPKeys.ACTIVE_DT_PORT_RANGE);
        int start = 0;
        int stop = 0;
        if (aux != null) {
            boolean valid = false;
            StringTokenizer st = new StringTokenizer(aux, "-");
            if (st.countTokens() == 2) {
                String s1 = st.nextToken();
                String s2 = st.nextToken();
                try {
                    v1 = Integer.parseInt(s1);
                } catch (NumberFormatException e) {
                    v1 = 0;
                }
                try {
                    v2 = Integer.parseInt(s2);
                } catch (NumberFormatException e2) {
                    v2 = 0;
                }
                if (v1 > 0 && v2 > 0 && v2 >= v1) {
                    start = v1;
                    stop = v2;
                    valid = true;
                    useRange = true;
                }
            }
            if (!valid) {
                System.err.println(new StringBuffer().append("WARNING: invalid value \"").append(aux).append("\" for the ").append(FTPKeys.ACTIVE_DT_PORT_RANGE).append(" system property. The value should ").append("be in the start-stop form, with ").append("start > 0, stop > 0 and start <= stop.").toString());
            }
        }
        if (useRange) {
            ArrayList availables = new ArrayList();
            for (int i = start; i <= stop; i++) {
                availables.add(new Integer(i));
            }
            boolean done = false;
            while (!done) {
                int size = availables.size();
                if (size <= 0) {
                    break;
                }
                int rand = (int) Math.floor(Math.random() * size);
                int port = ((Integer) availables.remove(rand)).intValue();
                try {
                    this.serverSocket = new ServerSocket();
                    this.serverSocket.setReceiveBufferSize(524288);
                    this.serverSocket.bind(new InetSocketAddress(port));
                    done = true;
                } catch (IOException e3) {
                }
            }
            if (!done) {
                throw new FTPDataTransferException(new StringBuffer().append("Cannot open the ServerSocket. No available port found in range ").append(aux).toString());
            }
        } else {
            try {
                this.serverSocket = new ServerSocket();
                this.serverSocket.setReceiveBufferSize(524288);
                this.serverSocket.bind(new InetSocketAddress(0));
            } catch (IOException e4) {
                throw new FTPDataTransferException("Cannot open the ServerSocket", e4);
            }
        }
        this.thread = new Thread(this);
        this.thread.start();
    }

    public int getPort() {
        return this.serverSocket.getLocalPort();
    }

    @Override // java.lang.Runnable
    public void run() {
        int value;
        int timeout = 30000;
        String aux = System.getProperty(FTPKeys.ACTIVE_DT_ACCEPT_TIMEOUT);
        if (aux != null) {
            boolean valid = false;
            try {
                value = Integer.parseInt(aux);
            } catch (NumberFormatException e) {
                value = -1;
            }
            if (value >= 0) {
                timeout = value;
                valid = true;
            }
            if (!valid) {
                System.err.println(new StringBuffer().append("WARNING: invalid value \"").append(aux).append("\" for the ").append(FTPKeys.ACTIVE_DT_ACCEPT_TIMEOUT).append(" system property. The value should ").append("be an integer greater or equal to 0.").toString());
            }
        }
        try {
            try {
                this.serverSocket.setSoTimeout(timeout);
                this.socket = this.serverSocket.accept();
                this.socket.setSendBufferSize(524288);
            } finally {
                try {
                    this.serverSocket.close();
                } catch (IOException e2) {
                }
            }
        } catch (IOException e3) {
            this.exception = e3;
            try {
                this.serverSocket.close();
            } catch (IOException e4) {
            }
        }
    }

    @Override // it.sauronsoftware.ftp4j.FTPDataTransferConnectionProvider
    public void dispose() {
        if (this.serverSocket != null) {
            try {
                this.serverSocket.close();
            } catch (IOException e) {
            }
        }
    }

    @Override // it.sauronsoftware.ftp4j.FTPDataTransferConnectionProvider
    public Socket openDataTransferConnection() throws FTPDataTransferException {
        if (this.socket == null && this.exception == null) {
            try {
                this.thread.join();
            } catch (Exception e) {
            }
        }
        if (this.exception != null) {
            throw new FTPDataTransferException("Cannot receive the incoming connection", this.exception);
        }
        if (this.socket == null) {
            throw new FTPDataTransferException("No socket available");
        }
        return this.socket;
    }
}
