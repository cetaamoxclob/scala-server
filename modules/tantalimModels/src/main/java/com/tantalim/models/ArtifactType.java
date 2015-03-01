package com.tantalim.models;

public enum ArtifactType {
    Menu, Page, Model, Table;

    public String getDirectory() {
        return super.toString().toLowerCase() + "s";
    }
}
