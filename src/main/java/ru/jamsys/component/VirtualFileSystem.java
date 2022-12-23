package ru.jamsys.component;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.jamsys.AbstractCoreComponent;
import ru.jamsys.virtual.file.system.File;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Lazy
public class VirtualFileSystem extends AbstractCoreComponent {

    List<File> list = new CopyOnWriteArrayList<>();
    Map<String, File> map = new ConcurrentHashMap<>();

    public void add(File file) {
        list.add(file);
        map.put(file.getAbsolutePath(), file);
    }

    public void remove(File file) {
        list.remove(file);
    }

    public File get(String filePath) {
        return map.get(filePath);
    }

    @Override
    public void flushStatistic() {

    }
}
