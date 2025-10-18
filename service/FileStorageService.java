package service;

import model.Version;
import java.io.*;
import java.util.List;

public class FileStorageService {

    private final File file;

    public FileStorageService(String filePath) {
        this.file = new File(filePath);
    }

    public <T> void saveToFile(List<Version<T>> versions) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(versions);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> List<Version<T>> loadFromFile() throws IOException, ClassNotFoundException {
        if (!file.exists()) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<Version<T>>) ois.readObject();
        }
    }
}
