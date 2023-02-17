package net.xcharge.sdk.server.coder.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* loaded from: classes.dex */
public class PropertiesUtil {
    private static final Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    public static String loadFileInJar(Class basePath, String resource) {
        BufferedReader reader = null;
        try {
            try {
                String url = basePath.newInstance().getClass().getProtectionDomain().getCodeSource().getLocation().toString();
                if (!url.startsWith("jar:")) {
                    url = "jar:" + url;
                }
                if (url.contains("!")) {
                    url = url.substring(0, url.indexOf("!"));
                }
                String url2 = url + "!" + resource;
                System.out.println(url2);
                StringBuilder sb = new StringBuilder();
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(new URL(url2).openStream()));
                while (true) {
                    try {
                        String line = reader2.readLine();
                        if (line == null) {
                            break;
                        }
                        sb.append(line);
                    } catch (Exception e) {
                        e = e;
                        reader = reader2;
                        logger.error("", (Throwable) e);
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e2) {
                                logger.info("", (Throwable) e2);
                            }
                        }
                        return null;
                    } catch (Throwable th) {
                        th = th;
                        reader = reader2;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e3) {
                                logger.info("", (Throwable) e3);
                            }
                        }
                        throw th;
                    }
                }
                String sb2 = sb.toString();
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e4) {
                        logger.info("", (Throwable) e4);
                    }
                }
                return sb2;
            } catch (Exception e5) {
                e = e5;
            }
        } catch (Throwable th2) {
            th = th2;
        }
    }

    public static String loadFile(Class basePath, String resource) {
        BufferedReader reader = null;
        try {
            try {
                StringBuilder sb = new StringBuilder();
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(basePath.getResourceAsStream(resource)));
                while (true) {
                    try {
                        String line = reader2.readLine();
                        if (line == null) {
                            break;
                        }
                        sb.append(line);
                    } catch (Exception e) {
                        e = e;
                        reader = reader2;
                        logger.error("", (Throwable) e);
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e2) {
                                logger.error("", (Throwable) e2);
                            }
                        }
                        return null;
                    } catch (Throwable th) {
                        th = th;
                        reader = reader2;
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e3) {
                                logger.error("", (Throwable) e3);
                            }
                        }
                        throw th;
                    }
                }
                String sb2 = sb.toString();
                if (reader2 != null) {
                    try {
                        reader2.close();
                    } catch (IOException e4) {
                        logger.error("", (Throwable) e4);
                    }
                }
                return sb2;
            } catch (Throwable th2) {
                th = th2;
            }
        } catch (Exception e5) {
            e = e5;
        }
    }
}