package service;

public interface AutoSavable {
    void enableAutosave(int intervalMinutes);
    void disableAutosave();
    boolean isAutosaveEnabled();
}
