package service;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manager class for file operations using collections and multithreading
 * Demonstrates: Collections, Generics, Multithreading, Exception Handling
 */
public class FileOperationManager {
    
    // Using collections to store file operations
    private final Map<String, FileOperation<String>> operations;
    private final ExecutorService executor;
    private String currentFilePath;
    private boolean isModified;
    
    public FileOperationManager() {
        // Using HashMap collection to store operations
        this.operations = new HashMap<>();
        // Using thread pool for multithreading
        this.executor = Executors.newFixedThreadPool(2);
        this.currentFilePath = null;
        this.isModified = false;
        
        // Register file operations
        registerOperations();
    }
    
    /**
     * Register all file operations using collections
     */
    private void registerOperations() {
        operations.put("Open", new OpenFileOperation());
        operations.put("Save", new SaveFileOperation());
        operations.put("Save As", new SaveAsFileOperation());
        operations.put("New", new NewFileOperation());
    }
    
    /**
     * Execute file operation asynchronously using multithreading
     */
    public CompletableFuture<String> executeOperationAsync(String operationName, JFrame frame, String content) {
        FileOperation<String> operation = operations.get(operationName);
        if (operation == null) {
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                return operation.execute(frame, content);
            } catch (IOException e) {
                SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE));
                return null;
            }
        }, executor);
    }
    
    /**
     * Execute file operation synchronously
     */
    public String executeOperation(String operationName, JFrame frame, String content) {
        FileOperation<String> operation = operations.get(operationName);
        if (operation == null) {
            return null;
        }
        
        try {
            return operation.execute(frame, content);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error: " + e.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    
    // Getters and setters
    public String getCurrentFilePath() { return currentFilePath; }
    public void setCurrentFilePath(String path) { this.currentFilePath = path; }
    public boolean isModified() { return isModified; }
    public void setModified(boolean modified) { this.isModified = modified; }
    
    /**
     * Get all available operations using collections
     */
    public Set<String> getAvailableOperations() {
        return new HashSet<>(operations.keySet());
    }
    
    /**
     * Cleanup resources
     */
    public void shutdown() {
        executor.shutdown();
    }
}
