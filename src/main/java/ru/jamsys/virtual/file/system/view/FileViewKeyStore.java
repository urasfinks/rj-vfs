package ru.jamsys.virtual.file.system.view;

import lombok.Getter;
import ru.jamsys.App;
import ru.jamsys.component.Security;
import ru.jamsys.virtual.file.system.File;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.net.ssl.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class FileViewKeyStore implements FileView {

    private String typeKeyStorage;
    private String securityKey;
    private Security security;
    private File file = null;
    private final Map<String, SSLSocketFactory> sslSocketFactory = new ConcurrentHashMap<>();

    @Getter
    private KeyManager[] keyManagers;

    @Getter
    private TrustManager[] trustManagers;

    @Getter
    private volatile KeyStore keyStore = null;

    @Override
    public void set(File file) {
        this.file = file;
        security = App.context.getBean(Security.class);
        typeKeyStorage = file.getProp("type", "JCEKS");
        securityKey = file.getProp("securityKey", file.getAbsolutePath());
    }

    @Override
    public void createCache() {
        char[] pass = security.get(securityKey);
        if (pass == null) {
            new Exception("Пароль не найден по ключу: " + file.getAbsolutePath()).printStackTrace();
        } else {
            try (InputStream stream = file.getInputStream()) {
                keyStore = KeyStore.getInstance(typeKeyStorage);
                keyStore.load(stream, pass);
            } catch (Exception e) {
                keyStore = null;
                e.printStackTrace();
            }
        }
        sslSocketFactory.clear();
    }

    public SSLSocketFactory getSslSocketFactory(String sslContextType) {
        if (keyStore == null) {
            return null;
        }
        String typeManager = "SunX509";
        if (keyManagers == null) {
            try {
                char[] pass = security.get(securityKey);
                KeyManagerFactory kmf = KeyManagerFactory.getInstance(typeManager);
                kmf.init(keyStore, pass);
                keyManagers = kmf.getKeyManagers();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (trustManagers == null) {
            try {
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(typeManager);
                tmf.init(keyStore);
                trustManagers = tmf.getTrustManagers();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!sslSocketFactory.containsKey(sslContextType)) {
            try {
                SSLContext ssl = SSLContext.getInstance(sslContextType);
                ssl.init(keyManagers, trustManagers, new SecureRandom());
                sslSocketFactory.put(sslContextType, ssl.getSocketFactory());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sslSocketFactory.get(sslContextType);
    }

}
