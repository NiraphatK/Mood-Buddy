package com.example.final_project;

public class MoodEntryClass {
    private String mood;
    private String note;
    private long timestamp;

    public MoodEntryClass() {
        // Default constructor required for Firebase
    }

    public MoodEntryClass(String mood, String note, long timestamp) {
        this.mood = mood;
        this.note = note;
        this.timestamp = timestamp;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
