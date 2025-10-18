package model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Version<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int number;
    private final LocalDateTime timestamp;
    private final T content;

    public Version(int number, LocalDateTime timestamp, T content) {
        this.number = number;
        this.timestamp = timestamp;
        this.content = content;
    }

    public int getNumber() { return number; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public T getContent() { return content; }
}
