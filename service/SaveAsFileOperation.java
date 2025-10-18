package service;

import javax.swing.*;
import java.io.*;

/**
 * Concrete class for Save As functionality
 * Demonstrates: Inheritance, File I/O, Exception Handling
 */
public class SaveAsFileOperation implements FileOperation<String> {
    
    @Override
    public String execute(JFrame frame, String content) throws IOException {
        if (content == null || content.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No content to save", "Save As Error", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save As");
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
        return "Save As";
    }
}
