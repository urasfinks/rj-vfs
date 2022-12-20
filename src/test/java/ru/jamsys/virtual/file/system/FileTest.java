package ru.jamsys.virtual.file.system;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

public class FileTest extends TestCase {
    @Test
    public void main() {
        File file = new File("/hello/world/1.txt", () -> null);
        Assert.assertEquals("Директория", "/hello/world", file.getFolder());
        Assert.assertEquals("Имя файла", "1", file.getFileName());
        Assert.assertEquals("Расширение", "txt", file.getExtension());
        Assert.assertEquals("Полный путь", "/hello/world/1.txt", file.getAbsolutePath());
    }

    @Test
    public void testGetString() throws UnsupportedEncodingException {
        File file = new File("/hello/world/1.txt", FileLoaderFactory.fromString("Hello world", "UTF-8"));
        Assert.assertEquals("#1", "Hello world", file.getString("UTF-8"));

        file = new File("/hello/world/1.txt", FileLoaderFactory.fromBase64("SGVsbG8gd29ybGQ=", "UTF-8"));
        Assert.assertEquals("#2", "Hello world", file.getString("UTF-8"));
    }

    public void testGetBase64() {
        File file = new File("/hello/world/1.txt", FileLoaderFactory.fromString("Hello world", "UTF-8"));
        Assert.assertEquals("#1", "SGVsbG8gd29ybGQ=", file.getBase64());
    }

    public void testFromFileSystem() throws UnsupportedEncodingException {
        File file = new File("/hello/world/1.txt", FileLoaderFactory.fromFileSystem("pom.xml"));
        Assert.assertTrue("#1", file.getString("UTF-8").length() > 0);
    }

}