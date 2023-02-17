package p010it.sauronsoftware.ftp4j;

import android.support.p000v4.view.MotionEventCompat;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import javax.net.ssl.SSLSocketFactory;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.mina.proxy.handlers.http.HttpProxyConstants;
import p010it.sauronsoftware.ftp4j.connectors.DirectConnector;
import p010it.sauronsoftware.ftp4j.extrecognizers.DefaultTextualExtensionRecognizer;
import p010it.sauronsoftware.ftp4j.listparsers.DOSListParser;
import p010it.sauronsoftware.ftp4j.listparsers.EPLFListParser;
import p010it.sauronsoftware.ftp4j.listparsers.MLSDListParser;
import p010it.sauronsoftware.ftp4j.listparsers.NetWareListParser;
import p010it.sauronsoftware.ftp4j.listparsers.UnixListParser;

/* renamed from: it.sauronsoftware.ftp4j.FTPClient */
public class FTPClient {
    private static final DateFormat MDTM_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    public static final int MLSD_ALWAYS = 1;
    public static final int MLSD_IF_SUPPORTED = 0;
    public static final int MLSD_NEVER = 2;
    private static final Pattern PASV_PATTERN = Pattern.compile("\\d{1,3},\\d{1,3},\\d{1,3},\\d{1,3},\\d{1,3},\\d{1,3}");
    private static final Pattern PWD_PATTERN = Pattern.compile("\"/.*\"");
    public static final int SECURITY_FTP = 0;
    public static final int SECURITY_FTPES = 2;
    public static final int SECURITY_FTPS = 1;
    private static final int SEND_AND_RECEIVE_BUFFER_SIZE = 65536;
    public static final int TYPE_AUTO = 0;
    public static final int TYPE_BINARY = 2;
    public static final int TYPE_TEXTUAL = 1;
    private Object abortLock = new Object();
    private boolean aborted = false;
    private boolean authenticated = false;
    private long autoNoopTimeout = 0;
    private AutoNoopTimer autoNoopTimer;
    private String charset = null;
    private FTPCommunicationChannel communication = null;
    private ArrayList communicationListeners = new ArrayList();
    private boolean compressionEnabled = false;
    private boolean connected = false;
    private FTPConnector connector = new DirectConnector();
    private boolean consumeAborCommandReply = false;
    private boolean dataChannelEncrypted = false;
    private InputStream dataTransferInputStream = null;
    private OutputStream dataTransferOutputStream = null;
    private String host = null;
    private ArrayList listParsers = new ArrayList();
    private Object lock = new Object();
    private int mlsdPolicy = 0;
    private boolean mlsdSupported = false;
    private boolean modezEnabled = false;
    private boolean modezSupported = false;
    private long nextAutoNoopTime;
    private boolean ongoingDataTransfer = false;
    private FTPListParser parser = null;
    private boolean passive = true;
    private String password;
    private int port = 0;
    private boolean restSupported = false;
    private int security = 0;
    private SSLSocketFactory sslSocketFactory = ((SSLSocketFactory) SSLSocketFactory.getDefault());
    private FTPTextualExtensionRecognizer textualExtensionRecognizer = DefaultTextualExtensionRecognizer.getInstance();
    private int type = 0;
    private String username;
    private boolean utf8Supported = false;

    static boolean access$000(FTPClient x0) {
        return x0.dataChannelEncrypted;
    }

    static Socket access$100(FTPClient x0, Socket x1, String x2, int x3) throws IOException {
        return x0.ssl(x1, x2, x3);
    }

    static FTPConnector access$200(FTPClient x0) {
        return x0.connector;
    }

    static String access$300(FTPClient x0) {
        return x0.host;
    }

    static Object access$500(FTPClient x0) {
        return x0.lock;
    }

    static long access$600(FTPClient x0) {
        return x0.nextAutoNoopTime;
    }

    static long access$602(FTPClient x0, long x1) {
        x0.nextAutoNoopTime = x1;
        return x1;
    }

    static long access$700(FTPClient x0) {
        return x0.autoNoopTimeout;
    }

    public FTPClient() {
        addListParser(new UnixListParser());
        addListParser(new DOSListParser());
        addListParser(new EPLFListParser());
        addListParser(new NetWareListParser());
        addListParser(new MLSDListParser());
    }

    public FTPConnector getConnector() {
        FTPConnector fTPConnector;
        synchronized (this.lock) {
            fTPConnector = this.connector;
        }
        return fTPConnector;
    }

