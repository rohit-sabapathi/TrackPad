package service;

import javax.swing.*;
import java.io.*;

/**
 * Concrete class for saving files
 * Demonstrates: Inheritance, File I/O, Exception Handling
 */
public class SaveFileOperation implements FileOperation<String> {
    
    @Override
    public String execute(JFrame frame, String content) throws IOException {
        // Check if we have a current file path
        if (content == null || content.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No content to save", "Save Error", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        
        // For now, use Save As functionality since we don't have current file tracking
        // This will be enhanced when integrated with the main app
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save File");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files", "txt"));
        
        int result = fileChooser.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            writeFileContent(selectedFile, content);
            return selectedFile.getAbsolutePath();
        }
        return null;
    }
    
    /**
     * Write content to file using File I/O
     */
    private void writeFileContent(File file, String content) throws IOException {
        // Using try-with-resources for automatic resource management
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        }
    }
    
    @Override
    public String getOperationName() {
        return "Save";
    }
}
