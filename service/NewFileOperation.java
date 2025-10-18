package service;

import javax.swing.*;
import java.io.IOException;

/**
 * Concrete class for creating new files
 * Demonstrates: Inheritance, Simple Operations
 */
public class NewFileOperation implements FileOperation<String> {
    
    @Override
    public String execute(JFrame frame, String content) throws IOException {
        // Check if there are unsaved changes
        if (content != null && !content.trim().isEmpty()) {
            int result = JOptionPane.showConfirmDialog(
                frame, 
                "Do you want to save current content before creating a new file?", 
                "New File", 
                JOptionPane.YES_NO_CANCEL_OPTION
            );
            
            if (result == JOptionPane.YES_OPTION) {
                // Trigger save operation
                SaveFileOperation saveOp = new SaveFileOperation();
                saveOp.execute(frame, content);
            } else if (result == JOptionPane.CANCEL_OPTION) {
                return null; // User cancelled
            }
        }
        
        // Return empty content for new file
        return "";
    }
    
    @Override
    public String getOperationName() {
        return "New";
    }
}
