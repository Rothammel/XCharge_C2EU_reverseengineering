package org.apache.mina.util;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

/* loaded from: classes.dex */
public class AvailablePortFinder {
    public static final int MAX_PORT_NUMBER = 49151;
    public static final int MIN_PORT_NUMBER = 1;

    private AvailablePortFinder() {
    }

    public static Set<Integer> getAvailablePorts() {
        return getAvailablePorts(1, MAX_PORT_NUMBER);
    }

    public static int getNextAvailable() {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            ioe = e;
        }
        try {
            int port = serverSocket.getLocalPort();
            serverSocket.close();
            return port;
        } catch (IOException e2) {
            ioe = e2;
            throw new NoSuchElementException(ioe.getMessage());
        }
    }

    public static int getNextAvailable(int fromPort) {
        if (fromPort < 1 || fromPort > 49151) {
            throw new IllegalArgumentException("Invalid start port: " + fromPort);
        }
        for (int i = fromPort; i <= 49151; i++) {
            if (available(i)) {
                return i;
            }
        }
        throw new NoSuchElementException("Could not find an available port above " + fromPort);
    }

    public static boolean available(int port) {
        if (port < 1 || port > 49151) {
            throw new IllegalArgumentException("Invalid start port: " + port);
        }
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ServerSocket ss2 = new ServerSocket(port);
            try {
                ss2.setReuseAddress(true);
                DatagramSocket ds2 = new DatagramSocket(port);
                try {
                    ds2.setReuseAddress(true);
                    if (ds2 != null) {
                        ds2.close();
                    }
                    if (ss2 != null) {
                        try {
                            ss2.close();
                        } catch (IOException e) {
                        }
                    }
                    return true;
                } catch (IOException e2) {
                    ds = ds2;
                    ss = ss2;
                    if (ds != null) {
                        ds.close();
                    }
                    if (ss != null) {
                        try {
                            ss.close();
                        } catch (IOException e3) {
                        }
                    }
                    return false;
                } catch (Throwable th) {
                    th = th;
                    ds = ds2;
                    ss = ss2;
                    if (ds != null) {
                        ds.close();
                    }
                    if (ss != null) {
                        try {
                            ss.close();
                        } catch (IOException e4) {
                        }
                    }
                    throw th;
                }
            } catch (IOException e5) {
                ss = ss2;
            } catch (Throwable th2) {
                th = th2;
                ss = ss2;
            }
        } catch (IOException e6) {
        } catch (Throwable th3) {
            th = th3;
        }
    }

    public static Set<Integer> getAvailablePorts(int fromPort, int toPort) {
        ServerSocket s;
        if (fromPort < 1 || toPort > 49151 || fromPort > toPort) {
            throw new IllegalArgumentException("Invalid port range: " + fromPort + " ~ " + toPort);
        }
        Set<Integer> result = new TreeSet<>();
        for (int i = fromPort; i <= toPort; i++) {
            ServerSocket s2 = null;
            try {
                s = new ServerSocket(i);
            } catch (IOException e) {
            } catch (Throwable th) {
                th = th;
            }
            try {
                result.add(Integer.valueOf(i));
                if (s != null) {
                    try {
                        s.close();
                    } catch (IOException e2) {
                    }
                }
            } catch (IOException e3) {
                s2 = s;
                if (s2 != null) {
                    try {
                        s2.close();
                    } catch (IOException e4) {
                    }
                }
            } catch (Throwable th2) {
                th = th2;
                s2 = s;
                if (s2 != null) {
                    try {
                        s2.close();
                    } catch (IOException e5) {
                    }
                }
                throw th;
            }
        }
        return result;
    }
}