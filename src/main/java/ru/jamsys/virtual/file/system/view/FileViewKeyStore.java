package ru.jamsys.virtual.file.system.view;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import ru.jamsys.component.Security;
import ru.jamsys.virtual.file.system.File;

import java.io.InputStream;
import java.security.KeyStore;

public class FileViewKeyStore implements FileView {

    private File file = null;
    private static final String JKS = "jks";

    @Getter
    private volatile KeyStore keyStore = null;

    @Autowired
    public void setSecurity(Security security) {
        this.security = security;
    }

    private Security security;

    @Override
    public void set(File file) {
        this.file = file;
    }

    @Override
    public void createCache() {
        String pass = security.get(file.getAbsolutePath());
        try (InputStream stream = file.getInputStream()) {
            keyStore = KeyStore.getInstance(JKS);
            keyStore.load(stream, pass.toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
