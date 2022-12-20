package ru.jamsys.virtual.file.system;

import lombok.Getter;
import lombok.Setter;
import ru.jamsys.SupplierThrowing;
import ru.jamsys.UtilBase64;
import ru.jamsys.virtual.file.system.view.FileView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class File {

    @Getter
    protected String folder; //Абосолюный путь папки
    @Getter
    protected String fileName; //Имя файла
    @Getter
    protected String extension; //Расширение файла (думаю сделать поиск по расширениям)

    protected int cacheTimeMillis = -1; //Время существования данных в памяти. -1 безсмертие

    @Setter
    protected volatile long timeRemoveCache; //Время когда надо удалить кеш из памяти

    protected SupplierThrowing<byte[]> loader = () -> null;
    private volatile byte[] fileData;

    Map<Class<FileView>, FileView> view = new HashMap<>();

    @Getter
    private String absolutePath = null;

    public void setView(Class<FileView> t) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        FileView e = t.getDeclaredConstructor().newInstance();
        e.set(this);
        view.put(t, e);
    }

    public <T extends FileView> T getView(Class<T> t) {
        return (T) view.get(t);
    }

    public File(String path, SupplierThrowing<byte[]> loader) {
        init(path, loader);
    }

    public File(String path, SupplierThrowing<byte[]> loader, int cacheTimeMillis) {
        this.cacheTimeMillis = cacheTimeMillis;
        init(path, loader);
    }

    private void init(String path, SupplierThrowing<byte[]> loader) {
        this.loader = loader;
        restoreTimeRemoveCache();
        parsePath(path);
    }

    private void parsePath(String path) {
        ArrayList<String> items = new ArrayList(Arrays.asList(path.trim().split("/")));
        while (true) {
            String s = items.get(0);
            if (s == null || s.equals("") || s.equals("..")) {
                items.remove(0);
            } else {
                break;
            }
        }
        String name = items.remove(items.size() - 1);
        String[] split = name.split("\\.");
        this.extension = split[split.length - 1].trim();
        this.fileName = name.substring(0, name.length() - this.extension.length() - 1);
        this.folder = "/" + String.join("/", items);
        this.absolutePath = folder + "/" + fileName + "." + extension;
    }

    public void reload() {
        fileData = null;
    }

    private void restoreTimeRemoveCache() {
        if (cacheTimeMillis > 0) {
            setTimeRemoveCache(System.currentTimeMillis() + cacheTimeMillis);
        }
    }

    public byte[] get() {
        if (fileData == null) {
            try {
                fileData = loader.get();
                Set<Class<FileView>> classes = view.keySet();
                for (Class<FileView> c : classes) {
                    view.get(c).createCache();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        restoreTimeRemoveCache();
        return fileData;
    }

    public String getString(String charset) throws UnsupportedEncodingException {
        return new String(get(), charset);
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(get());
    }

    public String getBase64() {
        return UtilBase64.base64Encode(get(), true);
    }

}
