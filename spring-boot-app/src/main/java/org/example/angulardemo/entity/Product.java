package org.example.angulardemo.entity;

import jakarta.persistence.Entity;

import jakarta.persistence.*;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String extId;
    private String name;
    private String description;

    public Long getId() {
        return id;
    }

    public String getExtId() {
        return extId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExtId(String extId) {
        this.extId = extId;
    }
}