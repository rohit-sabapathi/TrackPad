package service;

import model.Version;
import model.VersionException;
import java.util.List;

public interface Versionable<T> {
    void addVersion(T content);
    T getVersionContent(int index) throws VersionException;
    int getVersionCount();
    List<Version<T>> getAllVersions();
}