    public void setConnector(FTPConnector connector2) {
        synchronized (this.lock) {
            this.connector = connector2;
        }
    }

    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory2) {
        synchronized (this.lock) {
            this.sslSocketFactory = sslSocketFactory2;
        }
    }

    public SSLSocketFactory getSSLSocketFactory() {
        SSLSocketFactory sSLSocketFactory;
        synchronized (this.lock) {
            sSLSocketFactory = this.sslSocketFactory;
        }
        return sSLSocketFactory;
    }

    public void setSecurity(int security2) throws IllegalStateException, IllegalArgumentException {
        if (security2 == 0 || security2 == 1 || security2 == 2) {
            synchronized (this.lock) {
                if (this.connected) {
                    throw new IllegalStateException("The security level of the connection can't be changed while the client is connected");
                }
                this.security = security2;
            }
            return;
        }
        throw new IllegalArgumentException("Invalid security");
    }

    public int getSecurity() {
        return this.security;
    }

    private Socket ssl(Socket socket, String host2, int port2) throws IOException {
        return this.sslSocketFactory.createSocket(socket, host2, port2, true);
    }

    public void setPassive(boolean passive2) {
        synchronized (this.lock) {
            this.passive = passive2;
        }
    }

    public void setType(int type2) throws IllegalArgumentException {
        if (type2 == 0 || type2 == 2 || type2 == 1) {
            synchronized (this.lock) {
                this.type = type2;
            }
            return;
        }
        throw new IllegalArgumentException("Invalid type");
    }

    public int getType() {
        int i;
        synchronized (this.lock) {
            i = this.type;
        }
        return i;
    }

    public void setMLSDPolicy(int mlsdPolicy2) throws IllegalArgumentException {
        if (this.type == 0 || this.type == 1 || this.type == 2) {
            synchronized (this.lock) {
                this.mlsdPolicy = mlsdPolicy2;
            }
            return;
        }
        throw new IllegalArgumentException("Invalid MLSD policy");
    }

    public int getMLSDPolicy() {
        int i;
        synchronized (this.lock) {
            i = this.mlsdPolicy;
        }
        return i;
    }

    public String getCharset() {
        String str;
        synchronized (this.lock) {
            str = this.charset;
        }
        return str;
    }

    public void setCharset(String charset2) {
        synchronized (this.lock) {
            this.charset = charset2;
            if (this.connected) {
                try {
                    this.communication.changeCharset(pickCharset());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isResumeSupported() {
        boolean z;
        synchronized (this.lock) {
            z = this.restSupported;
        }
        return z;
    }

    public boolean isCompressionSupported() {
        return this.modezSupported;
    }

    public void setCompressionEnabled(boolean compressionEnabled2) {
        this.compressionEnabled = compressionEnabled2;
    }

    public boolean isCompressionEnabled() {
        return this.compressionEnabled;
    }

    public FTPTextualExtensionRecognizer getTextualExtensionRecognizer() {
        FTPTextualExtensionRecognizer fTPTextualExtensionRecognizer;
        synchronized (this.lock) {
            fTPTextualExtensionRecognizer = this.textualExtensionRecognizer;
        }
        return fTPTextualExtensionRecognizer;
    }

    public void setTextualExtensionRecognizer(FTPTextualExtensionRecognizer textualExtensionRecognizer2) {
        synchronized (this.lock) {
            this.textualExtensionRecognizer = textualExtensionRecognizer2;
        }
    }

    public boolean isAuthenticated() {
        boolean z;
        synchronized (this.lock) {
            z = this.authenticated;
        }
        return z;
    }

    public boolean isConnected() {
        boolean z;
        synchronized (this.lock) {
            z = this.connected;
        }
        return z;
    }

    public boolean isPassive() {
        boolean z;
        synchronized (this.lock) {
            z = this.passive;
        }
        return z;
    }

    public String getHost() {
        String str;
        synchronized (this.lock) {
            str = this.host;
        }
        return str;
    }

    public int getPort() {
        int i;
        synchronized (this.lock) {
            i = this.port;
        }
        return i;
    }

    public String getPassword() {
        String str;
        synchronized (this.lock) {
            str = this.password;
        }
        return str;
    }

    public String getUsername() {
        String str;
        synchronized (this.lock) {
            str = this.username;
        }
        return str;
    }

    public void setAutoNoopTimeout(long autoNoopTimeout2) {
        synchronized (this.lock) {
            if (this.connected && this.authenticated) {
                stopAutoNoopTimer();
            }
            long oldValue = this.autoNoopTimeout;
            long newValue = autoNoopTimeout2;
            this.autoNoopTimeout = autoNoopTimeout2;
            if (!(oldValue == 0 || newValue == 0 || this.nextAutoNoopTime <= 0)) {
                this.nextAutoNoopTime -= oldValue - newValue;
            }
            if (this.connected && this.authenticated) {
                startAutoNoopTimer();
            }
        }
    }

    public long getAutoNoopTimeout() {
        long j;
        synchronized (this.lock) {
            j = this.autoNoopTimeout;
        }
        return j;
    }

    public void addCommunicationListener(FTPCommunicationListener listener) {
        synchronized (this.lock) {
            this.communicationListeners.add(listener);
            if (this.communication != null) {
                this.communication.addCommunicationListener(listener);
            }
        }
    }

    public void removeCommunicationListener(FTPCommunicationListener listener) {
        synchronized (this.lock) {
            this.communicationListeners.remove(listener);
            if (this.communication != null) {
                this.communication.removeCommunicationListener(listener);
            }
        }
    }

    public FTPCommunicationListener[] getCommunicationListeners() {
        FTPCommunicationListener[] ret;
        synchronized (this.lock) {
            int size = this.communicationListeners.size();
            ret = new FTPCommunicationListener[size];
            for (int i = 0; i < size; i++) {
                ret[i] = (FTPCommunicationListener) this.communicationListeners.get(i);
            }
        }
        return ret;
    }

    public void addListParser(FTPListParser listParser) {
        synchronized (this.lock) {
            this.listParsers.add(listParser);
        }
    }

    public void removeListParser(FTPListParser listParser) {
        synchronized (this.lock) {
            this.listParsers.remove(listParser);
        }
    }

    public FTPListParser[] getListParsers() {
        FTPListParser[] ret;
        synchronized (this.lock) {
            int size = this.listParsers.size();
            ret = new FTPListParser[size];
            for (int i = 0; i < size; i++) {
                ret[i] = (FTPListParser) this.listParsers.get(i);
            }
        }
        return ret;
    }

    public String[] connect(String host2) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        int def;
        if (this.security == 1) {
            def = 990;
        } else {
            def = 21;
        }
        return connect(host2, def);
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:29:0x006c=Splitter:B:29:0x006c, B:43:0x00ae=Splitter:B:43:0x00ae} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String[] connect(java.lang.String r9, int r10) throws java.lang.IllegalStateException, java.io.IOException, p010it.sauronsoftware.ftp4j.FTPIllegalReplyException, p010it.sauronsoftware.ftp4j.FTPException {
        /*
            r8 = this;
            r6 = 1
            java.lang.Object r5 = r8.lock
            monitor-enter(r5)
            boolean r4 = r8.connected     // Catch:{ all -> 0x002b }
            if (r4 == 0) goto L_0x002e
            java.lang.IllegalStateException r4 = new java.lang.IllegalStateException     // Catch:{ all -> 0x002b }
            java.lang.StringBuffer r6 = new java.lang.StringBuffer     // Catch:{ all -> 0x002b }
            r6.<init>()     // Catch:{ all -> 0x002b }
            java.lang.String r7 = "Client already connected to "
            java.lang.StringBuffer r6 = r6.append(r7)     // Catch:{ all -> 0x002b }
            java.lang.StringBuffer r6 = r6.append(r9)     // Catch:{ all -> 0x002b }
            java.lang.String r7 = " on port "
            java.lang.StringBuffer r6 = r6.append(r7)     // Catch:{ all -> 0x002b }
            java.lang.StringBuffer r6 = r6.append(r10)     // Catch:{ all -> 0x002b }
            java.lang.String r6 = r6.toString()     // Catch:{ all -> 0x002b }
            r4.<init>(r6)     // Catch:{ all -> 0x002b }
            throw r4     // Catch:{ all -> 0x002b }
        L_0x002b:
            r4 = move-exception
            monitor-exit(r5)     // Catch:{ all -> 0x002b }
            throw r4
        L_0x002e:
            r0 = 0
            it.sauronsoftware.ftp4j.FTPConnector r4 = r8.connector     // Catch:{ IOException -> 0x0060 }
            java.net.Socket r0 = r4.connectForCommunicationChannel(r9, r10)     // Catch:{ IOException -> 0x0060 }
            int r4 = r8.security     // Catch:{ IOException -> 0x0060 }
            if (r4 != r6) goto L_0x003d
            java.net.Socket r0 = r8.ssl(r0, r9, r10)     // Catch:{ IOException -> 0x0060 }
        L_0x003d:
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r4 = new it.sauronsoftware.ftp4j.FTPCommunicationChannel     // Catch:{ IOException -> 0x0060 }
            java.lang.String r6 = r8.pickCharset()     // Catch:{ IOException -> 0x0060 }
            r4.<init>(r0, r6)     // Catch:{ IOException -> 0x0060 }
            r8.communication = r4     // Catch:{ IOException -> 0x0060 }
            java.util.ArrayList r4 = r8.communicationListeners     // Catch:{ IOException -> 0x0060 }
            java.util.Iterator r2 = r4.iterator()     // Catch:{ IOException -> 0x0060 }
        L_0x004e:
            boolean r4 = r2.hasNext()     // Catch:{ IOException -> 0x0060 }
            if (r4 == 0) goto L_0x006d
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r6 = r8.communication     // Catch:{ IOException -> 0x0060 }
            java.lang.Object r4 = r2.next()     // Catch:{ IOException -> 0x0060 }
            it.sauronsoftware.ftp4j.FTPCommunicationListener r4 = (p010it.sauronsoftware.ftp4j.FTPCommunicationListener) r4     // Catch:{ IOException -> 0x0060 }
            r6.addCommunicationListener(r4)     // Catch:{ IOException -> 0x0060 }
            goto L_0x004e
        L_0x0060:
            r1 = move-exception
            throw r1     // Catch:{ all -> 0x0062 }
        L_0x0062:
            r4 = move-exception
            boolean r6 = r8.connected     // Catch:{ all -> 0x002b }
            if (r6 != 0) goto L_0x006c
            if (r0 == 0) goto L_0x006c
            r0.close()     // Catch:{ Throwable -> 0x00b2 }
        L_0x006c:
            throw r4     // Catch:{ all -> 0x002b }
        L_0x006d:
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r4 = r8.communication     // Catch:{ IOException -> 0x0060 }
            it.sauronsoftware.ftp4j.FTPReply r3 = r4.readFTPReply()     // Catch:{ IOException -> 0x0060 }
            boolean r4 = r3.isSuccessCode()     // Catch:{ IOException -> 0x0060 }
            if (r4 != 0) goto L_0x007f
            it.sauronsoftware.ftp4j.FTPException r4 = new it.sauronsoftware.ftp4j.FTPException     // Catch:{ IOException -> 0x0060 }
            r4.<init>((p010it.sauronsoftware.ftp4j.FTPReply) r3)     // Catch:{ IOException -> 0x0060 }
            throw r4     // Catch:{ IOException -> 0x0060 }
        L_0x007f:
            r4 = 1
            r8.connected = r4     // Catch:{ IOException -> 0x0060 }
            r4 = 0
            r8.authenticated = r4     // Catch:{ IOException -> 0x0060 }
            r4 = 0
            r8.parser = r4     // Catch:{ IOException -> 0x0060 }
            r8.host = r9     // Catch:{ IOException -> 0x0060 }
            r8.port = r10     // Catch:{ IOException -> 0x0060 }
            r4 = 0
            r8.username = r4     // Catch:{ IOException -> 0x0060 }
            r4 = 0
            r8.password = r4     // Catch:{ IOException -> 0x0060 }
            r4 = 0
            r8.utf8Supported = r4     // Catch:{ IOException -> 0x0060 }
            r4 = 0
            r8.restSupported = r4     // Catch:{ IOException -> 0x0060 }
            r4 = 0
            r8.mlsdSupported = r4     // Catch:{ IOException -> 0x0060 }
            r4 = 0
            r8.modezSupported = r4     // Catch:{ IOException -> 0x0060 }
            r4 = 0
            r8.dataChannelEncrypted = r4     // Catch:{ IOException -> 0x0060 }
            java.lang.String[] r4 = r3.getMessages()     // Catch:{ IOException -> 0x0060 }
            boolean r6 = r8.connected     // Catch:{ all -> 0x002b }
            if (r6 != 0) goto L_0x00ae
            if (r0 == 0) goto L_0x00ae
            r0.close()     // Catch:{ Throwable -> 0x00b0 }
        L_0x00ae:
            monitor-exit(r5)     // Catch:{ all -> 0x002b }
            return r4
        L_0x00b0:
            r6 = move-exception
            goto L_0x00ae
        L_0x00b2:
            r6 = move-exception
            goto L_0x006c
        */
        throw new UnsupportedOperationException("Method not decompiled: p010it.sauronsoftware.ftp4j.FTPClient.connect(java.lang.String, int):java.lang.String[]");
    }

    public void abortCurrentConnectionAttempt() {
        this.connector.abortConnectForCommunicationChannel();
    }

    public void disconnect(boolean sendQuitCommand) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            }
            if (this.authenticated) {
                stopAutoNoopTimer();
            }
            if (sendQuitCommand) {
                this.communication.sendFTPCommand("QUIT");
                FTPReply r = this.communication.readFTPReply();
                if (!r.isSuccessCode()) {
                    throw new FTPException(r);
                }
            }
            this.communication.close();
            this.communication = null;
            this.connected = false;
        }
    }

    public void abruptlyCloseCommunication() {
        if (this.communication != null) {
            this.communication.close();
            this.communication = null;
        }
        this.connected = false;
        stopAutoNoopTimer();
    }

    public void login(String username2, String password2) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        login(username2, password2, (String) null);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x005e, code lost:
        throw new p010it.sauronsoftware.ftp4j.FTPException(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0088, code lost:
        if (r1 == false) goto L_0x00c6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x008a, code lost:
        if (r9 != null) goto L_0x009a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0093, code lost:
        throw new p010it.sauronsoftware.ftp4j.FTPException((int) p010it.sauronsoftware.ftp4j.FTPCodes.USERNAME_OK);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x009a, code lost:
        r7.communication.sendFTPCommand(new java.lang.StringBuffer().append("PASS ").append(r9).toString());
        r2 = r7.communication.readFTPReply();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00bc, code lost:
        switch(r2.getCode()) {
            case it.sauronsoftware.ftp4j.FTPCodes.USER_LOGGED_IN :int: goto L_0x00c5;
            case it.sauronsoftware.ftp4j.FTPCodes.NEED_ACCOUNT :int: goto L_0x00d2;
            default: goto L_0x00bf;
        };
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00c4, code lost:
        throw new p010it.sauronsoftware.ftp4j.FTPException(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00c5, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00c6, code lost:
        if (r0 == false) goto L_0x00ff;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00c8, code lost:
        if (r10 != null) goto L_0x00d4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00d1, code lost:
        throw new p010it.sauronsoftware.ftp4j.FTPException((int) p010it.sauronsoftware.ftp4j.FTPCodes.NEED_ACCOUNT);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00d2, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00d4, code lost:
        r7.communication.sendFTPCommand(new java.lang.StringBuffer().append("ACCT ").append(r10).toString());
        r2 = r7.communication.readFTPReply();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00f6, code lost:
        switch(r2.getCode()) {
            case it.sauronsoftware.ftp4j.FTPCodes.USER_LOGGED_IN :int: goto L_0x00ff;
            default: goto L_0x00f9;
        };
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00fe, code lost:
        throw new p010it.sauronsoftware.ftp4j.FTPException(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00ff, code lost:
        r7.authenticated = true;
        r7.username = r8;
        r7.password = r9;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void login(java.lang.String r8, java.lang.String r9, java.lang.String r10) throws java.lang.IllegalStateException, java.io.IOException, p010it.sauronsoftware.ftp4j.FTPIllegalReplyException, p010it.sauronsoftware.ftp4j.FTPException {
        /*
            r7 = this;
            java.lang.Object r4 = r7.lock
            monitor-enter(r4)
            boolean r3 = r7.connected     // Catch:{ all -> 0x000f }
            if (r3 != 0) goto L_0x0012
            java.lang.IllegalStateException r3 = new java.lang.IllegalStateException     // Catch:{ all -> 0x000f }
            java.lang.String r5 = "Client not connected"
            r3.<init>(r5)     // Catch:{ all -> 0x000f }
            throw r3     // Catch:{ all -> 0x000f }
        L_0x000f:
            r3 = move-exception
            monitor-exit(r4)     // Catch:{ all -> 0x000f }
            throw r3
        L_0x0012:
            int r3 = r7.security     // Catch:{ all -> 0x000f }
            r5 = 2
            if (r3 != r5) goto L_0x0031
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r3 = r7.communication     // Catch:{ all -> 0x000f }
            java.lang.String r5 = "AUTH TLS"
            r3.sendFTPCommand(r5)     // Catch:{ all -> 0x000f }
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r3 = r7.communication     // Catch:{ all -> 0x000f }
            it.sauronsoftware.ftp4j.FTPReply r2 = r3.readFTPReply()     // Catch:{ all -> 0x000f }
            boolean r3 = r2.isSuccessCode()     // Catch:{ all -> 0x000f }
            if (r3 == 0) goto L_0x005f
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r3 = r7.communication     // Catch:{ all -> 0x000f }
            javax.net.ssl.SSLSocketFactory r5 = r7.sslSocketFactory     // Catch:{ all -> 0x000f }
            r3.ssl(r5)     // Catch:{ all -> 0x000f }
        L_0x0031:
            r3 = 0
            r7.authenticated = r3     // Catch:{ all -> 0x000f }
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r3 = r7.communication     // Catch:{ all -> 0x000f }
            java.lang.StringBuffer r5 = new java.lang.StringBuffer     // Catch:{ all -> 0x000f }
            r5.<init>()     // Catch:{ all -> 0x000f }
            java.lang.String r6 = "USER "
            java.lang.StringBuffer r5 = r5.append(r6)     // Catch:{ all -> 0x000f }
            java.lang.StringBuffer r5 = r5.append(r8)     // Catch:{ all -> 0x000f }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x000f }
            r3.sendFTPCommand(r5)     // Catch:{ all -> 0x000f }
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r3 = r7.communication     // Catch:{ all -> 0x000f }
            it.sauronsoftware.ftp4j.FTPReply r2 = r3.readFTPReply()     // Catch:{ all -> 0x000f }
            int r3 = r2.getCode()     // Catch:{ all -> 0x000f }
            switch(r3) {
                case 230: goto L_0x0086;
                case 331: goto L_0x0094;
                case 332: goto L_0x0097;
                default: goto L_0x0059;
            }     // Catch:{ all -> 0x000f }
        L_0x0059:
            it.sauronsoftware.ftp4j.FTPException r3 = new it.sauronsoftware.ftp4j.FTPException     // Catch:{ all -> 0x000f }
            r3.<init>((p010it.sauronsoftware.ftp4j.FTPReply) r2)     // Catch:{ all -> 0x000f }
            throw r3     // Catch:{ all -> 0x000f }
        L_0x005f:
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r3 = r7.communication     // Catch:{ all -> 0x000f }
            java.lang.String r5 = "AUTH SSL"
            r3.sendFTPCommand(r5)     // Catch:{ all -> 0x000f }
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r3 = r7.communication     // Catch:{ all -> 0x000f }
            it.sauronsoftware.ftp4j.FTPReply r2 = r3.readFTPReply()     // Catch:{ all -> 0x000f }
            boolean r3 = r2.isSuccessCode()     // Catch:{ all -> 0x000f }
            if (r3 == 0) goto L_0x007a
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r3 = r7.communication     // Catch:{ all -> 0x000f }
            javax.net.ssl.SSLSocketFactory r5 = r7.sslSocketFactory     // Catch:{ all -> 0x000f }
            r3.ssl(r5)     // Catch:{ all -> 0x000f }
            goto L_0x0031
        L_0x007a:
            it.sauronsoftware.ftp4j.FTPException r3 = new it.sauronsoftware.ftp4j.FTPException     // Catch:{ all -> 0x000f }
            int r5 = r2.getCode()     // Catch:{ all -> 0x000f }
            java.lang.String r6 = "SECURITY_FTPES cannot be applied: the server refused both AUTH TLS and AUTH SSL commands"
            r3.<init>(r5, r6)     // Catch:{ all -> 0x000f }
            throw r3     // Catch:{ all -> 0x000f }
        L_0x0086:
            r1 = 0
            r0 = 0
        L_0x0088:
            if (r1 == 0) goto L_0x00c6
            if (r9 != 0) goto L_0x009a
            it.sauronsoftware.ftp4j.FTPException r3 = new it.sauronsoftware.ftp4j.FTPException     // Catch:{ all -> 0x000f }
            r5 = 331(0x14b, float:4.64E-43)
            r3.<init>((int) r5)     // Catch:{ all -> 0x000f }
            throw r3     // Catch:{ all -> 0x000f }
        L_0x0094:
            r1 = 1
            r0 = 0
            goto L_0x0088
        L_0x0097:
            r1 = 0
            r0 = 1
            goto L_0x0059
        L_0x009a:
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r3 = r7.communication     // Catch:{ all -> 0x000f }
            java.lang.StringBuffer r5 = new java.lang.StringBuffer     // Catch:{ all -> 0x000f }
            r5.<init>()     // Catch:{ all -> 0x000f }
            java.lang.String r6 = "PASS "
            java.lang.StringBuffer r5 = r5.append(r6)     // Catch:{ all -> 0x000f }
            java.lang.StringBuffer r5 = r5.append(r9)     // Catch:{ all -> 0x000f }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x000f }
            r3.sendFTPCommand(r5)     // Catch:{ all -> 0x000f }
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r3 = r7.communication     // Catch:{ all -> 0x000f }
            it.sauronsoftware.ftp4j.FTPReply r2 = r3.readFTPReply()     // Catch:{ all -> 0x000f }
            int r3 = r2.getCode()     // Catch:{ all -> 0x000f }
            switch(r3) {
                case 230: goto L_0x00c5;
                case 332: goto L_0x00d2;
                default: goto L_0x00bf;
            }     // Catch:{ all -> 0x000f }
        L_0x00bf:
            it.sauronsoftware.ftp4j.FTPException r3 = new it.sauronsoftware.ftp4j.FTPException     // Catch:{ all -> 0x000f }
            r3.<init>((p010it.sauronsoftware.ftp4j.FTPReply) r2)     // Catch:{ all -> 0x000f }
            throw r3     // Catch:{ all -> 0x000f }
        L_0x00c5:
            r0 = 0
        L_0x00c6:
            if (r0 == 0) goto L_0x00ff
            if (r10 != 0) goto L_0x00d4
            it.sauronsoftware.ftp4j.FTPException r3 = new it.sauronsoftware.ftp4j.FTPException     // Catch:{ all -> 0x000f }
            r5 = 332(0x14c, float:4.65E-43)
            r3.<init>((int) r5)     // Catch:{ all -> 0x000f }
            throw r3     // Catch:{ all -> 0x000f }
        L_0x00d2:
            r0 = 1
            goto L_0x00c6
        L_0x00d4:
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r3 = r7.communication     // Catch:{ all -> 0x000f }
            java.lang.StringBuffer r5 = new java.lang.StringBuffer     // Catch:{ all -> 0x000f }
            r5.<init>()     // Catch:{ all -> 0x000f }
            java.lang.String r6 = "ACCT "
            java.lang.StringBuffer r5 = r5.append(r6)     // Catch:{ all -> 0x000f }
            java.lang.StringBuffer r5 = r5.append(r10)     // Catch:{ all -> 0x000f }
            java.lang.String r5 = r5.toString()     // Catch:{ all -> 0x000f }
            r3.sendFTPCommand(r5)     // Catch:{ all -> 0x000f }
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r3 = r7.communication     // Catch:{ all -> 0x000f }
            it.sauronsoftware.ftp4j.FTPReply r2 = r3.readFTPReply()     // Catch:{ all -> 0x000f }
            int r3 = r2.getCode()     // Catch:{ all -> 0x000f }
            switch(r3) {
                case 230: goto L_0x00ff;
                default: goto L_0x00f9;
            }     // Catch:{ all -> 0x000f }
        L_0x00f9:
            it.sauronsoftware.ftp4j.FTPException r3 = new it.sauronsoftware.ftp4j.FTPException     // Catch:{ all -> 0x000f }
            r3.<init>((p010it.sauronsoftware.ftp4j.FTPReply) r2)     // Catch:{ all -> 0x000f }
            throw r3     // Catch:{ all -> 0x000f }
        L_0x00ff:
            r3 = 1
            r7.authenticated = r3     // Catch:{ all -> 0x000f }
            r7.username = r8     // Catch:{ all -> 0x000f }
            r7.password = r9     // Catch:{ all -> 0x000f }
            monitor-exit(r4)     // Catch:{ all -> 0x000f }
            r7.postLoginOperations()
            r7.startAutoNoopTimer()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: p010it.sauronsoftware.ftp4j.FTPClient.login(java.lang.String, java.lang.String, java.lang.String):void");
    }

    private void postLoginOperations() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        synchronized (this.lock) {
            this.utf8Supported = false;
            this.restSupported = false;
            this.mlsdSupported = false;
            this.modezSupported = false;
            this.dataChannelEncrypted = false;
            this.communication.sendFTPCommand("FEAT");
            FTPReply r = this.communication.readFTPReply();
            if (r.getCode() == 211) {
                String[] lines = r.getMessages();
                for (int i = 1; i < lines.length - 1; i++) {
                    String feat = lines[i].trim().toUpperCase();
                    if ("REST STREAM".equalsIgnoreCase(feat)) {
                        this.restSupported = true;
                    } else if ("UTF8".equalsIgnoreCase(feat)) {
                        this.utf8Supported = true;
                        this.communication.changeCharset(CharEncoding.UTF_8);
                    } else if ("MLSD".equalsIgnoreCase(feat)) {
                        this.mlsdSupported = true;
                    } else if ("MODE Z".equalsIgnoreCase(feat) || feat.startsWith("MODE Z ")) {
                        this.modezSupported = true;
                    }
                }
            }
            if (this.utf8Supported) {
                this.communication.sendFTPCommand("OPTS UTF8 ON");
                this.communication.readFTPReply();
            }
            if (this.security == 1 || this.security == 2) {
                this.communication.sendFTPCommand("PBSZ 0");
                this.communication.readFTPReply();
                this.communication.sendFTPCommand("PROT P");
                if (this.communication.readFTPReply().isSuccessCode()) {
                    this.dataChannelEncrypted = true;
                }
            }
        }
    }

    public void logout() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand("REIN");
                FTPReply r = this.communication.readFTPReply();
                if (!r.isSuccessCode()) {
                    throw new FTPException(r);
                }
                stopAutoNoopTimer();
                this.authenticated = false;
                this.username = null;
                this.password = null;
            }
        }
    }

    public void noop() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                try {
                    this.communication.sendFTPCommand("NOOP");
                    FTPReply r = this.communication.readFTPReply();
                    if (!r.isSuccessCode()) {
                        throw new FTPException(r);
                    }
                    touchAutoNoopTimer();
                } catch (Throwable th) {
                    touchAutoNoopTimer();
                    throw th;
                }
            }
        }
    }

    public FTPReply sendCustomCommand(String command) throws IllegalStateException, IOException, FTPIllegalReplyException {
        FTPReply readFTPReply;
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            }
            this.communication.sendFTPCommand(command);
            touchAutoNoopTimer();
            readFTPReply = this.communication.readFTPReply();
        }
        return readFTPReply;
    }

    public FTPReply sendSiteCommand(String command) throws IllegalStateException, IOException, FTPIllegalReplyException {
        FTPReply readFTPReply;
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            }
            this.communication.sendFTPCommand(new StringBuffer().append("SITE ").append(command).toString());
            touchAutoNoopTimer();
            readFTPReply = this.communication.readFTPReply();
        }
        return readFTPReply;
    }

    public void changeAccount(String account) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand(new StringBuffer().append("ACCT ").append(account).toString());
                FTPReply r = this.communication.readFTPReply();
                touchAutoNoopTimer();
                if (!r.isSuccessCode()) {
                    throw new FTPException(r);
                }
            }
        }
    }

    public String currentDirectory() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        String substring;
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand(HttpProxyConstants.PWD_PROPERTY);
                FTPReply r = this.communication.readFTPReply();
                touchAutoNoopTimer();
                if (!r.isSuccessCode()) {
                    throw new FTPException(r);
                }
                String[] messages = r.getMessages();
                if (messages.length != 1) {
                    throw new FTPIllegalReplyException();
                }
                Matcher m = PWD_PATTERN.matcher(messages[0]);
                if (m.find()) {
                    substring = messages[0].substring(m.start() + 1, m.end() - 1);
                } else {
                    throw new FTPIllegalReplyException();
                }
            }
        }
        return substring;
    }

    public void changeDirectory(String path) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand(new StringBuffer().append("CWD ").append(path).toString());
                FTPReply r = this.communication.readFTPReply();
                touchAutoNoopTimer();
                if (!r.isSuccessCode()) {
                    throw new FTPException(r);
                }
            }
        }
    }

    public void changeDirectoryUp() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand("CDUP");
                FTPReply r = this.communication.readFTPReply();
                touchAutoNoopTimer();
                if (!r.isSuccessCode()) {
                    throw new FTPException(r);
                }
            }
        }
    }

    public Date modifiedDate(String path) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        Date parse;
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand(new StringBuffer().append("MDTM ").append(path).toString());
                FTPReply r = this.communication.readFTPReply();
                touchAutoNoopTimer();
                if (!r.isSuccessCode()) {
                    throw new FTPException(r);
                }
                String[] messages = r.getMessages();
                if (messages.length != 1) {
                    throw new FTPIllegalReplyException();
                }
                try {
                    parse = MDTM_DATE_FORMAT.parse(messages[0]);
                } catch (ParseException e) {
                    throw new FTPIllegalReplyException();
                }
            }
        }
        return parse;
    }

    public long fileSize(String path) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        long parseLong;
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand("TYPE I");
                FTPReply r = this.communication.readFTPReply();
                touchAutoNoopTimer();
                if (!r.isSuccessCode()) {
                    throw new FTPException(r);
                }
                this.communication.sendFTPCommand(new StringBuffer().append("SIZE ").append(path).toString());
                FTPReply r2 = this.communication.readFTPReply();
                touchAutoNoopTimer();
                if (!r2.isSuccessCode()) {
                    throw new FTPException(r2);
                }
                String[] messages = r2.getMessages();
                if (messages.length != 1) {
                    throw new FTPIllegalReplyException();
                }
                try {
                    parseLong = Long.parseLong(messages[0]);
                } catch (Throwable th) {
                    throw new FTPIllegalReplyException();
                }
            }
        }
        return parseLong;
    }

    public void rename(String oldPath, String newPath) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand(new StringBuffer().append("RNFR ").append(oldPath).toString());
                FTPReply r = this.communication.readFTPReply();
                touchAutoNoopTimer();
                if (r.getCode() != 350) {
                    throw new FTPException(r);
                }
                this.communication.sendFTPCommand(new StringBuffer().append("RNTO ").append(newPath).toString());
                FTPReply r2 = this.communication.readFTPReply();
                touchAutoNoopTimer();
                if (!r2.isSuccessCode()) {
                    throw new FTPException(r2);
                }
            }
        }
    }

    public void deleteFile(String path) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand(new StringBuffer().append("DELE ").append(path).toString());
                FTPReply r = this.communication.readFTPReply();
                touchAutoNoopTimer();
                if (!r.isSuccessCode()) {
                    throw new FTPException(r);
                }
            }
        }
    }

    public void deleteDirectory(String path) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand(new StringBuffer().append("RMD ").append(path).toString());
                FTPReply r = this.communication.readFTPReply();
                touchAutoNoopTimer();
                if (!r.isSuccessCode()) {
                    throw new FTPException(r);
                }
            }
        }
    }

    public void createDirectory(String directoryName) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand(new StringBuffer().append("MKD ").append(directoryName).toString());
                FTPReply r = this.communication.readFTPReply();
                touchAutoNoopTimer();
                if (!r.isSuccessCode()) {
                    throw new FTPException(r);
                }
            }
        }
    }

    public String[] help() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        String[] messages;
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand("HELP");
                FTPReply r = this.communication.readFTPReply();
                touchAutoNoopTimer();
                if (!r.isSuccessCode()) {
                    throw new FTPException(r);
                }
                messages = r.getMessages();
            }
        }
        return messages;
    }

    public String[] serverStatus() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        String[] messages;
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand("STAT");
                FTPReply r = this.communication.readFTPReply();
                touchAutoNoopTimer();
                if (!r.isSuccessCode()) {
                    throw new FTPException(r);
                }
                messages = r.getMessages();
            }
        }
        return messages;
    }

    public FTPFile[] list(String fileSpec) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException, FTPListParseException {
        boolean mlsdCommand;
        boolean wasAborted;
        FTPFile[] ret;
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                this.communication.sendFTPCommand("TYPE A");
                FTPReply r = this.communication.readFTPReply();
                touchAutoNoopTimer();
                if (!r.isSuccessCode()) {
                    throw new FTPException(r);
                }
                FTPDataTransferConnectionProvider provider = openDataTransferChannel();
                if (this.mlsdPolicy == 0) {
                    mlsdCommand = this.mlsdSupported;
                } else if (this.mlsdPolicy == 1) {
                    mlsdCommand = true;
                } else {
                    mlsdCommand = false;
                }
                String command = mlsdCommand ? "MLSD" : "LIST";
                if (fileSpec != null && fileSpec.length() > 0) {
                    command = new StringBuffer().append(command).append(StringUtils.SPACE).append(fileSpec).toString();
                }
                ArrayList lines = new ArrayList();
                boolean wasAborted2 = false;
                this.communication.sendFTPCommand(command);
                try {
                    Socket dtConnection = provider.openDataTransferConnection();
                    provider.dispose();
                    synchronized (this.abortLock) {
                        this.ongoingDataTransfer = true;
                        this.aborted = false;
                        this.consumeAborCommandReply = false;
                    }
                    NVTASCIIReader dataReader = null;
                    try {
                        this.dataTransferInputStream = dtConnection.getInputStream();
                        if (this.modezEnabled) {
                            this.dataTransferInputStream = new InflaterInputStream(this.dataTransferInputStream);
                        }
                        NVTASCIIReader dataReader2 = new NVTASCIIReader(this.dataTransferInputStream, mlsdCommand ? CharEncoding.UTF_8 : pickCharset());
                        while (true) {
                            try {
                                String line = dataReader2.readLine();
                                if (line == null) {
                                    break;
                                } else if (line.length() > 0) {
                                    lines.add(line);
                                }
                            } catch (IOException e) {
                                e = e;
                                dataReader = dataReader2;
                                try {
                                    synchronized (this.abortLock) {
                                        if (this.aborted) {
                                            throw new FTPAbortedException();
                                        }
                                        throw new FTPDataTransferException("I/O error in data transfer", e);
                                    }
                                } catch (Throwable th) {
                                    th = th;
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                dataReader = dataReader2;
                                if (dataReader != null) {
                                    try {
                                        dataReader.close();
                                    } catch (Throwable th3) {
                                    }
                                }
                                try {
                                    dtConnection.close();
                                } catch (Throwable th4) {
                                }
                                this.dataTransferInputStream = null;
                                synchronized (this.abortLock) {
                                    wasAborted2 = this.aborted;
                                    this.ongoingDataTransfer = false;
                                    this.aborted = false;
                                }
                                throw th;
                            }
                        }
                        if (dataReader2 != null) {
                            try {
                                dataReader2.close();
                            } catch (Throwable th5) {
                            }
                        }
                        try {
                            dtConnection.close();
                        } catch (Throwable th6) {
                        }
                        this.dataTransferInputStream = null;
                        synchronized (this.abortLock) {
                            wasAborted = this.aborted;
                            this.ongoingDataTransfer = false;
                            this.aborted = false;
                        }
                        FTPReply r2 = this.communication.readFTPReply();
                        touchAutoNoopTimer();
                        if (r2.getCode() == 150 || r2.getCode() == 125) {
                            FTPReply r3 = this.communication.readFTPReply();
                            if (wasAborted || r3.getCode() == 226) {
                                if (this.consumeAborCommandReply) {
                                    this.communication.readFTPReply();
                                    this.consumeAborCommandReply = false;
                                }
                                int size = lines.size();
                                String[] list = new String[size];
                                for (int i = 0; i < size; i++) {
                                    list[i] = (String) lines.get(i);
                                }
                                ret = null;
                                if (mlsdCommand) {
                                    ret = new MLSDListParser().parse(list);
                                } else {
                                    if (this.parser != null) {
                                        try {
                                            ret = this.parser.parse(list);
                                        } catch (FTPListParseException e2) {
                                            this.parser = null;
                                        }
                                    }
                                    if (ret == null) {
                                        Iterator i2 = this.listParsers.iterator();
                                        while (i2.hasNext()) {
                                            FTPListParser aux = (FTPListParser) i2.next();
                                            try {
                                                ret = aux.parse(list);
                                                this.parser = aux;
                                                break;
                                            } catch (FTPListParseException e3) {
                                            }
                                        }
                                    }
                                }
                                if (ret == null) {
                                    throw new FTPListParseException();
                                }
                            } else {
                                throw new FTPException(r3);
                            }
                        } else {
                            throw new FTPException(r2);
                        }
                    } catch (IOException e4) {
                        e = e4;
                    }
                } catch (Throwable th7) {
                    FTPReply r4 = this.communication.readFTPReply();
                    touchAutoNoopTimer();
                    if (r4.getCode() == 150 || r4.getCode() == 125) {
                        FTPReply r5 = this.communication.readFTPReply();
                        if (wasAborted2 || r5.getCode() == 226) {
                            if (this.consumeAborCommandReply) {
                                this.communication.readFTPReply();
                                this.consumeAborCommandReply = false;
                            }
                            throw th7;
                        }
                        throw new FTPException(r5);
                    }
                    throw new FTPException(r4);
                }
            }
        }
        return ret;
    }

    public FTPFile[] list() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException, FTPListParseException {
        return list((String) null);
    }

    /*  JADX ERROR: IndexOutOfBoundsException in pass: RegionMakerVisitor
        java.lang.IndexOutOfBoundsException: Index 0 out of bounds for length 0
        	at java.base/jdk.internal.util.Preconditions.outOfBounds(Preconditions.java:64)
        	at java.base/jdk.internal.util.Preconditions.outOfBoundsCheckIndex(Preconditions.java:70)
        	at java.base/jdk.internal.util.Preconditions.checkIndex(Preconditions.java:248)
        	at java.base/java.util.Objects.checkIndex(Objects.java:372)
        	at java.base/java.util.ArrayList.get(ArrayList.java:458)
        	at jadx.core.dex.nodes.InsnNode.getArg(InsnNode.java:101)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:611)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:561)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processExcHandler(RegionMaker.java:1043)
        	at jadx.core.dex.visitors.regions.RegionMaker.processTryCatchBlocks(RegionMaker.java:975)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:52)
        */
    public java.lang.String[] listNames() throws java.lang.IllegalStateException, java.io.IOException, p010it.sauronsoftware.ftp4j.FTPIllegalReplyException, p010it.sauronsoftware.ftp4j.FTPException, p010it.sauronsoftware.ftp4j.FTPDataTransferException, p010it.sauronsoftware.ftp4j.FTPAbortedException, p010it.sauronsoftware.ftp4j.FTPListParseException {
        /*
            r18 = this;
            r0 = r18
            java.lang.Object r15 = r0.lock
            monitor-enter(r15)
            r0 = r18
            boolean r14 = r0.connected     // Catch:{ all -> 0x0015 }
            if (r14 != 0) goto L_0x0018
            java.lang.IllegalStateException r14 = new java.lang.IllegalStateException     // Catch:{ all -> 0x0015 }
            java.lang.String r16 = "Client not connected"
            r0 = r16
            r14.<init>(r0)     // Catch:{ all -> 0x0015 }
            throw r14     // Catch:{ all -> 0x0015 }
        L_0x0015:
            r14 = move-exception
            monitor-exit(r15)     // Catch:{ all -> 0x0015 }
            throw r14
        L_0x0018:
            r0 = r18
            boolean r14 = r0.authenticated     // Catch:{ all -> 0x0015 }
            if (r14 != 0) goto L_0x0028
            java.lang.IllegalStateException r14 = new java.lang.IllegalStateException     // Catch:{ all -> 0x0015 }
            java.lang.String r16 = "Client not authenticated"
            r0 = r16
            r14.<init>(r0)     // Catch:{ all -> 0x0015 }
            throw r14     // Catch:{ all -> 0x0015 }
        L_0x0028:
            r0 = r18
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r14 = r0.communication     // Catch:{ all -> 0x0015 }
            java.lang.String r16 = "TYPE A"
            r0 = r16
            r14.sendFTPCommand(r0)     // Catch:{ all -> 0x0015 }
            r0 = r18
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r14 = r0.communication     // Catch:{ all -> 0x0015 }
            it.sauronsoftware.ftp4j.FTPReply r11 = r14.readFTPReply()     // Catch:{ all -> 0x0015 }
            r18.touchAutoNoopTimer()     // Catch:{ all -> 0x0015 }
            boolean r14 = r11.isSuccessCode()     // Catch:{ all -> 0x0015 }
            if (r14 != 0) goto L_0x004a
            it.sauronsoftware.ftp4j.FTPException r14 = new it.sauronsoftware.ftp4j.FTPException     // Catch:{ all -> 0x0015 }
            r14.<init>((p010it.sauronsoftware.ftp4j.FTPReply) r11)     // Catch:{ all -> 0x0015 }
            throw r14     // Catch:{ all -> 0x0015 }
        L_0x004a:
            java.util.ArrayList r8 = new java.util.ArrayList     // Catch:{ all -> 0x0015 }
            r8.<init>()     // Catch:{ all -> 0x0015 }
            r13 = 0
            it.sauronsoftware.ftp4j.FTPDataTransferConnectionProvider r10 = r18.openDataTransferChannel()     // Catch:{ all -> 0x0015 }
            r0 = r18
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r14 = r0.communication     // Catch:{ all -> 0x0015 }
            java.lang.String r16 = "NLST"
            r0 = r16
            r14.sendFTPCommand(r0)     // Catch:{ all -> 0x0015 }
            java.net.Socket r4 = r10.openDataTransferConnection()     // Catch:{ all -> 0x012b }
            r10.dispose()     // Catch:{ all -> 0x0102 }
            r0 = r18
            java.lang.Object r0 = r0.abortLock     // Catch:{ all -> 0x0102 }
            r16 = r0
            monitor-enter(r16)     // Catch:{ all -> 0x0102 }
            r14 = 1
            r0 = r18
            r0.ongoingDataTransfer = r14     // Catch:{ all -> 0x0130 }
            r14 = 0
            r0 = r18
            r0.aborted = r14     // Catch:{ all -> 0x0130 }
            r14 = 0
            r0 = r18
            r0.consumeAborCommandReply = r14     // Catch:{ all -> 0x0130 }
            monitor-exit(r16)     // Catch:{ all -> 0x0130 }
            r2 = 0
            java.io.InputStream r14 = r4.getInputStream()     // Catch:{ IOException -> 0x0210 }
            r0 = r18
            r0.dataTransferInputStream = r14     // Catch:{ IOException -> 0x0210 }
            r0 = r18
            boolean r14 = r0.modezEnabled     // Catch:{ IOException -> 0x0210 }
            if (r14 == 0) goto L_0x009d
            java.util.zip.InflaterInputStream r14 = new java.util.zip.InflaterInputStream     // Catch:{ IOException -> 0x0210 }
            r0 = r18
            java.io.InputStream r0 = r0.dataTransferInputStream     // Catch:{ IOException -> 0x0210 }
            r16 = r0
            r0 = r16
            r14.<init>(r0)     // Catch:{ IOException -> 0x0210 }
            r0 = r18
            r0.dataTransferInputStream = r14     // Catch:{ IOException -> 0x0210 }
        L_0x009d:
            it.sauronsoftware.ftp4j.NVTASCIIReader r3 = new it.sauronsoftware.ftp4j.NVTASCIIReader     // Catch:{ IOException -> 0x0210 }
            r0 = r18
            java.io.InputStream r14 = r0.dataTransferInputStream     // Catch:{ IOException -> 0x0210 }
            java.lang.String r16 = r18.pickCharset()     // Catch:{ IOException -> 0x0210 }
            r0 = r16
            r3.<init>(r14, r0)     // Catch:{ IOException -> 0x0210 }
        L_0x00ac:
            java.lang.String r7 = r3.readLine()     // Catch:{ IOException -> 0x00bc, all -> 0x020c }
            if (r7 == 0) goto L_0x01a6
            int r14 = r7.length()     // Catch:{ IOException -> 0x00bc, all -> 0x020c }
            if (r14 <= 0) goto L_0x00ac
            r8.add(r7)     // Catch:{ IOException -> 0x00bc, all -> 0x020c }
            goto L_0x00ac
        L_0x00bc:
            r5 = move-exception
            r2 = r3
        L_0x00be:
            r0 = r18
            java.lang.Object r0 = r0.abortLock     // Catch:{ all -> 0x00d4 }
            r16 = r0
            monitor-enter(r16)     // Catch:{ all -> 0x00d4 }
            r0 = r18
            boolean r14 = r0.aborted     // Catch:{ all -> 0x00d1 }
            if (r14 == 0) goto L_0x0133
            it.sauronsoftware.ftp4j.FTPAbortedException r14 = new it.sauronsoftware.ftp4j.FTPAbortedException     // Catch:{ all -> 0x00d1 }
            r14.<init>()     // Catch:{ all -> 0x00d1 }
            throw r14     // Catch:{ all -> 0x00d1 }
        L_0x00d1:
            r14 = move-exception
            monitor-exit(r16)     // Catch:{ all -> 0x00d1 }
            throw r14     // Catch:{ all -> 0x00d4 }
        L_0x00d4:
            r14 = move-exception
        L_0x00d5:
            if (r2 == 0) goto L_0x00da
            r2.close()     // Catch:{ Throwable -> 0x019d }
        L_0x00da:
            r4.close()     // Catch:{ Throwable -> 0x01a0 }
        L_0x00dd:
            r16 = 0
            r0 = r16
            r1 = r18
            r1.dataTransferInputStream = r0     // Catch:{ all -> 0x0102 }
            r0 = r18
            java.lang.Object r0 = r0.abortLock     // Catch:{ all -> 0x0102 }
            r16 = r0
            monitor-enter(r16)     // Catch:{ all -> 0x0102 }
            r0 = r18
            boolean r13 = r0.aborted     // Catch:{ all -> 0x01a3 }
            r17 = 0
            r0 = r17
            r1 = r18
            r1.ongoingDataTransfer = r0     // Catch:{ all -> 0x01a3 }
            r17 = 0
            r0 = r17
            r1 = r18
            r1.aborted = r0     // Catch:{ all -> 0x01a3 }
            monitor-exit(r16)     // Catch:{ all -> 0x01a3 }
            throw r14     // Catch:{ all -> 0x0102 }
        L_0x0102:
            r14 = move-exception
            r0 = r18
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r0 = r0.communication     // Catch:{ all -> 0x0015 }
            r16 = r0
            it.sauronsoftware.ftp4j.FTPReply r11 = r16.readFTPReply()     // Catch:{ all -> 0x0015 }
            int r16 = r11.getCode()     // Catch:{ all -> 0x0015 }
            r17 = 150(0x96, float:2.1E-43)
            r0 = r16
            r1 = r17
            if (r0 == r1) goto L_0x017f
            int r16 = r11.getCode()     // Catch:{ all -> 0x0015 }
            r17 = 125(0x7d, float:1.75E-43)
            r0 = r16
            r1 = r17
            if (r0 == r1) goto L_0x017f
            it.sauronsoftware.ftp4j.FTPException r14 = new it.sauronsoftware.ftp4j.FTPException     // Catch:{ all -> 0x0015 }
            r14.<init>((p010it.sauronsoftware.ftp4j.FTPReply) r11)     // Catch:{ all -> 0x0015 }
            throw r14     // Catch:{ all -> 0x0015 }
        L_0x012b:
            r14 = move-exception
            r10.dispose()     // Catch:{ all -> 0x0102 }
            throw r14     // Catch:{ all -> 0x0102 }
        L_0x0130:
            r14 = move-exception
            monitor-exit(r16)     // Catch:{ all -> 0x0130 }
            throw r14     // Catch:{ all -> 0x0102 }
        L_0x0133:
            it.sauronsoftware.ftp4j.FTPDataTransferException r14 = new it.sauronsoftware.ftp4j.FTPDataTransferException     // Catch:{ all -> 0x00d1 }
            java.lang.String r17 = "I/O error in data transfer"
            r0 = r17
            r14.<init>(r0, r5)     // Catch:{ all -> 0x00d1 }
            throw r14     // Catch:{ all -> 0x00d1 }
        L_0x013d:
            r0 = r18
            boolean r0 = r0.consumeAborCommandReply     // Catch:{ all -> 0x0015 }
            r16 = r0
            if (r16 == 0) goto L_0x0156
            r0 = r18
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r0 = r0.communication     // Catch:{ all -> 0x0015 }
            r16 = r0
            r16.readFTPReply()     // Catch:{ all -> 0x0015 }
            r16 = 0
            r0 = r16
            r1 = r18
            r1.consumeAborCommandReply = r0     // Catch:{ all -> 0x0015 }
        L_0x0156:
            throw r14     // Catch:{ all -> 0x0015 }
        L_0x0157:
            r0 = r18
            boolean r14 = r0.consumeAborCommandReply     // Catch:{ all -> 0x0015 }
            if (r14 == 0) goto L_0x0169
            r0 = r18
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r14 = r0.communication     // Catch:{ all -> 0x0015 }
            r14.readFTPReply()     // Catch:{ all -> 0x0015 }
            r14 = 0
            r0 = r18
            r0.consumeAborCommandReply = r14     // Catch:{ all -> 0x0015 }
        L_0x0169:
            int r12 = r8.size()     // Catch:{ all -> 0x0015 }
            java.lang.String[] r9 = new java.lang.String[r12]     // Catch:{ all -> 0x0015 }
            r6 = 0
        L_0x0170:
            if (r6 >= r12) goto L_0x017d
            java.lang.Object r14 = r8.get(r6)     // Catch:{ all -> 0x0015 }
            java.lang.String r14 = (java.lang.String) r14     // Catch:{ all -> 0x0015 }
            r9[r6] = r14     // Catch:{ all -> 0x0015 }
            int r6 = r6 + 1
            goto L_0x0170
        L_0x017d:
            monitor-exit(r15)     // Catch:{ all -> 0x0015 }
            return r9
        L_0x017f:
            r0 = r18
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r0 = r0.communication     // Catch:{ all -> 0x0015 }
            r16 = r0
            it.sauronsoftware.ftp4j.FTPReply r11 = r16.readFTPReply()     // Catch:{ all -> 0x0015 }
            if (r13 != 0) goto L_0x013d
            int r16 = r11.getCode()     // Catch:{ all -> 0x0015 }
            r17 = 226(0xe2, float:3.17E-43)
            r0 = r16
            r1 = r17
            if (r0 == r1) goto L_0x013d
            it.sauronsoftware.ftp4j.FTPException r14 = new it.sauronsoftware.ftp4j.FTPException     // Catch:{ all -> 0x0015 }
            r14.<init>((p010it.sauronsoftware.ftp4j.FTPReply) r11)     // Catch:{ all -> 0x0015 }
            throw r14     // Catch:{ all -> 0x0015 }
        L_0x019d:
            r16 = move-exception
            goto L_0x00da
        L_0x01a0:
            r16 = move-exception
            goto L_0x00dd
        L_0x01a3:
            r14 = move-exception
            monitor-exit(r16)     // Catch:{ all -> 0x01a3 }
            throw r14     // Catch:{ all -> 0x0102 }
        L_0x01a6:
            if (r3 == 0) goto L_0x01ab
            r3.close()     // Catch:{ Throwable -> 0x01eb }
        L_0x01ab:
            r4.close()     // Catch:{ Throwable -> 0x01ed }
        L_0x01ae:
            r14 = 0
            r0 = r18
            r0.dataTransferInputStream = r14     // Catch:{ all -> 0x0102 }
            r0 = r18
            java.lang.Object r0 = r0.abortLock     // Catch:{ all -> 0x0102 }
            r16 = r0
            monitor-enter(r16)     // Catch:{ all -> 0x0102 }
            r0 = r18
            boolean r13 = r0.aborted     // Catch:{ all -> 0x01ef }
            r14 = 0
            r0 = r18
            r0.ongoingDataTransfer = r14     // Catch:{ all -> 0x01ef }
            r14 = 0
            r0 = r18
            r0.aborted = r14     // Catch:{ all -> 0x01ef }
            monitor-exit(r16)     // Catch:{ all -> 0x01ef }
            r0 = r18
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r14 = r0.communication     // Catch:{ all -> 0x0015 }
            it.sauronsoftware.ftp4j.FTPReply r11 = r14.readFTPReply()     // Catch:{ all -> 0x0015 }
            int r14 = r11.getCode()     // Catch:{ all -> 0x0015 }
            r16 = 150(0x96, float:2.1E-43)
            r0 = r16
            if (r14 == r0) goto L_0x01f2
            int r14 = r11.getCode()     // Catch:{ all -> 0x0015 }
            r16 = 125(0x7d, float:1.75E-43)
            r0 = r16
            if (r14 == r0) goto L_0x01f2
            it.sauronsoftware.ftp4j.FTPException r14 = new it.sauronsoftware.ftp4j.FTPException     // Catch:{ all -> 0x0015 }
            r14.<init>((p010it.sauronsoftware.ftp4j.FTPReply) r11)     // Catch:{ all -> 0x0015 }
            throw r14     // Catch:{ all -> 0x0015 }
        L_0x01eb:
            r14 = move-exception
            goto L_0x01ab
        L_0x01ed:
            r14 = move-exception
            goto L_0x01ae
        L_0x01ef:
            r14 = move-exception
            monitor-exit(r16)     // Catch:{ all -> 0x01ef }
            throw r14     // Catch:{ all -> 0x0102 }
        L_0x01f2:
            r0 = r18
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r14 = r0.communication     // Catch:{ all -> 0x0015 }
            it.sauronsoftware.ftp4j.FTPReply r11 = r14.readFTPReply()     // Catch:{ all -> 0x0015 }
            if (r13 != 0) goto L_0x0157
            int r14 = r11.getCode()     // Catch:{ all -> 0x0015 }
            r16 = 226(0xe2, float:3.17E-43)
            r0 = r16
            if (r14 == r0) goto L_0x0157
            it.sauronsoftware.ftp4j.FTPException r14 = new it.sauronsoftware.ftp4j.FTPException     // Catch:{ all -> 0x0015 }
            r14.<init>((p010it.sauronsoftware.ftp4j.FTPReply) r11)     // Catch:{ all -> 0x0015 }
            throw r14     // Catch:{ all -> 0x0015 }
        L_0x020c:
            r14 = move-exception
            r2 = r3
            goto L_0x00d5
        L_0x0210:
            r5 = move-exception
            goto L_0x00be
        */
        throw new UnsupportedOperationException("Method not decompiled: p010it.sauronsoftware.ftp4j.FTPClient.listNames():java.lang.String[]");
    }

    public void upload(File file) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        upload(file, 0, (FTPDataTransferListener) null);
    }

    public void upload(File file, FTPDataTransferListener listener) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        upload(file, 0, listener);
    }

    public void upload(File file, long restartAt) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        upload(file, restartAt, (FTPDataTransferListener) null);
    }

    public void upload(File file, long restartAt, FTPDataTransferListener listener) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        try {
            InputStream inputStream = new FileInputStream(file);
            try {
                upload(file.getName(), inputStream, restartAt, restartAt, listener);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable th) {
                    }
                }
            } catch (IllegalStateException e) {
                throw e;
            } catch (IOException e2) {
                throw e2;
            } catch (FTPIllegalReplyException e3) {
                throw e3;
            } catch (FTPException e4) {
                throw e4;
            } catch (FTPDataTransferException e5) {
                throw e5;
            } catch (FTPAbortedException e6) {
                throw e6;
            } catch (Throwable th2) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable th3) {
                    }
                }
                throw th2;
            }
        } catch (IOException e7) {
            throw new FTPDataTransferException((Throwable) e7);
        }
    }

    public void upload(String fileName, InputStream inputStream, long restartAt, long streamOffset, FTPDataTransferListener listener) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        boolean wasAborted;
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                int tp = this.type;
                if (tp == 0) {
                    tp = detectType(fileName);
                }
                if (tp == 1) {
                    this.communication.sendFTPCommand("TYPE A");
                } else if (tp == 2) {
                    this.communication.sendFTPCommand("TYPE I");
                }
                FTPReply r = this.communication.readFTPReply();
                touchAutoNoopTimer();
                if (!r.isSuccessCode()) {
                    throw new FTPException(r);
                }
                FTPDataTransferConnectionProvider provider = openDataTransferChannel();
                if (this.restSupported || restartAt > 0) {
                    try {
                        this.communication.sendFTPCommand(new StringBuffer().append("REST ").append(restartAt).toString());
                        FTPReply r2 = this.communication.readFTPReply();
                        touchAutoNoopTimer();
                        if (r2.getCode() != 350 && ((r2.getCode() != 501 && r2.getCode() != 502) || restartAt > 0)) {
                            throw new FTPException(r2);
                        } else if (1 == 0) {
                            provider.dispose();
                        }
                    } catch (Throwable th) {
                        if (0 == 0) {
                            provider.dispose();
                        }
                        throw th;
                    }
                }
                boolean wasAborted2 = false;
                this.communication.sendFTPCommand(new StringBuffer().append("STOR ").append(fileName).toString());
                try {
                    Socket dtConnection = provider.openDataTransferConnection();
                    provider.dispose();
                    synchronized (this.abortLock) {
                        this.ongoingDataTransfer = true;
                        this.aborted = false;
                        this.consumeAborCommandReply = false;
                    }
                    try {
                        inputStream.skip(streamOffset);
                        this.dataTransferOutputStream = dtConnection.getOutputStream();
                        if (this.modezEnabled) {
                            this.dataTransferOutputStream = new DeflaterOutputStream(this.dataTransferOutputStream);
                        }
                        if (listener != null) {
                            listener.started();
                        }
                        if (tp == 1) {
                            Reader reader = new InputStreamReader(inputStream);
                            Writer writer = new OutputStreamWriter(this.dataTransferOutputStream, pickCharset());
                            char[] buffer = new char[65536];
                            while (true) {
                                int l = reader.read(buffer);
                                if (l == -1) {
                                    break;
                                }
                                writer.write(buffer, 0, l);
                                writer.flush();
                                if (listener != null) {
                                    listener.transferred(l);
                                }
                            }
                        } else if (tp == 2) {
                            byte[] buffer2 = new byte[65536];
                            while (true) {
                                int l2 = inputStream.read(buffer2);
                                if (l2 == -1) {
                                    break;
                                }
                                this.dataTransferOutputStream.write(buffer2, 0, l2);
                                this.dataTransferOutputStream.flush();
                                if (listener != null) {
                                    listener.transferred(l2);
                                }
                            }
                        }
                        if (this.dataTransferOutputStream != null) {
                            try {
                                this.dataTransferOutputStream.close();
                            } catch (Throwable th2) {
                            }
                        }
                        try {
                            dtConnection.close();
                        } catch (Throwable th3) {
                        }
                        this.dataTransferOutputStream = null;
                        synchronized (this.abortLock) {
                            wasAborted = this.aborted;
                            this.ongoingDataTransfer = false;
                            this.aborted = false;
                        }
                        FTPReply r3 = this.communication.readFTPReply();
                        touchAutoNoopTimer();
                        if (r3.getCode() == 150 || r3.getCode() == 125) {
                            FTPReply r4 = this.communication.readFTPReply();
                            if (wasAborted || r4.getCode() == 226) {
                                if (this.consumeAborCommandReply) {
                                    this.communication.readFTPReply();
                                    this.consumeAborCommandReply = false;
                                }
                                if (listener != null) {
                                    listener.completed();
                                }
                            } else {
                                throw new FTPException(r4);
                            }
                        } else {
                            throw new FTPException(r3);
                        }
                    } catch (IOException e) {
                        synchronized (this.abortLock) {
                            if (this.aborted) {
                                if (listener != null) {
                                    listener.aborted();
                                }
                                throw new FTPAbortedException();
                            }
                            if (listener != null) {
                                listener.failed();
                            }
                            throw new FTPDataTransferException("I/O error in data transfer", e);
                        }
                    } catch (Throwable th4) {
                        if (this.dataTransferOutputStream != null) {
                            this.dataTransferOutputStream.close();
                        }
                        dtConnection.close();
                        this.dataTransferOutputStream = null;
                        synchronized (this.abortLock) {
                            wasAborted2 = this.aborted;
                            this.ongoingDataTransfer = false;
                            this.aborted = false;
                            throw th4;
                        }
                    }
                } catch (Throwable th5) {
                }
            }
        }
    }

    public void append(File file) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        append(file, (FTPDataTransferListener) null);
    }

    public void append(File file, FTPDataTransferListener listener) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        try {
            InputStream inputStream = new FileInputStream(file);
            try {
                append(file.getName(), inputStream, 0, listener);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable th) {
                    }
                }
            } catch (IllegalStateException e) {
                throw e;
            } catch (IOException e2) {
                throw e2;
            } catch (FTPIllegalReplyException e3) {
                throw e3;
            } catch (FTPException e4) {
                throw e4;
            } catch (FTPDataTransferException e5) {
                throw e5;
            } catch (FTPAbortedException e6) {
                throw e6;
            } catch (Throwable th2) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable th3) {
                    }
                }
                throw th2;
            }
        } catch (IOException e7) {
            throw new FTPDataTransferException((Throwable) e7);
        }
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:166:0x01d3=Splitter:B:166:0x01d3, B:116:0x0158=Splitter:B:116:0x0158} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void append(java.lang.String r16, java.io.InputStream r17, long r18, p010it.sauronsoftware.ftp4j.FTPDataTransferListener r20) throws java.lang.IllegalStateException, java.io.IOException, p010it.sauronsoftware.ftp4j.FTPIllegalReplyException, p010it.sauronsoftware.ftp4j.FTPException, p010it.sauronsoftware.ftp4j.FTPDataTransferException, p010it.sauronsoftware.ftp4j.FTPAbortedException {
        /*
            r15 = this;
            java.lang.Object r12 = r15.lock
            monitor-enter(r12)
            boolean r11 = r15.connected     // Catch:{ all -> 0x000f }
            if (r11 != 0) goto L_0x0012
            java.lang.IllegalStateException r11 = new java.lang.IllegalStateException     // Catch:{ all -> 0x000f }
            java.lang.String r13 = "Client not connected"
            r11.<init>(r13)     // Catch:{ all -> 0x000f }
            throw r11     // Catch:{ all -> 0x000f }
        L_0x000f:
            r11 = move-exception
            monitor-exit(r12)     // Catch:{ all -> 0x000f }
            throw r11
        L_0x0012:
            boolean r11 = r15.authenticated     // Catch:{ all -> 0x000f }
            if (r11 != 0) goto L_0x001e
            java.lang.IllegalStateException r11 = new java.lang.IllegalStateException     // Catch:{ all -> 0x000f }
            java.lang.String r13 = "Client not authenticated"
            r11.<init>(r13)     // Catch:{ all -> 0x000f }
            throw r11     // Catch:{ all -> 0x000f }
        L_0x001e:
            int r8 = r15.type     // Catch:{ all -> 0x000f }
            if (r8 != 0) goto L_0x0026
            int r8 = r15.detectType(r16)     // Catch:{ all -> 0x000f }
        L_0x0026:
            r11 = 1
            if (r8 != r11) goto L_0x0045
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r11 = r15.communication     // Catch:{ all -> 0x000f }
            java.lang.String r13 = "TYPE A"
            r11.sendFTPCommand(r13)     // Catch:{ all -> 0x000f }
        L_0x0030:
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r11 = r15.communication     // Catch:{ all -> 0x000f }
            it.sauronsoftware.ftp4j.FTPReply r6 = r11.readFTPReply()     // Catch:{ all -> 0x000f }
            r15.touchAutoNoopTimer()     // Catch:{ all -> 0x000f }
            boolean r11 = r6.isSuccessCode()     // Catch:{ all -> 0x000f }
            if (r11 != 0) goto L_0x0050
            it.sauronsoftware.ftp4j.FTPException r11 = new it.sauronsoftware.ftp4j.FTPException     // Catch:{ all -> 0x000f }
            r11.<init>((p010it.sauronsoftware.ftp4j.FTPReply) r6)     // Catch:{ all -> 0x000f }
            throw r11     // Catch:{ all -> 0x000f }
        L_0x0045:
            r11 = 2
            if (r8 != r11) goto L_0x0030
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r11 = r15.communication     // Catch:{ all -> 0x000f }
            java.lang.String r13 = "TYPE I"
            r11.sendFTPCommand(r13)     // Catch:{ all -> 0x000f }
            goto L_0x0030
        L_0x0050:
            r9 = 0
            it.sauronsoftware.ftp4j.FTPDataTransferConnectionProvider r5 = r15.openDataTransferChannel()     // Catch:{ all -> 0x000f }
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r11 = r15.communication     // Catch:{ all -> 0x000f }
            java.lang.StringBuffer r13 = new java.lang.StringBuffer     // Catch:{ all -> 0x000f }
            r13.<init>()     // Catch:{ all -> 0x000f }
            java.lang.String r14 = "APPE "
            java.lang.StringBuffer r13 = r13.append(r14)     // Catch:{ all -> 0x000f }
            r0 = r16
            java.lang.StringBuffer r13 = r13.append(r0)     // Catch:{ all -> 0x000f }
            java.lang.String r13 = r13.toString()     // Catch:{ all -> 0x000f }
            r11.sendFTPCommand(r13)     // Catch:{ all -> 0x000f }
            java.net.Socket r2 = r5.openDataTransferConnection()     // Catch:{ all -> 0x0120 }
            r5.dispose()     // Catch:{ all -> 0x0100 }
            java.lang.Object r13 = r15.abortLock     // Catch:{ all -> 0x0100 }
            monitor-enter(r13)     // Catch:{ all -> 0x0100 }
            r11 = 1
            r15.ongoingDataTransfer = r11     // Catch:{ all -> 0x0125 }
            r11 = 0
            r15.aborted = r11     // Catch:{ all -> 0x0125 }
            r11 = 0
            r15.consumeAborCommandReply = r11     // Catch:{ all -> 0x0125 }
            monitor-exit(r13)     // Catch:{ all -> 0x0125 }
            r17.skip(r18)     // Catch:{ IOException -> 0x00cd }
            java.io.OutputStream r11 = r2.getOutputStream()     // Catch:{ IOException -> 0x00cd }
            r15.dataTransferOutputStream = r11     // Catch:{ IOException -> 0x00cd }
            boolean r11 = r15.modezEnabled     // Catch:{ IOException -> 0x00cd }
            if (r11 == 0) goto L_0x0099
            java.util.zip.DeflaterOutputStream r11 = new java.util.zip.DeflaterOutputStream     // Catch:{ IOException -> 0x00cd }
            java.io.OutputStream r13 = r15.dataTransferOutputStream     // Catch:{ IOException -> 0x00cd }
            r11.<init>(r13)     // Catch:{ IOException -> 0x00cd }
            r15.dataTransferOutputStream = r11     // Catch:{ IOException -> 0x00cd }
        L_0x0099:
            if (r20 == 0) goto L_0x009e
            r20.started()     // Catch:{ IOException -> 0x00cd }
        L_0x009e:
            r11 = 1
            if (r8 != r11) goto L_0x0128
            java.io.InputStreamReader r7 = new java.io.InputStreamReader     // Catch:{ IOException -> 0x00cd }
            r0 = r17
            r7.<init>(r0)     // Catch:{ IOException -> 0x00cd }
            java.io.OutputStreamWriter r10 = new java.io.OutputStreamWriter     // Catch:{ IOException -> 0x00cd }
            java.io.OutputStream r11 = r15.dataTransferOutputStream     // Catch:{ IOException -> 0x00cd }
            java.lang.String r13 = r15.pickCharset()     // Catch:{ IOException -> 0x00cd }
            r10.<init>(r11, r13)     // Catch:{ IOException -> 0x00cd }
            r11 = 65536(0x10000, float:9.18355E-41)
            char[] r1 = new char[r11]     // Catch:{ IOException -> 0x00cd }
        L_0x00b7:
            int r4 = r7.read(r1)     // Catch:{ IOException -> 0x00cd }
            r11 = -1
            if (r4 == r11) goto L_0x0194
            r11 = 0
            r10.write(r1, r11, r4)     // Catch:{ IOException -> 0x00cd }
            r10.flush()     // Catch:{ IOException -> 0x00cd }
            if (r20 == 0) goto L_0x00b7
            r0 = r20
            r0.transferred(r4)     // Catch:{ IOException -> 0x00cd }
            goto L_0x00b7
        L_0x00cd:
            r3 = move-exception
            java.lang.Object r13 = r15.abortLock     // Catch:{ all -> 0x00e3 }
            monitor-enter(r13)     // Catch:{ all -> 0x00e3 }
            boolean r11 = r15.aborted     // Catch:{ all -> 0x00e0 }
            if (r11 == 0) goto L_0x014b
            if (r20 == 0) goto L_0x00da
            r20.aborted()     // Catch:{ all -> 0x00e0 }
        L_0x00da:
            it.sauronsoftware.ftp4j.FTPAbortedException r11 = new it.sauronsoftware.ftp4j.FTPAbortedException     // Catch:{ all -> 0x00e0 }
            r11.<init>()     // Catch:{ all -> 0x00e0 }
            throw r11     // Catch:{ all -> 0x00e0 }
        L_0x00e0:
            r11 = move-exception
            monitor-exit(r13)     // Catch:{ all -> 0x00e0 }
            throw r11     // Catch:{ all -> 0x00e3 }
        L_0x00e3:
            r11 = move-exception
            java.io.OutputStream r13 = r15.dataTransferOutputStream     // Catch:{ all -> 0x0100 }
            if (r13 == 0) goto L_0x00ed
            java.io.OutputStream r13 = r15.dataTransferOutputStream     // Catch:{ Throwable -> 0x01eb }
            r13.close()     // Catch:{ Throwable -> 0x01eb }
        L_0x00ed:
            r2.close()     // Catch:{ Throwable -> 0x018e }
        L_0x00f0:
            r13 = 0
            r15.dataTransferOutputStream = r13     // Catch:{ all -> 0x0100 }
            java.lang.Object r13 = r15.abortLock     // Catch:{ all -> 0x0100 }
            monitor-enter(r13)     // Catch:{ all -> 0x0100 }
            boolean r9 = r15.aborted     // Catch:{ all -> 0x0191 }
            r14 = 0
            r15.ongoingDataTransfer = r14     // Catch:{ all -> 0x0191 }
            r14 = 0
            r15.aborted = r14     // Catch:{ all -> 0x0191 }
            monitor-exit(r13)     // Catch:{ all -> 0x0191 }
            throw r11     // Catch:{ all -> 0x0100 }
        L_0x0100:
            r11 = move-exception
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r13 = r15.communication     // Catch:{ all -> 0x000f }
            it.sauronsoftware.ftp4j.FTPReply r6 = r13.readFTPReply()     // Catch:{ all -> 0x000f }
            r15.touchAutoNoopTimer()     // Catch:{ all -> 0x000f }
            int r13 = r6.getCode()     // Catch:{ all -> 0x000f }
            r14 = 150(0x96, float:2.1E-43)
            if (r13 == r14) goto L_0x0178
            int r13 = r6.getCode()     // Catch:{ all -> 0x000f }
            r14 = 125(0x7d, float:1.75E-43)
            if (r13 == r14) goto L_0x0178
            it.sauronsoftware.ftp4j.FTPException r11 = new it.sauronsoftware.ftp4j.FTPException     // Catch:{ all -> 0x000f }
            r11.<init>((p010it.sauronsoftware.ftp4j.FTPReply) r6)     // Catch:{ all -> 0x000f }
            throw r11     // Catch:{ all -> 0x000f }
        L_0x0120:
            r11 = move-exception
            r5.dispose()     // Catch:{ all -> 0x0100 }
            throw r11     // Catch:{ all -> 0x0100 }
        L_0x0125:
            r11 = move-exception
            monitor-exit(r13)     // Catch:{ all -> 0x0125 }
            throw r11     // Catch:{ all -> 0x0100 }
        L_0x0128:
            r11 = 2
            if (r8 != r11) goto L_0x0194
            r11 = 65536(0x10000, float:9.18355E-41)
            byte[] r1 = new byte[r11]     // Catch:{ IOException -> 0x00cd }
        L_0x012f:
            r0 = r17
            int r4 = r0.read(r1)     // Catch:{ IOException -> 0x00cd }
            r11 = -1
            if (r4 == r11) goto L_0x0194
            java.io.OutputStream r11 = r15.dataTransferOutputStream     // Catch:{ IOException -> 0x00cd }
            r13 = 0
            r11.write(r1, r13, r4)     // Catch:{ IOException -> 0x00cd }
            java.io.OutputStream r11 = r15.dataTransferOutputStream     // Catch:{ IOException -> 0x00cd }
            r11.flush()     // Catch:{ IOException -> 0x00cd }
            if (r20 == 0) goto L_0x012f
            r0 = r20
            r0.transferred(r4)     // Catch:{ IOException -> 0x00cd }
            goto L_0x012f
        L_0x014b:
            if (r20 == 0) goto L_0x0150
            r20.failed()     // Catch:{ all -> 0x00e0 }
        L_0x0150:
            it.sauronsoftware.ftp4j.FTPDataTransferException r11 = new it.sauronsoftware.ftp4j.FTPDataTransferException     // Catch:{ all -> 0x00e0 }
            java.lang.String r14 = "I/O error in data transfer"
            r11.<init>(r14, r3)     // Catch:{ all -> 0x00e0 }
            throw r11     // Catch:{ all -> 0x00e0 }
        L_0x0158:
            boolean r13 = r15.consumeAborCommandReply     // Catch:{ all -> 0x000f }
            if (r13 == 0) goto L_0x0164
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r13 = r15.communication     // Catch:{ all -> 0x000f }
            r13.readFTPReply()     // Catch:{ all -> 0x000f }
            r13 = 0
            r15.consumeAborCommandReply = r13     // Catch:{ all -> 0x000f }
        L_0x0164:
            throw r11     // Catch:{ all -> 0x000f }
        L_0x0165:
            boolean r11 = r15.consumeAborCommandReply     // Catch:{ all -> 0x000f }
            if (r11 == 0) goto L_0x0171
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r11 = r15.communication     // Catch:{ all -> 0x000f }
            r11.readFTPReply()     // Catch:{ all -> 0x000f }
            r11 = 0
            r15.consumeAborCommandReply = r11     // Catch:{ all -> 0x000f }
        L_0x0171:
            if (r20 == 0) goto L_0x0176
            r20.completed()     // Catch:{ all -> 0x000f }
        L_0x0176:
            monitor-exit(r12)     // Catch:{ all -> 0x000f }
            return
        L_0x0178:
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r13 = r15.communication     // Catch:{ all -> 0x000f }
            it.sauronsoftware.ftp4j.FTPReply r6 = r13.readFTPReply()     // Catch:{ all -> 0x000f }
            if (r9 != 0) goto L_0x0158
            int r13 = r6.getCode()     // Catch:{ all -> 0x000f }
            r14 = 226(0xe2, float:3.17E-43)
            if (r13 == r14) goto L_0x0158
            it.sauronsoftware.ftp4j.FTPException r11 = new it.sauronsoftware.ftp4j.FTPException     // Catch:{ all -> 0x000f }
            r11.<init>((p010it.sauronsoftware.ftp4j.FTPReply) r6)     // Catch:{ all -> 0x000f }
            throw r11     // Catch:{ all -> 0x000f }
        L_0x018e:
            r13 = move-exception
            goto L_0x00f0
        L_0x0191:
            r11 = move-exception
            monitor-exit(r13)     // Catch:{ all -> 0x0191 }
            throw r11     // Catch:{ all -> 0x0100 }
        L_0x0194:
            java.io.OutputStream r11 = r15.dataTransferOutputStream     // Catch:{ all -> 0x0100 }
            if (r11 == 0) goto L_0x019d
            java.io.OutputStream r11 = r15.dataTransferOutputStream     // Catch:{ Throwable -> 0x01e9 }
            r11.close()     // Catch:{ Throwable -> 0x01e9 }
        L_0x019d:
            r2.close()     // Catch:{ Throwable -> 0x01ce }
        L_0x01a0:
            r11 = 0
            r15.dataTransferOutputStream = r11     // Catch:{ all -> 0x0100 }
            java.lang.Object r13 = r15.abortLock     // Catch:{ all -> 0x0100 }
            monitor-enter(r13)     // Catch:{ all -> 0x0100 }
            boolean r9 = r15.aborted     // Catch:{ all -> 0x01d0 }
            r11 = 0
            r15.ongoingDataTransfer = r11     // Catch:{ all -> 0x01d0 }
            r11 = 0
            r15.aborted = r11     // Catch:{ all -> 0x01d0 }
            monitor-exit(r13)     // Catch:{ all -> 0x01d0 }
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r11 = r15.communication     // Catch:{ all -> 0x000f }
            it.sauronsoftware.ftp4j.FTPReply r6 = r11.readFTPReply()     // Catch:{ all -> 0x000f }
            r15.touchAutoNoopTimer()     // Catch:{ all -> 0x000f }
            int r11 = r6.getCode()     // Catch:{ all -> 0x000f }
            r13 = 150(0x96, float:2.1E-43)
            if (r11 == r13) goto L_0x01d3
            int r11 = r6.getCode()     // Catch:{ all -> 0x000f }
            r13 = 125(0x7d, float:1.75E-43)
            if (r11 == r13) goto L_0x01d3
            it.sauronsoftware.ftp4j.FTPException r11 = new it.sauronsoftware.ftp4j.FTPException     // Catch:{ all -> 0x000f }
            r11.<init>((p010it.sauronsoftware.ftp4j.FTPReply) r6)     // Catch:{ all -> 0x000f }
            throw r11     // Catch:{ all -> 0x000f }
        L_0x01ce:
            r11 = move-exception
            goto L_0x01a0
        L_0x01d0:
            r11 = move-exception
            monitor-exit(r13)     // Catch:{ all -> 0x01d0 }
            throw r11     // Catch:{ all -> 0x0100 }
        L_0x01d3:
            it.sauronsoftware.ftp4j.FTPCommunicationChannel r11 = r15.communication     // Catch:{ all -> 0x000f }
            it.sauronsoftware.ftp4j.FTPReply r6 = r11.readFTPReply()     // Catch:{ all -> 0x000f }
            if (r9 != 0) goto L_0x0165
            int r11 = r6.getCode()     // Catch:{ all -> 0x000f }
            r13 = 226(0xe2, float:3.17E-43)
            if (r11 == r13) goto L_0x0165
            it.sauronsoftware.ftp4j.FTPException r11 = new it.sauronsoftware.ftp4j.FTPException     // Catch:{ all -> 0x000f }
            r11.<init>((p010it.sauronsoftware.ftp4j.FTPReply) r6)     // Catch:{ all -> 0x000f }
            throw r11     // Catch:{ all -> 0x000f }
        L_0x01e9:
            r11 = move-exception
            goto L_0x019d
        L_0x01eb:
            r13 = move-exception
            goto L_0x00ed
        */
        throw new UnsupportedOperationException("Method not decompiled: p010it.sauronsoftware.ftp4j.FTPClient.append(java.lang.String, java.io.InputStream, long, it.sauronsoftware.ftp4j.FTPDataTransferListener):void");
    }

    public void download(String remoteFileName, File localFile) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        download(remoteFileName, localFile, 0, (FTPDataTransferListener) null);
    }

    public void download(String remoteFileName, File localFile, FTPDataTransferListener listener) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        download(remoteFileName, localFile, 0, listener);
    }

    public void download(String remoteFileName, File localFile, long restartAt) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        download(remoteFileName, localFile, restartAt, (FTPDataTransferListener) null);
    }

    public void download(String remoteFileName, File localFile, long restartAt, FTPDataTransferListener listener) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        try {
            OutputStream outputStream = new FileOutputStream(localFile, restartAt > 0);
            try {
                download(remoteFileName, outputStream, restartAt, listener);
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (Throwable th) {
                    }
                }
            } catch (IllegalStateException e) {
                throw e;
            } catch (IOException e2) {
                throw e2;
            } catch (FTPIllegalReplyException e3) {
                throw e3;
            } catch (FTPException e4) {
                throw e4;
            } catch (FTPDataTransferException e5) {
                throw e5;
            } catch (FTPAbortedException e6) {
                throw e6;
            } catch (Throwable th2) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (Throwable th3) {
                    }
                }
                throw th2;
            }
        } catch (IOException e7) {
            throw new FTPDataTransferException((Throwable) e7);
        }
    }

    public void download(String fileName, OutputStream outputStream, long restartAt, FTPDataTransferListener listener) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        boolean wasAborted;
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            } else if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            } else {
                int tp = this.type;
                if (tp == 0) {
                    tp = detectType(fileName);
                }
                if (tp == 1) {
                    this.communication.sendFTPCommand("TYPE A");
                } else if (tp == 2) {
                    this.communication.sendFTPCommand("TYPE I");
                }
                FTPReply r = this.communication.readFTPReply();
                touchAutoNoopTimer();
                if (!r.isSuccessCode()) {
                    throw new FTPException(r);
                }
                FTPDataTransferConnectionProvider provider = openDataTransferChannel();
                if (this.restSupported || restartAt > 0) {
                    try {
                        this.communication.sendFTPCommand(new StringBuffer().append("REST ").append(restartAt).toString());
                        FTPReply r2 = this.communication.readFTPReply();
                        touchAutoNoopTimer();
                        if (r2.getCode() != 350 && ((r2.getCode() != 501 && r2.getCode() != 502) || restartAt > 0)) {
                            throw new FTPException(r2);
                        } else if (1 == 0) {
                            provider.dispose();
                        }
                    } catch (Throwable th) {
                        if (0 == 0) {
                            provider.dispose();
                        }
                        throw th;
                    }
                }
                boolean wasAborted2 = false;
                this.communication.sendFTPCommand(new StringBuffer().append("RETR ").append(fileName).toString());
                try {
                    Socket dtConnection = provider.openDataTransferConnection();
                    provider.dispose();
                    synchronized (this.abortLock) {
                        this.ongoingDataTransfer = true;
                        this.aborted = false;
                        this.consumeAborCommandReply = false;
                    }
                    try {
                        this.dataTransferInputStream = dtConnection.getInputStream();
                        if (this.modezEnabled) {
                            this.dataTransferInputStream = new InflaterInputStream(this.dataTransferInputStream);
                        }
                        if (listener != null) {
                            listener.started();
                        }
                        if (tp == 1) {
                            Reader reader = new InputStreamReader(this.dataTransferInputStream, pickCharset());
                            Writer writer = new OutputStreamWriter(outputStream);
                            char[] buffer = new char[65536];
                            while (true) {
                                int l = reader.read(buffer, 0, buffer.length);
                                if (l == -1) {
                                    break;
                                }
                                writer.write(buffer, 0, l);
                                writer.flush();
                                if (listener != null) {
                                    listener.transferred(l);
                                }
                            }
                        } else if (tp == 2) {
                            byte[] buffer2 = new byte[65536];
                            while (true) {
                                int l2 = this.dataTransferInputStream.read(buffer2, 0, buffer2.length);
                                if (l2 == -1) {
                                    break;
                                }
                                outputStream.write(buffer2, 0, l2);
                                if (listener != null) {
                                    listener.transferred(l2);
                                }
                            }
                        }
                        if (this.dataTransferInputStream != null) {
                            try {
                                this.dataTransferInputStream.close();
                            } catch (Throwable th2) {
                            }
                        }
                        try {
                            dtConnection.close();
                        } catch (Throwable th3) {
                        }
                        this.dataTransferInputStream = null;
                        synchronized (this.abortLock) {
                            wasAborted = this.aborted;
                            this.ongoingDataTransfer = false;
                            this.aborted = false;
                        }
                        FTPReply r3 = this.communication.readFTPReply();
                        touchAutoNoopTimer();
                        if (r3.getCode() == 150 || r3.getCode() == 125) {
                            FTPReply r4 = this.communication.readFTPReply();
                            if (wasAborted || r4.getCode() == 226) {
                                if (this.consumeAborCommandReply) {
                                    this.communication.readFTPReply();
                                    this.consumeAborCommandReply = false;
                                }
                                if (listener != null) {
                                    listener.completed();
                                }
                            } else {
                                throw new FTPException(r4);
                            }
                        } else {
                            throw new FTPException(r3);
                        }
                    } catch (IOException e) {
                        synchronized (this.abortLock) {
                            if (this.aborted) {
                                if (listener != null) {
                                    listener.aborted();
                                }
                                throw new FTPAbortedException();
                            }
                            if (listener != null) {
                                listener.failed();
                            }
                            throw new FTPDataTransferException("I/O error in data transfer", e);
                        }
                    } catch (Throwable th4) {
                        if (this.dataTransferInputStream != null) {
                            this.dataTransferInputStream.close();
                        }
                        dtConnection.close();
                        this.dataTransferInputStream = null;
                        synchronized (this.abortLock) {
                            wasAborted2 = this.aborted;
                            this.ongoingDataTransfer = false;
                            this.aborted = false;
                            throw th4;
                        }
                    }
                } catch (Throwable th5) {
                }
            }
        }
    }

    private int detectType(String fileName) throws IOException, FTPIllegalReplyException, FTPException {
        int start = fileName.lastIndexOf(46) + 1;
        int stop = fileName.length();
        if (start <= 0 || start >= stop - 1) {
            return 2;
        }
        if (this.textualExtensionRecognizer.isTextualExt(fileName.substring(start, stop).toLowerCase())) {
            return 1;
        }
        return 2;
    }

    private FTPDataTransferConnectionProvider openDataTransferChannel() throws IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException {
        if (!this.modezSupported || !this.compressionEnabled) {
            if (this.modezEnabled) {
                this.communication.sendFTPCommand("MODE S");
                FTPReply r = this.communication.readFTPReply();
                touchAutoNoopTimer();
                if (r.isSuccessCode()) {
                    this.modezEnabled = false;
                }
            }
        } else if (!this.modezEnabled) {
            this.communication.sendFTPCommand("MODE Z");
            FTPReply r2 = this.communication.readFTPReply();
            touchAutoNoopTimer();
            if (r2.isSuccessCode()) {
                this.modezEnabled = true;
            }
        }
        if (this.passive) {
            return openPassiveDataTransferChannel();
        }
        return openActiveDataTransferChannel();
    }

    private FTPDataTransferConnectionProvider openActiveDataTransferChannel() throws IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException {
        FTPDataTransferServer server = new FTPDataTransferServer(this) {
            private final FTPClient this$0;

            {
                this.this$0 = r1;
            }

            public Socket openDataTransferConnection() throws FTPDataTransferException {
                Socket socket = super.openDataTransferConnection();
                if (!FTPClient.access$000(this.this$0)) {
                    return socket;
                }
                try {
                    return FTPClient.access$100(this.this$0, socket, socket.getInetAddress().getHostName(), socket.getPort());
                } catch (IOException e) {
                    socket.close();
                } catch (Throwable th) {
                }
                throw new FTPDataTransferException((Throwable) e);
            }
        };
        int port2 = server.getPort();
        int p2 = port2 & MotionEventCompat.ACTION_MASK;
        int[] addr = pickLocalAddress();
        this.communication.sendFTPCommand(new StringBuffer().append("PORT ").append(addr[0]).append(",").append(addr[1]).append(",").append(addr[2]).append(",").append(addr[3]).append(",").append(port2 >>> 8).append(",").append(p2).toString());
        FTPReply r = this.communication.readFTPReply();
        touchAutoNoopTimer();
        if (r.isSuccessCode()) {
            return server;
        }
        server.dispose();
        try {
            server.openDataTransferConnection().close();
        } catch (Throwable th) {
        }
        throw new FTPException(r);
    }

    private FTPDataTransferConnectionProvider openPassiveDataTransferChannel() throws IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException {
        this.communication.sendFTPCommand("PASV");
        FTPReply r = this.communication.readFTPReply();
        touchAutoNoopTimer();
        if (!r.isSuccessCode()) {
            throw new FTPException(r);
        }
        String addressAndPort = null;
        String[] messages = r.getMessages();
        int i = 0;
        while (true) {
            if (i >= messages.length) {
                break;
            }
            Matcher m = PASV_PATTERN.matcher(messages[i]);
            if (m.find()) {
                addressAndPort = messages[i].substring(m.start(), m.end());
                break;
            }
            i++;
        }
        if (addressAndPort == null) {
            throw new FTPIllegalReplyException();
        }
        StringTokenizer stringTokenizer = new StringTokenizer(addressAndPort, ",");
        int b1 = Integer.parseInt(stringTokenizer.nextToken());
        int b2 = Integer.parseInt(stringTokenizer.nextToken());
        int b3 = Integer.parseInt(stringTokenizer.nextToken());
        int b4 = Integer.parseInt(stringTokenizer.nextToken());
        int p1 = Integer.parseInt(stringTokenizer.nextToken());
        int p2 = Integer.parseInt(stringTokenizer.nextToken());
        return new FTPDataTransferConnectionProvider(this, new StringBuffer().append(b1).append(".").append(b2).append(".").append(b3).append(".").append(b4).toString(), (p1 << 8) | p2) {
            private final FTPClient this$0;
            private final String val$pasvHost;
            private final int val$pasvPort;

            {
                this.this$0 = r1;
                this.val$pasvHost = r2;
                this.val$pasvPort = r3;
            }

            public Socket openDataTransferConnection() throws FTPDataTransferException {
                try {
                    String selectedHost = FTPClient.access$200(this.this$0).getUseSuggestedAddressForDataConnections() ? this.val$pasvHost : FTPClient.access$300(this.this$0);
                    Socket dtConnection = FTPClient.access$200(this.this$0).connectForDataTransferChannel(selectedHost, this.val$pasvPort);
                    if (FTPClient.access$000(this.this$0)) {
                        return FTPClient.access$100(this.this$0, dtConnection, selectedHost, this.val$pasvPort);
                    }
                    return dtConnection;
                } catch (IOException e) {
                    throw new FTPDataTransferException("Cannot connect to the remote server", e);
                }
            }

            public void dispose() {
            }
        };
    }

    public void abortCurrentDataTransfer(boolean sendAborCommand) throws IOException, FTPIllegalReplyException {
        synchronized (this.abortLock) {
            if (this.ongoingDataTransfer && !this.aborted) {
                if (sendAborCommand) {
                    this.communication.sendFTPCommand("ABOR");
                    touchAutoNoopTimer();
                    this.consumeAborCommandReply = true;
                }
                if (this.dataTransferInputStream != null) {
                    try {
                        this.dataTransferInputStream.close();
                    } catch (Throwable th) {
                    }
                }
                if (this.dataTransferOutputStream != null) {
                    try {
                        this.dataTransferOutputStream.close();
                    } catch (Throwable th2) {
                    }
                }
                this.aborted = true;
            }
        }
    }

    private String pickCharset() {
        if (this.charset != null) {
            return this.charset;
        }
        if (this.utf8Supported) {
            return CharEncoding.UTF_8;
        }
        return System.getProperty("file.encoding");
    }

    private int[] pickLocalAddress() throws IOException {
        int[] ret = pickForcedLocalAddress();
        if (ret == null) {
            return pickAutoDetectedLocalAddress();
        }
        return ret;
    }

    private int[] pickForcedLocalAddress() {
        int[] ret = null;
        String aux = System.getProperty(FTPKeys.ACTIVE_DT_HOST_ADDRESS);
        if (aux != null) {
            boolean valid = false;
            StringTokenizer st = new StringTokenizer(aux, ".");
            if (st.countTokens() == 4) {
                valid = true;
                int[] arr = new int[4];
                int i = 0;
                while (true) {
                    if (i >= 4) {
                        break;
                    }
                    try {
                        arr[i] = Integer.parseInt(st.nextToken());
                    } catch (NumberFormatException e) {
                        arr[i] = -1;
                    }
                    if (arr[i] < 0 || arr[i] > 255) {
                        valid = false;
                    } else {
                        i++;
                    }
                }
                if (valid) {
                    ret = arr;
                }
            }
            if (!valid) {
                System.err.println(new StringBuffer().append("WARNING: invalid value \"").append(aux).append("\" for the ").append(FTPKeys.ACTIVE_DT_HOST_ADDRESS).append(" system property. The value should ").append("be in the x.x.x.x form.").toString());
            }
        }
        return ret;
    }

    private int[] pickAutoDetectedLocalAddress() throws IOException {
        byte[] addr = InetAddress.getLocalHost().getAddress();
        return new int[]{addr[0] & 255, addr[1] & 255, addr[2] & 255, addr[3] & 255};
    }

    public String toString() {
        String stringBuffer;
        synchronized (this.lock) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(getClass().getName());
            buffer.append(" [connected=");
            buffer.append(this.connected);
            if (this.connected) {
                buffer.append(", host=");
                buffer.append(this.host);
                buffer.append(", port=");
                buffer.append(this.port);
            }
            buffer.append(", connector=");
            buffer.append(this.connector);
            buffer.append(", security=");
            switch (this.security) {
                case 0:
                    buffer.append("SECURITY_FTP");
                    break;
                case 1:
                    buffer.append("SECURITY_FTPS");
                    break;
                case 2:
                    buffer.append("SECURITY_FTPES");
                    break;
            }
            buffer.append(", authenticated=");
            buffer.append(this.authenticated);
            if (this.authenticated) {
                buffer.append(", username=");
                buffer.append(this.username);
                buffer.append(", password=");
                StringBuffer buffer2 = new StringBuffer();
                for (int i = 0; i < this.password.length(); i++) {
                    buffer2.append('*');
                }
                buffer.append(buffer2);
                buffer.append(", restSupported=");
                buffer.append(this.restSupported);
                buffer.append(", utf8supported=");
                buffer.append(this.utf8Supported);
                buffer.append(", mlsdSupported=");
                buffer.append(this.mlsdSupported);
                buffer.append(", mode=modezSupported");
                buffer.append(this.modezSupported);
                buffer.append(", mode=modezEnabled");
                buffer.append(this.modezEnabled);
            }
            buffer.append(", transfer mode=");
            buffer.append(this.passive ? "passive" : CAPMessage.DIRECTIVE_ACTIVE);
            buffer.append(", transfer type=");
            switch (this.type) {
                case 0:
                    buffer.append("TYPE_AUTO");
                    break;
                case 1:
                    buffer.append("TYPE_TEXTUAL");
                    break;
                case 2:
                    buffer.append("TYPE_BINARY");
                    break;
            }
            buffer.append(", textualExtensionRecognizer=");
            buffer.append(this.textualExtensionRecognizer);
            FTPListParser[] listParsers2 = getListParsers();
            if (listParsers2.length > 0) {
                buffer.append(", listParsers=");
                for (int i2 = 0; i2 < listParsers2.length; i2++) {
                    if (i2 > 0) {
                        buffer.append(", ");
                    }
                    buffer.append(listParsers2[i2]);
                }
            }
            FTPCommunicationListener[] communicationListeners2 = getCommunicationListeners();
            if (communicationListeners2.length > 0) {
                buffer.append(", communicationListeners=");
                for (int i3 = 0; i3 < communicationListeners2.length; i3++) {
                    if (i3 > 0) {
                        buffer.append(", ");
                    }
                    buffer.append(communicationListeners2[i3]);
                }
            }
            buffer.append(", autoNoopTimeout=");
            buffer.append(this.autoNoopTimeout);
            buffer.append("]");
            stringBuffer = buffer.toString();
        }
        return stringBuffer;
    }

    private void startAutoNoopTimer() {
        if (this.autoNoopTimeout > 0) {
            this.autoNoopTimer = new AutoNoopTimer(this, (C04261) null);
            this.autoNoopTimer.start();
        }
    }

    private void stopAutoNoopTimer() {
        if (this.autoNoopTimer != null) {
            this.autoNoopTimer.interrupt();
            this.autoNoopTimer = null;
        }
    }

    private void touchAutoNoopTimer() {
        if (this.autoNoopTimer != null) {
            this.nextAutoNoopTime = System.currentTimeMillis() + this.autoNoopTimeout;
        }
    }

    /* renamed from: it.sauronsoftware.ftp4j.FTPClient$AutoNoopTimer */
    private class AutoNoopTimer extends Thread {
        private final FTPClient this$0;

        private AutoNoopTimer(FTPClient fTPClient) {
            this.this$0 = fTPClient;
        }

        AutoNoopTimer(FTPClient x0, C04261 x1) {
            this(x0);
        }

        public void run() {
            synchronized (FTPClient.access$500(this.this$0)) {
                if (FTPClient.access$600(this.this$0) <= 0 && FTPClient.access$700(this.this$0) > 0) {
                    FTPClient.access$602(this.this$0, System.currentTimeMillis() + FTPClient.access$700(this.this$0));
                }
                while (!Thread.interrupted() && FTPClient.access$700(this.this$0) > 0) {
                    long delay = FTPClient.access$600(this.this$0) - System.currentTimeMillis();
                    if (delay > 0) {
                        try {
                            FTPClient.access$500(this.this$0).wait(delay);
                        } catch (InterruptedException e) {
                        }
                    }
                    if (System.currentTimeMillis() >= FTPClient.access$600(this.this$0)) {
                        try {
                            this.this$0.noop();
                        } catch (Throwable th) {
                        }
                    }
                }
            }
        }
    }
}
