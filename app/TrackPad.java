package app;

import model.Version;
import model.VersionException;
import service.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TrackPad extends JFrame {

    private final JTextArea editor;
    private final DefaultListModel<String> versionListModel;
    private final JList<String> versionList;
    private final VersionManager<String> versionManager;
    private final SaveStrategy manualSaver;
    private final AutosaveWorker autosaveWorker;
    private final FileStorageService fileStorageService;
    private final FileOperationManager fileOperationManager;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String SAVE_FILE = "versions.ser";

    public TrackPad() {
        super("Java TrackPad");

        versionManager = new VersionManager<>();
        manualSaver = new ManualSave(versionManager);
        autosaveWorker = new AutosaveWorker(this::saveVersion, 5);
        fileStorageService = new FileStorageService(SAVE_FILE);
        fileOperationManager = new FileOperationManager();

        editor = new JTextArea();
        versionListModel = new DefaultListModel<>();
        versionList = new JList<>(versionListModel);
        versionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(editor), new JScrollPane(versionList));
        splitPane.setResizeWeight(0.8);
        add(splitPane, BorderLayout.CENTER);

        setupMenu();
        setupListeners();
        setupShortcuts();

        loadVersionsFromFile();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }

    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu options = new JMenu("Options");

        // New basic file operations
        JMenuItem newFile = new JMenuItem("New");
        newFile.addActionListener(e -> handleNewFile());
        
        JMenuItem openFile = new JMenuItem("Open");
        openFile.addActionListener(e -> handleOpenFile());
        
        JMenuItem saveFile = new JMenuItem("Save");
        saveFile.addActionListener(e -> handleSaveFile());
        
        JMenuItem saveAsFile = new JMenuItem("Save As");
        saveAsFile.addActionListener(e -> handleSaveAsFile());

        // Separator
        JSeparator separator1 = new JSeparator();
        
        // Existing version management operations
        JMenuItem saveToDisk = new JMenuItem("Save All to Disk");
        saveToDisk.addActionListener(e -> saveToDisk());

        JMenuItem loadFromDisk = new JMenuItem("Load from Disk");
        loadFromDisk.addActionListener(e -> loadVersionsFromFile());

        // Separator
        JSeparator separator2 = new JSeparator();
        
        // Exit option
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> handleExit());

        // Options menu items
        JCheckBoxMenuItem autoSaveToggle = new JCheckBoxMenuItem("Enable Autosave");
        JMenuItem setInterval = new JMenuItem("Set Autosave Interval");

        autoSaveToggle.addActionListener(e -> {
            if (autoSaveToggle.isSelected()) autosaveWorker.enableAutosave(5);
            else autosaveWorker.disableAutosave();
        });

        setInterval.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter interval (minutes):", 5);
            if (input != null) {
                try {
                    int min = Integer.parseInt(input);
                    autosaveWorker.enableAutosave(min);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid number", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Add items to File menu
        fileMenu.add(newFile);
        fileMenu.add(openFile);
        fileMenu.add(saveFile);
        fileMenu.add(saveAsFile);
        fileMenu.add(separator1);
        fileMenu.add(saveToDisk);
        fileMenu.add(loadFromDisk);
        fileMenu.add(separator2);
        fileMenu.add(exitItem);
        
        // Add items to Options menu
        options.add(autoSaveToggle);
        options.add(setInterval);

        menuBar.add(fileMenu);
        menuBar.add(options);
        setJMenuBar(menuBar);
    }

    private void setupListeners() {
        versionList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                try {
                    String content = versionManager.getVersionContent(versionList.getSelectedIndex());
                    editor.setText(content);
                } catch (VersionException ignored) {}
            }
        });
    }

    private void setupShortcuts() {
        // Version save shortcut (Ctrl+S)
        KeyStroke saveKey = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        editor.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(saveKey, "save");
        editor.getActionMap().put("save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveVersion();
            }
        });
        
        // File operation shortcuts
        KeyStroke newKey = KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        editor.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(newKey, "new");
        editor.getActionMap().put("new", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleNewFile();
            }
        });
        
        KeyStroke openKey = KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        editor.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(openKey, "open");
        editor.getActionMap().put("open", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleOpenFile();
            }
        });
        
        KeyStroke saveFileKey = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_DOWN_MASK);
        editor.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(saveFileKey, "saveFile");
        editor.getActionMap().put("saveFile", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSaveFile();
            }
        });
    }

    private void saveVersion() {
        manualSaver.save(editor.getText());
        int index = versionManager.getVersionCount() - 1;
        versionListModel.addElement("v" + (index + 1) + " - " + LocalDateTime.now().format(TIME_FORMAT));
        versionList.setSelectedIndex(index);
    }

    private void saveToDisk() {
        try {
            fileStorageService.saveToFile(versionManager.getAllVersions());
            JOptionPane.showMessageDialog(this, "Versions saved to disk.");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving to disk: " + e.getMessage());
        }
    }

    private void loadVersionsFromFile() {
        try {
            List<Version<String>> loaded = fileStorageService.loadFromFile();
            if (loaded != null) {
                versionManager.setAllVersions(loaded);
                versionListModel.clear();
                for (Version<String> v : loaded) {
                    versionListModel.addElement("v" + v.getNumber() + " - " + v.getTimestamp().format(TIME_FORMAT));
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No saved versions found.");
        }
    }

    // New file operation handlers
    private void handleNewFile() {
        String result = fileOperationManager.executeOperation("New", this, editor.getText());
        if (result != null) {
            editor.setText(result);
            // Clear version list for new file
            versionListModel.clear();
            versionManager.setAllVersions(new java.util.ArrayList<>());
            setTitle("Java TrackPad - New File");
        }
    }
    
    private void handleOpenFile() {
        String result = fileOperationManager.executeOperation("Open", this, editor.getText());
        if (result != null) {
            editor.setText(result);
            // Clear version list and add the opened content as first version
            versionListModel.clear();
            versionManager.setAllVersions(new java.util.ArrayList<>());
            saveVersion(); // Save the opened content as first version
            setTitle("Java TrackPad - " + fileOperationManager.getCurrentFilePath());
        }
    }
    
    private void handleSaveFile() {
        String result = fileOperationManager.executeOperation("Save", this, editor.getText());
        if (result != null) {
            fileOperationManager.setCurrentFilePath(result);
            setTitle("Java TrackPad - " + result);
            JOptionPane.showMessageDialog(this, "File saved successfully!", "Save", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void handleSaveAsFile() {
        String result = fileOperationManager.executeOperation("Save As", this, editor.getText());
        if (result != null) {
            fileOperationManager.setCurrentFilePath(result);
            setTitle("Java TrackPad - " + result);
            JOptionPane.showMessageDialog(this, "File saved successfully!", "Save As", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void handleExit() {
        // Check for unsaved changes
        if (fileOperationManager.isModified()) {
            int result = JOptionPane.showConfirmDialog(
                this, 
                "Do you want to save before exiting?", 
                "Exit", 
                JOptionPane.YES_NO_CANCEL_OPTION
            );
            
            if (result == JOptionPane.YES_OPTION) {
                handleSaveFile();
            } else if (result == JOptionPane.CANCEL_OPTION) {
                return; // Don't exit
            }
        }
        
        // Cleanup resources
        fileOperationManager.shutdown();
        System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TrackPad().setVisible(true));
    }
}
