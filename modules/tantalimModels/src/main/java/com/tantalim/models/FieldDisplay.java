package com.tantalim.models;

public enum FieldDisplay {
    Text, Checkbox, Select, Textarea, Date, DateTime;

    public String lower() {
        return this.toString().toLowerCase();
    }
}
