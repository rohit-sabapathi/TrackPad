package service;

import model.Version;
import model.VersionException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VersionManager<T> implements Versionable<T> {
    private final List<Version<T>> versions = new ArrayList<>();
    private int nextVersionNumber = 1;

    @Override
    public void addVersion(T content) {
        versions.add(new Version<>(nextVersionNumber++, LocalDateTime.now(), content));
    }

    @Override
    public T getVersionContent(int index) throws VersionException {
        if (index < 0 || index >= versions.size()) {
            throw new VersionException("Invalid version index: " + index);
        }
        return versions.get(index).getContent();
    }

    @Override
    public int getVersionCount() {
        return versions.size();
    }

    @Override
    public List<Version<T>> getAllVersions() {
        return new ArrayList<>(versions);
    }

    public void setAllVersions(List<Version<T>> loaded) {
        versions.clear();
        versions.addAll(loaded);
        nextVersionNumber = versions.size() + 1;
    }
}
