package ru.jamsys.virtual.file.system;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import ru.jamsys.App;
import ru.jamsys.UtilFile;
import ru.jamsys.component.Security;
import ru.jamsys.virtual.file.system.view.FileViewKeyStore;

import java.io.UnsupportedEncodingException;

public class FileTest {

    @BeforeAll
    static void beforeAll() {
        String[] args = new String[]{};
        App.context = SpringApplication.run(App.class, args);
    }

    @Test
    public void testMain() {
        File file = new File("/hello/world/1.txt", () -> null);
        Assertions.assertEquals("/hello/world", file.getFolder(), "Директория");
        Assertions.assertEquals("1", file.getFileName(), "Имя файла");
        Assertions.assertEquals("txt", file.getExtension(), "Расширение");
        Assertions.assertEquals("/hello/world/1.txt", file.getAbsolutePath(), "Полный путь");

        file = new File("/1.txt", () -> null);
        Assertions.assertEquals("/1.txt", file.getAbsolutePath(), "Полный путь");

        file = new File("1.txt", () -> null);
        Assertions.assertEquals("/1.txt", file.getAbsolutePath(), "Полный путь");
    }

    @Test
    public void testGetString() throws UnsupportedEncodingException {
        File file = new File("/hello/world/1.txt", FileLoaderFactory.fromString("Hello world", "UTF-8"));
        Assertions.assertEquals("Hello world", file.getString("UTF-8"), "#1");

        file = new File("/hello/world/1.txt", FileLoaderFactory.fromBase64("SGVsbG8gd29ybGQ=", "UTF-8"));
        Assertions.assertEquals("Hello world", file.getString("UTF-8"), "#2");
    }

    @Test
    public void testGetBase64() {
        File file = new File("/hello/world/1.txt", FileLoaderFactory.fromString("Hello world", "UTF-8"));
        Assertions.assertEquals("SGVsbG8gd29ybGQ=", file.getBase64(), "#1");
    }

    @Test
    public void testFromFileSystem() throws UnsupportedEncodingException {
        File file = new File("/hello/world/1.txt", FileLoaderFactory.fromFileSystem("pom.xml"));
        Assertions.assertTrue(file.getString("UTF-8").length() > 0, "#1");
    }

    @Test
    public void testKeyStore() throws Exception {
        Security security = App.context.getBean(Security.class);
        security.add("test", "12345");
        File file = new File("/test.p12", FileLoaderFactory.createKeyStore("one.jks", "test"));
        file.setSaver(FileSaverFactory.writeFile("one.jks"));
        file.setProp("securityKey", "test");
        FileViewKeyStore view = file.getView(FileViewKeyStore.class);
        view.setSecret("h1", "Hello world");

        Assertions.assertEquals("Hello world", view.getSecret("h1"), "#1");

        UtilFile.remove("one.jks");


    }

}