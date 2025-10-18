package service;

import javax.swing.*;
import java.io.*;

/**
 * Concrete class for opening files
 * Demonstrates: Inheritance, File I/O, Exception Handling
 */
public class OpenFileOperation implements FileOperation<String> {
    
    @Override
    public String execute(JFrame frame, String content) throws IOException {
        // Using JFileChooser for file selection
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open File");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files", "txt"));
        
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            return readFileContent(selectedFile);
        }
        return null;
    }
    
    /**
     * Read file content using File I/O
     */
    private String readFileContent(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        
        // Using try-with-resources for automatic resource management
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        return content.toString();
    }
    
    @Override
    public String getOperationName() {
        return "Open";
    }
}
