package com.example.productapi.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private double price;

    @ManyToMany
    @JoinTable(name = "product_sources", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "source_id"))
    private List<Product> sources = new ArrayList<>();

    // Constructeur vide (requis)
    public Product() {
        // Constructeur vide requis par JPA
    }

    // Constructeur avec paramètres (optionnel mais pratique)
    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }

    // Getters et setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<Product> getSources() {
        return sources;
    }

    public void setSources(List<Product> sources) {
        this.sources = sources;
    }
}
