package service;

public class ManualSave extends SaveStrategy {

    private final VersionManager<String> versionManager;

    public ManualSave(VersionManager<String> versionManager) {
        this.versionManager = versionManager;
    }

    @Override
    public void save(String content) {
        versionManager.addVersion(content);
    }
}
