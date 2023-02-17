package it.sauronsoftware.ftp4j;

import android.support.v4.view.MotionEventCompat;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import it.sauronsoftware.ftp4j.connectors.DirectConnector;
import it.sauronsoftware.ftp4j.extrecognizers.DefaultTextualExtensionRecognizer;
import it.sauronsoftware.ftp4j.listparsers.DOSListParser;
import it.sauronsoftware.ftp4j.listparsers.EPLFListParser;
import it.sauronsoftware.ftp4j.listparsers.MLSDListParser;
import it.sauronsoftware.ftp4j.listparsers.NetWareListParser;
import it.sauronsoftware.ftp4j.listparsers.UnixListParser;
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

/* loaded from: classes.dex */
public class FTPClient {
    public static final int MLSD_ALWAYS = 1;
    public static final int MLSD_IF_SUPPORTED = 0;
    public static final int MLSD_NEVER = 2;
    public static final int SECURITY_FTP = 0;
    public static final int SECURITY_FTPES = 2;
    public static final int SECURITY_FTPS = 1;
    private static final int SEND_AND_RECEIVE_BUFFER_SIZE = 65536;
    public static final int TYPE_AUTO = 0;
    public static final int TYPE_BINARY = 2;
    public static final int TYPE_TEXTUAL = 1;
    private AutoNoopTimer autoNoopTimer;
    private long nextAutoNoopTime;
    private String password;
    private String username;
    private static final DateFormat MDTM_DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final Pattern PASV_PATTERN = Pattern.compile("\\d{1,3},\\d{1,3},\\d{1,3},\\d{1,3},\\d{1,3},\\d{1,3}");
    private static final Pattern PWD_PATTERN = Pattern.compile("\"/.*\"");
    private FTPConnector connector = new DirectConnector();
    private SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    private ArrayList communicationListeners = new ArrayList();
    private ArrayList listParsers = new ArrayList();
    private FTPTextualExtensionRecognizer textualExtensionRecognizer = DefaultTextualExtensionRecognizer.getInstance();
    private FTPListParser parser = null;
    private String host = null;
    private int port = 0;
    private int security = 0;
    private boolean connected = false;
    private boolean authenticated = false;
    private boolean passive = true;
    private int type = 0;
    private int mlsdPolicy = 0;
    private long autoNoopTimeout = 0;
    private boolean restSupported = false;
    private String charset = null;
    private boolean compressionEnabled = false;
    private boolean utf8Supported = false;
    private boolean mlsdSupported = false;
    private boolean modezSupported = false;
    private boolean modezEnabled = false;
    private boolean dataChannelEncrypted = false;
    private boolean ongoingDataTransfer = false;
    private InputStream dataTransferInputStream = null;
    private OutputStream dataTransferOutputStream = null;
    private boolean aborted = false;
    private boolean consumeAborCommandReply = false;
    private Object lock = new Object();
    private Object abortLock = new Object();
    private FTPCommunicationChannel communication = null;

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

    public void setConnector(FTPConnector connector) {
        synchronized (this.lock) {
            this.connector = connector;
        }
    }

