package ru.jamsys.virtual.file.system;

import lombok.Getter;
import lombok.Setter;
import ru.jamsys.ConsumerThrowing;
import ru.jamsys.SupplierThrowing;
import ru.jamsys.UtilBase64;
import ru.jamsys.virtual.file.system.view.FileView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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

    @Setter
    private ConsumerThrowing<byte[]> saver = null;

    private volatile byte[] fileData;

    Map<Class<? extends FileView>, FileView> view = new HashMap<>();

    Map<String, Object> prop = new HashMap<>();

    public void setProp(String key, Object value) {
        prop.put(key, value);
    }

    @SuppressWarnings({"unused", "unchecked"})
    public <T> T getProp(String key, T def) {
        return prop.containsKey(key) ? (T) prop.get(key) : def;
    }

    @SuppressWarnings({"unused", "unchecked"})
    public <T> T getProp(String key) {
        return (T) prop.get(key);
    }

    @Getter
    private String absolutePath = null;

    private void setView(Class<? extends FileView> t) throws Exception {
        FileView e = t.getDeclaredConstructor().newInstance();
        e.set(this);
        view.putIfAbsent(t, e);
    }

    @SuppressWarnings("unchecked")
    public <T extends FileView> T getView(Class<T> t) throws Exception {
        if (!view.containsKey(t)) {
            setView(t);
        }
        init();
        return (T) view.get(t);
    }

    @SuppressWarnings("unchecked")
    public <T extends FileView> T getView(Class<T> t, Object... props) throws Exception {
        for (int i = 0; i < props.length; i += 2) {
            setProp(props[i].toString(), props[i + 1]);
        }
        if (!view.containsKey(t)) {
            setView(t);
        }
        init();
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
        ArrayList<String> items = new ArrayList<>(Arrays.asList(path.trim().split("/")));
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
        if (folder.equals("/")) {
            this.absolutePath = "/" + fileName + "." + extension;
        } else {
            this.absolutePath = folder + "/" + fileName + "." + extension;
        }
    }

    @SuppressWarnings("unused")
    public void reset() {
        fileData = null;
    }

    private void restoreTimeRemoveCache() {
        if (cacheTimeMillis > 0) {
            setTimeRemoveCache(System.currentTimeMillis() + cacheTimeMillis);
        }
    }

    private void init() {
        if (fileData == null) {
            try {
                fileData = loader.get();
                Set<Class<? extends FileView>> classes = view.keySet();
                for (Class<? extends FileView> c : classes) {
                    view.get(c).createCache();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        restoreTimeRemoveCache();
    }

    public byte[] getBytes() {
        init();
        return fileData;
    }

    public String getString(String charset) throws UnsupportedEncodingException {
        return new String(getBytes(), charset);
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(getBytes());
    }

    public String getBase64() {
        return UtilBase64.base64Encode(getBytes(), true);
    }

    public void save(byte[] data) throws Exception {
        if (saver == null) {
            throw new Exception("Consumer saver not found. File: " + getAbsolutePath());
        }
        fileData = data;
        saver.accept(data);
        //reload(); //Сохранение не должно вызывать перезагрузку, так как в памяти должны быть внесены изменения, это только для Supplier загрузчика
    }

}
