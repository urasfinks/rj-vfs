package ru.jamsys.virtual.file.system;

import ru.jamsys.SupplierThrowing;
import ru.jamsys.UtilBase64;
import ru.jamsys.UtilFile;

import java.nio.charset.Charset;


public class FileLoaderFactory {

    public static SupplierThrowing<byte[]> fromFileSystem(String path) {
        return () -> UtilFile.readBytes(path);
    }

    public static SupplierThrowing<byte[]> fromBase64(String base64decoded, String charset) {
        return () -> UtilBase64.base64DecodeResultBytes(base64decoded, charset);
    }

    public static SupplierThrowing<byte[]> fromString(String data, String charset) {
        return () -> data.getBytes(Charset.forName(charset));
    }

}
