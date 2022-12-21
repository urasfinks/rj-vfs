package ru.jamsys.virtual.file.system;

import ru.jamsys.ConsumerThrowing;
import ru.jamsys.FileWriteOptions;
import ru.jamsys.UtilFile;

public class FileSaverFactory {

    public static ConsumerThrowing<byte[]> writeFile(String path){
        return (data)-> {
            UtilFile.writeBytes(path, data, FileWriteOptions.CREATE_OR_REPLACE);
        };
    }

}
