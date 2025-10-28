package app;

import model.Version;
import model.VersionException;
import service.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
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
        JMenu editMenu = new JMenu("Edit");
        JMenu options = new JMenu("Options");

        JMenuItem newFile = new JMenuItem("New");
        newFile.addActionListener(e -> handleNewFile());
        
        JMenuItem openFile = new JMenuItem("Open");
        openFile.addActionListener(e -> handleOpenFile());
        
        JMenuItem saveFile = new JMenuItem("Save");
        saveFile.addActionListener(e -> handleSaveFile());
        
        JMenuItem saveAsFile = new JMenuItem("Save As");
        saveAsFile.addActionListener(e -> handleSaveAsFile());

        JSeparator separator1 = new JSeparator();
        JMenuItem saveToDisk = new JMenuItem("Save All to Disk");
        saveToDisk.addActionListener(e -> saveToDisk());

        JMenuItem loadFromDisk = new JMenuItem("Load from Disk");
        loadFromDisk.addActionListener(e -> loadVersionsFromFile());

        JSeparator separator2 = new JSeparator();
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> handleExit());

        JMenuItem cutItem = new JMenuItem("Cut");
        cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        cutItem.addActionListener(e -> cutText());
        
        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        copyItem.addActionListener(e -> copyText());
        
        JMenuItem pasteItem = new JMenuItem("Paste");
        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        pasteItem.addActionListener(e -> pasteText());
        
        JMenuItem selectAllItem = new JMenuItem("Select All");
        selectAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        selectAllItem.addActionListener(e -> editor.selectAll());

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

        fileMenu.add(newFile);
        fileMenu.add(openFile);
        fileMenu.add(saveFile);
        fileMenu.add(saveAsFile);
        fileMenu.add(separator1);
        fileMenu.add(saveToDisk);
        fileMenu.add(loadFromDisk);
        fileMenu.add(separator2);
        fileMenu.add(exitItem);
        
        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.addSeparator();
        editMenu.add(selectAllItem);
        
        options.add(autoSaveToggle);
        options.add(setInterval);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
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
        KeyStroke saveKey = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        editor.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(saveKey, "save");
        editor.getActionMap().put("save", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveVersion();
            }
        });
        
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
        
        KeyStroke cutKey = KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        editor.getInputMap(JComponent.WHEN_FOCUSED).put(cutKey, "cut");
        editor.getActionMap().put("cut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cutText();
            }
        });
        
        KeyStroke copyKey = KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        editor.getInputMap(JComponent.WHEN_FOCUSED).put(copyKey, "copy");
        editor.getActionMap().put("copy", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyText();
            }
        });
        
        KeyStroke pasteKey = KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        editor.getInputMap(JComponent.WHEN_FOCUSED).put(pasteKey, "paste");
        editor.getActionMap().put("paste", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pasteText();
            }
        });
        
        KeyStroke selectAllKey = KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
        editor.getInputMap(JComponent.WHEN_FOCUSED).put(selectAllKey, "selectAll");
        editor.getActionMap().put("selectAll", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editor.selectAll();
            }
        });

        JComponent root = getRootPane();
        InputMap rim = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap ram = root.getActionMap();

        rim.put(cutKey, "globalCut");
        ram.put("globalCut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { cutText(); }
        });

        rim.put(copyKey, "globalCopy");
        ram.put("globalCopy", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { copyText(); }
        });

        rim.put(pasteKey, "globalPaste");
        ram.put("globalPaste", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { pasteText(); }
        });

        rim.put(selectAllKey, "globalSelectAll");
        ram.put("globalSelectAll", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { editor.selectAll(); }
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

    private void handleNewFile() {
        String result = fileOperationManager.executeOperation("New", this, editor.getText());
        if (result != null) {
            editor.setText(result);
            versionListModel.clear();
            versionManager.setAllVersions(new java.util.ArrayList<>());
            setTitle("Java TrackPad - New File");
        }
    }
    
    private void handleOpenFile() {
        String result = fileOperationManager.executeOperation("Open", this, editor.getText());
        if (result != null) {
            editor.setText(result);
            versionListModel.clear();
            versionManager.setAllVersions(new java.util.ArrayList<>());
            saveVersion();
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
                return;
            }
        }
        
        fileOperationManager.shutdown();
        System.exit(0);
    }

    private void cutText() {
        String selectedText = editor.getSelectedText();
        if (selectedText != null && !selectedText.isEmpty()) {
            copyToClipboard(selectedText);
            editor.replaceSelection("");
        }
    }

    private void copyText() {
        String selectedText = editor.getSelectedText();
        if (selectedText != null && !selectedText.isEmpty()) {
            copyToClipboard(selectedText);
        }
    }

    private void pasteText() {
        String clipboardText = getFromClipboard();
        if (clipboardText != null) {
            editor.replaceSelection(clipboardText);
        }
    }

    private void copyToClipboard(String text) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(text);
        clipboard.setContents(selection, null);
    }

    private String getFromClipboard() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            return (String) clipboard.getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TrackPad().setVisible(true));
    }
}
