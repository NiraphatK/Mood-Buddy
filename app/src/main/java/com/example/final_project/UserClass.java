package com.example.final_project;

import java.util.HashMap;
import java.util.Map;

public class UserClass {
    private String email, username;
    private long createdAt;
    private Map<String, MoodEntryClass> moodEntries;

    public UserClass() {
        // Default constructor required for Firebase
    }

    public UserClass(String email, String username) {
        this.email = email;
        this.username = username;
        this.createdAt = System.currentTimeMillis();
        this.moodEntries = new HashMap<>();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, MoodEntryClass> getMoodEntries() {
        return moodEntries;
    }

    public void setMoodEntries(Map<String, MoodEntryClass> moodEntries) {
        this.moodEntries = moodEntries;
    }
}
