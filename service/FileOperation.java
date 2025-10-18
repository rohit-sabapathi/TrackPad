package service;

import javax.swing.JFrame;
import java.io.IOException;

/**
 * Interface for file operations using generics
 * Demonstrates interface concept in Java
 */
public interface FileOperation<T> {
    /**
     * Execute the file operation
     * @param frame the main application frame
     * @param content the content to operate on
     * @return result of the operation
     * @throws IOException if file operation fails
     */
    T execute(JFrame frame, String content) throws IOException;
    
    /**
     * Get the operation name for display
     * @return operation name
     */
    String getOperationName();
}