    public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
        synchronized (this.lock) {
            this.sslSocketFactory = sslSocketFactory;
        }
    }

    public SSLSocketFactory getSSLSocketFactory() {
        SSLSocketFactory sSLSocketFactory;
        synchronized (this.lock) {
            sSLSocketFactory = this.sslSocketFactory;
        }
        return sSLSocketFactory;
    }

    public void setSecurity(int security) throws IllegalStateException, IllegalArgumentException {
        if (security != 0 && security != 1 && security != 2) {
            throw new IllegalArgumentException("Invalid security");
        }
        synchronized (this.lock) {
            if (this.connected) {
                throw new IllegalStateException("The security level of the connection can't be changed while the client is connected");
            }
            this.security = security;
        }
    }

    public int getSecurity() {
        return this.security;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Socket ssl(Socket socket, String host, int port) throws IOException {
        return this.sslSocketFactory.createSocket(socket, host, port, true);
    }

    public void setPassive(boolean passive) {
        synchronized (this.lock) {
            this.passive = passive;
        }
    }

    public void setType(int type) throws IllegalArgumentException {
        if (type != 0 && type != 2 && type != 1) {
            throw new IllegalArgumentException("Invalid type");
        }
        synchronized (this.lock) {
            this.type = type;
        }
    }

    public int getType() {
        int i;
        synchronized (this.lock) {
            i = this.type;
        }
        return i;
    }

    public void setMLSDPolicy(int mlsdPolicy) throws IllegalArgumentException {
        if (this.type != 0 && this.type != 1 && this.type != 2) {
            throw new IllegalArgumentException("Invalid MLSD policy");
        }
        synchronized (this.lock) {
            this.mlsdPolicy = mlsdPolicy;
        }
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

    public void setCharset(String charset) {
        synchronized (this.lock) {
            this.charset = charset;
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

    public void setCompressionEnabled(boolean compressionEnabled) {
        this.compressionEnabled = compressionEnabled;
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

    public void setTextualExtensionRecognizer(FTPTextualExtensionRecognizer textualExtensionRecognizer) {
        synchronized (this.lock) {
            this.textualExtensionRecognizer = textualExtensionRecognizer;
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

    public void setAutoNoopTimeout(long autoNoopTimeout) {
        synchronized (this.lock) {
            if (this.connected && this.authenticated) {
                stopAutoNoopTimer();
            }
            long oldValue = this.autoNoopTimeout;
            this.autoNoopTimeout = autoNoopTimeout;
            if (oldValue != 0 && autoNoopTimeout != 0 && this.nextAutoNoopTime > 0) {
                this.nextAutoNoopTime -= oldValue - autoNoopTimeout;
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

    public String[] connect(String host) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        int def;
        if (this.security == 1) {
            def = 990;
        } else {
            def = 21;
        }
        return connect(host, def);
    }

    public String[] connect(String host, int port) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        String[] messages;
        synchronized (this.lock) {
            if (this.connected) {
                throw new IllegalStateException(new StringBuffer().append("Client already connected to ").append(host).append(" on port ").append(port).toString());
            }
            try {
                Socket connection = this.connector.connectForCommunicationChannel(host, port);
                if (this.security == 1) {
                    connection = ssl(connection, host, port);
                }
                this.communication = new FTPCommunicationChannel(connection, pickCharset());
                Iterator i = this.communicationListeners.iterator();
                while (i.hasNext()) {
                    this.communication.addCommunicationListener((FTPCommunicationListener) i.next());
                }
                FTPReply wm = this.communication.readFTPReply();
                if (!wm.isSuccessCode()) {
                    throw new FTPException(wm);
                }
                this.connected = true;
                this.authenticated = false;
                this.parser = null;
                this.host = host;
                this.port = port;
                this.username = null;
                this.password = null;
                this.utf8Supported = false;
                this.restSupported = false;
                this.mlsdSupported = false;
                this.modezSupported = false;
                this.dataChannelEncrypted = false;
                messages = wm.getMessages();
                if (!this.connected && connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable th) {
                    }
                }
            } catch (IOException e) {
                throw e;
            }
        }
        return messages;
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

    public void login(String username, String password) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        login(username, password, null);
    }

    /* JADX WARN: Removed duplicated region for block: B:27:0x008a  */
    /* JADX WARN: Removed duplicated region for block: B:38:0x00c8  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public void login(java.lang.String r8, java.lang.String r9, java.lang.String r10) throws java.lang.IllegalStateException, java.io.IOException, it.sauronsoftware.ftp4j.FTPIllegalReplyException, it.sauronsoftware.ftp4j.FTPException {
        /*
            Method dump skipped, instructions count: 300
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: it.sauronsoftware.ftp4j.FTPClient.login(java.lang.String, java.lang.String, java.lang.String):void");
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
                FTPReply reply = this.communication.readFTPReply();
                if (reply.isSuccessCode()) {
                    this.dataChannelEncrypted = true;
                }
            }
        }
    }

    public void logout() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            }
            if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            }
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

    public void noop() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            }
            if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            }
            this.communication.sendFTPCommand("NOOP");
            FTPReply r = this.communication.readFTPReply();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
            touchAutoNoopTimer();
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
            }
            if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            }
            this.communication.sendFTPCommand(new StringBuffer().append("ACCT ").append(account).toString());
            FTPReply r = this.communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
        }
    }

    public String currentDirectory() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        String substring;
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            }
            if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            }
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
        return substring;
    }

    public void changeDirectory(String path) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            }
            if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            }
            this.communication.sendFTPCommand(new StringBuffer().append("CWD ").append(path).toString());
            FTPReply r = this.communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
        }
    }

    public void changeDirectoryUp() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            }
            if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            }
            this.communication.sendFTPCommand("CDUP");
            FTPReply r = this.communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
        }
    }

    public Date modifiedDate(String path) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        Date parse;
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            }
            if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            }
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
        return parse;
    }

    public long fileSize(String path) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        long parseLong;
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            }
            if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            }
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
            parseLong = Long.parseLong(messages[0]);
        }
        return parseLong;
    }

    public void rename(String oldPath, String newPath) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            }
            if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            }
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

    public void deleteFile(String path) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            }
            if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            }
            this.communication.sendFTPCommand(new StringBuffer().append("DELE ").append(path).toString());
            FTPReply r = this.communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
        }
    }

    public void deleteDirectory(String path) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            }
            if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            }
            this.communication.sendFTPCommand(new StringBuffer().append("RMD ").append(path).toString());
            FTPReply r = this.communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
        }
    }

    public void createDirectory(String directoryName) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            }
            if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            }
            this.communication.sendFTPCommand(new StringBuffer().append("MKD ").append(directoryName).toString());
            FTPReply r = this.communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
        }
    }

    public String[] help() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        String[] messages;
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            }
            if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            }
            this.communication.sendFTPCommand("HELP");
            FTPReply r = this.communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
            messages = r.getMessages();
        }
        return messages;
    }

    public String[] serverStatus() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException {
        String[] messages;
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            }
            if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            }
            this.communication.sendFTPCommand("STAT");
            FTPReply r = this.communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
            messages = r.getMessages();
        }
        return messages;
    }

    public FTPFile[] list(String fileSpec) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException, FTPListParseException {
        boolean wasAborted;
        FTPFile[] ret;
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            }
            if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            }
            this.communication.sendFTPCommand("TYPE A");
            FTPReply r = this.communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
            FTPDataTransferConnectionProvider provider = openDataTransferChannel();
            boolean mlsdCommand = this.mlsdPolicy == 0 ? this.mlsdSupported : this.mlsdPolicy == 1;
            String command = mlsdCommand ? "MLSD" : "LIST";
            if (fileSpec != null && fileSpec.length() > 0) {
                command = new StringBuffer().append(command).append(StringUtils.SPACE).append(fileSpec).toString();
            }
            ArrayList lines = new ArrayList();
            this.communication.sendFTPCommand(command);
            Socket dtConnection = provider.openDataTransferConnection();
            provider.dispose();
            synchronized (this.abortLock) {
                this.ongoingDataTransfer = true;
                this.aborted = false;
                this.consumeAborCommandReply = false;
            }
            NVTASCIIReader dataReader = null;
            try {
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
                            synchronized (this.abortLock) {
                                if (this.aborted) {
                                    throw new FTPAbortedException();
                                }
                                throw new FTPDataTransferException("I/O error in data transfer", e);
                            }
                        } catch (Throwable th) {
                            th = th;
                            dataReader = dataReader2;
                            if (dataReader != null) {
                                try {
                                    dataReader.close();
                                } catch (Throwable th2) {
                                }
                            }
                            try {
                                dtConnection.close();
                            } catch (Throwable th3) {
                            }
                            this.dataTransferInputStream = null;
                            synchronized (this.abortLock) {
                                boolean z = this.aborted;
                                this.ongoingDataTransfer = false;
                                this.aborted = false;
                            }
                            throw th;
                        }
                    }
                    if (dataReader2 != null) {
                        try {
                            dataReader2.close();
                        } catch (Throwable th4) {
                        }
                    }
                    try {
                        dtConnection.close();
                    } catch (Throwable th5) {
                    }
                    this.dataTransferInputStream = null;
                    synchronized (this.abortLock) {
                        wasAborted = this.aborted;
                        this.ongoingDataTransfer = false;
                        this.aborted = false;
                    }
                    FTPReply r2 = this.communication.readFTPReply();
                    touchAutoNoopTimer();
                    if (r2.getCode() != 150 && r2.getCode() != 125) {
                        throw new FTPException(r2);
                    }
                    FTPReply r3 = this.communication.readFTPReply();
                    if (!wasAborted && r3.getCode() != 226) {
                        throw new FTPException(r3);
                    }
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
                        MLSDListParser parser = new MLSDListParser();
                        ret = parser.parse(list);
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
                } catch (IOException e4) {
                    e = e4;
                }
            } catch (Throwable th6) {
                th = th6;
            }
        }
        return ret;
    }

    public FTPFile[] list() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException, FTPListParseException {
        return list(null);
    }

    public String[] listNames() throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException, FTPListParseException {
        boolean wasAborted;
        String[] list;
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            }
            if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            }
            this.communication.sendFTPCommand("TYPE A");
            FTPReply r = this.communication.readFTPReply();
            touchAutoNoopTimer();
            if (!r.isSuccessCode()) {
                throw new FTPException(r);
            }
            ArrayList lines = new ArrayList();
            FTPDataTransferConnectionProvider provider = openDataTransferChannel();
            this.communication.sendFTPCommand("NLST");
            Socket dtConnection = provider.openDataTransferConnection();
            provider.dispose();
            synchronized (this.abortLock) {
                this.ongoingDataTransfer = true;
                this.aborted = false;
                this.consumeAborCommandReply = false;
            }
            NVTASCIIReader dataReader = null;
            try {
                try {
                    this.dataTransferInputStream = dtConnection.getInputStream();
                    if (this.modezEnabled) {
                        this.dataTransferInputStream = new InflaterInputStream(this.dataTransferInputStream);
                    }
                    NVTASCIIReader dataReader2 = new NVTASCIIReader(this.dataTransferInputStream, pickCharset());
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
                            synchronized (this.abortLock) {
                                if (this.aborted) {
                                    throw new FTPAbortedException();
                                }
                                throw new FTPDataTransferException("I/O error in data transfer", e);
                            }
                        } catch (Throwable th) {
                            th = th;
                            dataReader = dataReader2;
                            if (dataReader != null) {
                                try {
                                    dataReader.close();
                                } catch (Throwable th2) {
                                }
                            }
                            try {
                                dtConnection.close();
                            } catch (Throwable th3) {
                            }
                            this.dataTransferInputStream = null;
                            synchronized (this.abortLock) {
                                boolean wasAborted2 = this.aborted;
                                this.ongoingDataTransfer = false;
                                this.aborted = false;
                            }
                            throw th;
                        }
                    }
                    if (dataReader2 != null) {
                        try {
                            dataReader2.close();
                        } catch (Throwable th4) {
                        }
                    }
                    try {
                        dtConnection.close();
                    } catch (Throwable th5) {
                    }
                    this.dataTransferInputStream = null;
                    synchronized (this.abortLock) {
                        wasAborted = this.aborted;
                        this.ongoingDataTransfer = false;
                        this.aborted = false;
                    }
                    FTPReply r2 = this.communication.readFTPReply();
                    if (r2.getCode() != 150 && r2.getCode() != 125) {
                        throw new FTPException(r2);
                    }
                    FTPReply r3 = this.communication.readFTPReply();
                    if (!wasAborted && r3.getCode() != 226) {
                        throw new FTPException(r3);
                    }
                    if (this.consumeAborCommandReply) {
                        this.communication.readFTPReply();
                        this.consumeAborCommandReply = false;
                    }
                    int size = lines.size();
                    list = new String[size];
                    for (int i = 0; i < size; i++) {
                        list[i] = (String) lines.get(i);
                    }
                } catch (IOException e2) {
                    e = e2;
                }
            } catch (Throwable th6) {
                th = th6;
            }
        }
        return list;
    }

    public void upload(File file) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        upload(file, 0L, null);
    }

    public void upload(File file, FTPDataTransferListener listener) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        upload(file, 0L, listener);
    }

    public void upload(File file, long restartAt) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        upload(file, restartAt, null);
    }

    public void upload(File file, long restartAt, FTPDataTransferListener listener) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        try {
            InputStream inputStream = new FileInputStream(file);
            try {
                try {
                    try {
                        try {
                            try {
                                try {
                                    try {
                                        upload(file.getName(), inputStream, restartAt, restartAt, listener);
                                        if (inputStream != null) {
                                            try {
                                                inputStream.close();
                                            } catch (Throwable th) {
                                            }
                                        }
                                    } catch (FTPIllegalReplyException e) {
                                        throw e;
                                    }
                                } catch (FTPDataTransferException e2) {
                                    throw e2;
                                }
                            } catch (FTPAbortedException e3) {
                                throw e3;
                            }
                        } catch (FTPException e4) {
                            throw e4;
                        }
                    } catch (IOException e5) {
                        throw e5;
                    }
                } catch (IllegalStateException e6) {
                    throw e6;
                }
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
            throw new FTPDataTransferException(e7);
        }
    }

    public void upload(String fileName, InputStream inputStream, long restartAt, long streamOffset, FTPDataTransferListener listener) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        boolean wasAborted;
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            }
            if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            }
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
                this.communication.sendFTPCommand(new StringBuffer().append("REST ").append(restartAt).toString());
                FTPReply r2 = this.communication.readFTPReply();
                touchAutoNoopTimer();
                if (r2.getCode() != 350 && ((r2.getCode() != 501 && r2.getCode() != 502) || restartAt > 0)) {
                    throw new FTPException(r2);
                }
                if (1 == 0) {
                    provider.dispose();
                }
            }
            this.communication.sendFTPCommand(new StringBuffer().append("STOR ").append(fileName).toString());
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
                    } catch (Throwable th) {
                    }
                }
                try {
                    dtConnection.close();
                } catch (Throwable th2) {
                }
                this.dataTransferOutputStream = null;
                synchronized (this.abortLock) {
                    wasAborted = this.aborted;
                    this.ongoingDataTransfer = false;
                    this.aborted = false;
                }
                FTPReply r3 = this.communication.readFTPReply();
                touchAutoNoopTimer();
                if (r3.getCode() != 150 && r3.getCode() != 125) {
                    throw new FTPException(r3);
                }
                FTPReply r4 = this.communication.readFTPReply();
                if (!wasAborted && r4.getCode() != 226) {
                    throw new FTPException(r4);
                }
                if (this.consumeAborCommandReply) {
                    this.communication.readFTPReply();
                    this.consumeAborCommandReply = false;
                }
                if (listener != null) {
                    listener.completed();
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
            }
        }
    }

    public void append(File file) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        append(file, null);
    }

    public void append(File file, FTPDataTransferListener listener) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        try {
            InputStream inputStream = new FileInputStream(file);
            try {
                try {
                    try {
                        try {
                            try {
                                try {
                                    try {
                                        append(file.getName(), inputStream, 0L, listener);
                                        if (inputStream != null) {
                                            try {
                                                inputStream.close();
                                            } catch (Throwable th) {
                                            }
                                        }
                                    } catch (FTPIllegalReplyException e) {
                                        throw e;
                                    }
                                } catch (FTPDataTransferException e2) {
                                    throw e2;
                                }
                            } catch (FTPAbortedException e3) {
                                throw e3;
                            }
                        } catch (FTPException e4) {
                            throw e4;
                        }
                    } catch (IOException e5) {
                        throw e5;
                    }
                } catch (IllegalStateException e6) {
                    throw e6;
                }
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
            throw new FTPDataTransferException(e7);
        }
    }

    public void append(String fileName, InputStream inputStream, long streamOffset, FTPDataTransferListener listener) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        boolean wasAborted;
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            }
            if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            }
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
            this.communication.sendFTPCommand(new StringBuffer().append("APPE ").append(fileName).toString());
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
                    } catch (Throwable th) {
                    }
                }
                try {
                    dtConnection.close();
                } catch (Throwable th2) {
                }
                this.dataTransferOutputStream = null;
                synchronized (this.abortLock) {
                    wasAborted = this.aborted;
                    this.ongoingDataTransfer = false;
                    this.aborted = false;
                }
                FTPReply r2 = this.communication.readFTPReply();
                touchAutoNoopTimer();
                if (r2.getCode() != 150 && r2.getCode() != 125) {
                    throw new FTPException(r2);
                }
                FTPReply r3 = this.communication.readFTPReply();
                if (!wasAborted && r3.getCode() != 226) {
                    throw new FTPException(r3);
                }
                if (this.consumeAborCommandReply) {
                    this.communication.readFTPReply();
                    this.consumeAborCommandReply = false;
                }
                if (listener != null) {
                    listener.completed();
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
            }
        }
    }

    public void download(String remoteFileName, File localFile) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        download(remoteFileName, localFile, 0L, (FTPDataTransferListener) null);
    }

    public void download(String remoteFileName, File localFile, FTPDataTransferListener listener) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        download(remoteFileName, localFile, 0L, listener);
    }

    public void download(String remoteFileName, File localFile, long restartAt) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        download(remoteFileName, localFile, restartAt, (FTPDataTransferListener) null);
    }

    public void download(String remoteFileName, File localFile, long restartAt, FTPDataTransferListener listener) throws IllegalStateException, FileNotFoundException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        try {
            OutputStream outputStream = new FileOutputStream(localFile, restartAt > 0);
            try {
                try {
                    try {
                        try {
                            try {
                                try {
                                    download(remoteFileName, outputStream, restartAt, listener);
                                    if (outputStream != null) {
                                        try {
                                            outputStream.close();
                                        } catch (Throwable th) {
                                        }
                                    }
                                } catch (FTPDataTransferException e) {
                                    throw e;
                                } catch (FTPException e2) {
                                    throw e2;
                                }
                            } catch (IOException e3) {
                                throw e3;
                            }
                        } catch (FTPIllegalReplyException e4) {
                            throw e4;
                        }
                    } catch (FTPAbortedException e5) {
                        throw e5;
                    }
                } catch (IllegalStateException e6) {
                    throw e6;
                }
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
            throw new FTPDataTransferException(e7);
        }
    }

    public void download(String fileName, OutputStream outputStream, long restartAt, FTPDataTransferListener listener) throws IllegalStateException, IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException, FTPAbortedException {
        boolean wasAborted;
        synchronized (this.lock) {
            if (!this.connected) {
                throw new IllegalStateException("Client not connected");
            }
            if (!this.authenticated) {
                throw new IllegalStateException("Client not authenticated");
            }
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
                this.communication.sendFTPCommand(new StringBuffer().append("REST ").append(restartAt).toString());
                FTPReply r2 = this.communication.readFTPReply();
                touchAutoNoopTimer();
                if (r2.getCode() != 350 && ((r2.getCode() != 501 && r2.getCode() != 502) || restartAt > 0)) {
                    throw new FTPException(r2);
                }
                if (1 == 0) {
                    provider.dispose();
                }
            }
            this.communication.sendFTPCommand(new StringBuffer().append("RETR ").append(fileName).toString());
            try {
                Socket dtConnection = provider.openDataTransferConnection();
                provider.dispose();
                synchronized (this.abortLock) {
                    this.ongoingDataTransfer = true;
                    this.aborted = false;
                    this.consumeAborCommandReply = false;
                    try {
                    } catch (Throwable th) {
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
                            boolean wasAborted2 = this.aborted;
                            this.ongoingDataTransfer = false;
                            this.aborted = false;
                            throw th;
                        }
                    }
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
                        } catch (Throwable th4) {
                        }
                    }
                    try {
                        dtConnection.close();
                    } catch (Throwable th5) {
                    }
                    this.dataTransferInputStream = null;
                    synchronized (this.abortLock) {
                        wasAborted = this.aborted;
                        this.ongoingDataTransfer = false;
                        this.aborted = false;
                    }
                    FTPReply r3 = this.communication.readFTPReply();
                    touchAutoNoopTimer();
                    if (r3.getCode() != 150 && r3.getCode() != 125) {
                        throw new FTPException(r3);
                    }
                    FTPReply r4 = this.communication.readFTPReply();
                    if (!wasAborted && r4.getCode() != 226) {
                        throw new FTPException(r4);
                    }
                    if (this.consumeAborCommandReply) {
                        this.communication.readFTPReply();
                        this.consumeAborCommandReply = false;
                    }
                    if (listener != null) {
                        listener.completed();
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
                }
            } catch (Throwable th6) {
                provider.dispose();
                throw th6;
            }
        }
    }

    private int detectType(String fileName) throws IOException, FTPIllegalReplyException, FTPException {
        int start = fileName.lastIndexOf(46) + 1;
        int stop = fileName.length();
        if (start <= 0 || start >= stop - 1) {
            return 2;
        }
        String ext = fileName.substring(start, stop);
        return this.textualExtensionRecognizer.isTextualExt(ext.toLowerCase()) ? 1 : 2;
    }

    private FTPDataTransferConnectionProvider openDataTransferChannel() throws IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException {
        if (this.modezSupported && this.compressionEnabled) {
            if (!this.modezEnabled) {
                this.communication.sendFTPCommand("MODE Z");
                FTPReply r = this.communication.readFTPReply();
                touchAutoNoopTimer();
                if (r.isSuccessCode()) {
                    this.modezEnabled = true;
                }
            }
        } else if (this.modezEnabled) {
            this.communication.sendFTPCommand("MODE S");
            FTPReply r2 = this.communication.readFTPReply();
            touchAutoNoopTimer();
            if (r2.isSuccessCode()) {
                this.modezEnabled = false;
            }
        }
        if (this.passive) {
            return openPassiveDataTransferChannel();
        }
        return openActiveDataTransferChannel();
    }

    private FTPDataTransferConnectionProvider openActiveDataTransferChannel() throws IOException, FTPIllegalReplyException, FTPException, FTPDataTransferException {
        FTPDataTransferServer server = new FTPDataTransferServer(this) { // from class: it.sauronsoftware.ftp4j.FTPClient.1
            private final FTPClient this$0;

            {
                this.this$0 = this;
            }

            @Override // it.sauronsoftware.ftp4j.FTPDataTransferServer, it.sauronsoftware.ftp4j.FTPDataTransferConnectionProvider
            public Socket openDataTransferConnection() throws FTPDataTransferException {
                Socket socket = super.openDataTransferConnection();
                if (this.this$0.dataChannelEncrypted) {
                    try {
                        return this.this$0.ssl(socket, socket.getInetAddress().getHostName(), socket.getPort());
                    } catch (IOException e) {
                        try {
                            socket.close();
                        } catch (Throwable th) {
                        }
                        throw new FTPDataTransferException(e);
                    }
                }
                return socket;
            }
        };
        int port = server.getPort();
        int p1 = port >>> 8;
        int p2 = port & MotionEventCompat.ACTION_MASK;
        int[] addr = pickLocalAddress();
        this.communication.sendFTPCommand(new StringBuffer().append("PORT ").append(addr[0]).append(",").append(addr[1]).append(",").append(addr[2]).append(",").append(addr[3]).append(",").append(p1).append(",").append(p2).toString());
        FTPReply r = this.communication.readFTPReply();
        touchAutoNoopTimer();
        if (!r.isSuccessCode()) {
            server.dispose();
            try {
                Socket aux = server.openDataTransferConnection();
                aux.close();
            } catch (Throwable th) {
            }
            throw new FTPException(r);
        }
        return server;
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
            if (!m.find()) {
                i++;
            } else {
                int start = m.start();
                int end = m.end();
                addressAndPort = messages[i].substring(start, end);
                break;
            }
        }
        if (addressAndPort == null) {
            throw new FTPIllegalReplyException();
        }
        StringTokenizer st = new StringTokenizer(addressAndPort, ",");
        int b1 = Integer.parseInt(st.nextToken());
        int b2 = Integer.parseInt(st.nextToken());
        int b3 = Integer.parseInt(st.nextToken());
        int b4 = Integer.parseInt(st.nextToken());
        int p1 = Integer.parseInt(st.nextToken());
        int p2 = Integer.parseInt(st.nextToken());
        String pasvHost = new StringBuffer().append(b1).append(".").append(b2).append(".").append(b3).append(".").append(b4).toString();
        int pasvPort = (p1 << 8) | p2;
        FTPDataTransferConnectionProvider provider = new FTPDataTransferConnectionProvider(this, pasvHost, pasvPort) { // from class: it.sauronsoftware.ftp4j.FTPClient.2
            private final FTPClient this$0;
            private final String val$pasvHost;
            private final int val$pasvPort;

            {
                this.this$0 = this;
                this.val$pasvHost = pasvHost;
                this.val$pasvPort = pasvPort;
            }

            @Override // it.sauronsoftware.ftp4j.FTPDataTransferConnectionProvider
            public Socket openDataTransferConnection() throws FTPDataTransferException {
                try {
                    String selectedHost = this.this$0.connector.getUseSuggestedAddressForDataConnections() ? this.val$pasvHost : this.this$0.host;
                    Socket dtConnection = this.this$0.connector.connectForDataTransferChannel(selectedHost, this.val$pasvPort);
                    if (this.this$0.dataChannelEncrypted) {
                        return this.this$0.ssl(dtConnection, selectedHost, this.val$pasvPort);
                    }
                    return dtConnection;
                } catch (IOException e) {
                    throw new FTPDataTransferException("Cannot connect to the remote server", e);
                }
            }

            @Override // it.sauronsoftware.ftp4j.FTPDataTransferConnectionProvider
            public void dispose() {
            }
        };
        return provider;
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
                for (int i = 0; i < 4; i++) {
                    String tk = st.nextToken();
                    try {
                        arr[i] = Integer.parseInt(tk);
                    } catch (NumberFormatException e) {
                        arr[i] = -1;
                    }
                    if (arr[i] < 0 || arr[i] > 255) {
                        valid = false;
                        break;
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
        InetAddress addressObj = InetAddress.getLocalHost();
        byte[] addr = addressObj.getAddress();
        int b1 = addr[0] & 255;
        int b2 = addr[1] & 255;
        int b3 = addr[2] & 255;
        int b4 = addr[3] & 255;
        int[] ret = {b1, b2, b3, b4};
        return ret;
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
            FTPListParser[] listParsers = getListParsers();
            if (listParsers.length > 0) {
                buffer.append(", listParsers=");
                for (int i2 = 0; i2 < listParsers.length; i2++) {
                    if (i2 > 0) {
                        buffer.append(", ");
                    }
                    buffer.append(listParsers[i2]);
                }
            }
            FTPCommunicationListener[] communicationListeners = getCommunicationListeners();
            if (communicationListeners.length > 0) {
                buffer.append(", communicationListeners=");
                for (int i3 = 0; i3 < communicationListeners.length; i3++) {
                    if (i3 > 0) {
                        buffer.append(", ");
                    }
                    buffer.append(communicationListeners[i3]);
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
            this.autoNoopTimer = new AutoNoopTimer(this, null);
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

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class AutoNoopTimer extends Thread {
        private final FTPClient this$0;

        private AutoNoopTimer(FTPClient fTPClient) {
            this.this$0 = fTPClient;
        }

        AutoNoopTimer(FTPClient x0, AnonymousClass1 x1) {
            this(x0);
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            synchronized (this.this$0.lock) {
                if (this.this$0.nextAutoNoopTime <= 0 && this.this$0.autoNoopTimeout > 0) {
                    this.this$0.nextAutoNoopTime = System.currentTimeMillis() + this.this$0.autoNoopTimeout;
                }
                while (!Thread.interrupted() && this.this$0.autoNoopTimeout > 0) {
                    long delay = this.this$0.nextAutoNoopTime - System.currentTimeMillis();
                    if (delay > 0) {
                        try {
                            this.this$0.lock.wait(delay);
                        } catch (InterruptedException e) {
                        }
                    }
                    if (System.currentTimeMillis() >= this.this$0.nextAutoNoopTime) {
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
