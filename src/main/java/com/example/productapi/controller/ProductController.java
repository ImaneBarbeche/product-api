package com.example.productapi.controller;

import com.example.productapi.model.Product;
import com.example.productapi.repository.ProductRepository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductRepository repository;

    public ProductController(ProductRepository repository) {
        this.repository = repository;

    }

    @GetMapping
    public List<Product> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return repository.findById(id).orElseThrow();
    }

    @PostMapping
    public Product create(@RequestBody Product product) {
        return repository.save(product);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @RequestBody Product product) {
        Product existing = repository.findById(id).orElseThrow();
        existing.setName(product.getName());
        existing.setPrice(product.getPrice());
        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    @PostMapping("/{id}/duplicate")
    public Product duplicate(@PathVariable Long id) {
        // Récupère le produit d'origine
        Product original = repository.findById(id).orElseThrow();
        // Crée une copie du produit (sans l'id)
        Product copy = new Product();
        copy.setName(original.getName());
        copy.setPrice(original.getPrice());

        // Sauvegarde et retourne la copie
        return repository.save(copy);
    }

    @PostMapping("/bundle")
    public Product createBundle(@RequestBody List<Long> sourceIds) {
        // 1. Récupérer tous les produits sources
        List<Product> sources = repository.findAllById(sourceIds);

        // 2. Vérifier que tous les IDs ont été trouvés
        if (sources.size() != sourceIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Un ou plusieurs produits n'ont pas été trouvés");
        }

        // 3. Vérifier l'absence de cycles (protection contre récursivité)
        checkForCycles(sources);

        // 4. Calculer le prix total
        double totalPrice = sources.stream()
                .mapToDouble(Product::getPrice)
                .sum();

        // 5. Générer le nom automatique du bundle
        String bundleName = sources.stream()
                .map(Product::getName)
                .reduce((a, b) -> a + " + " + b)
                .orElse("Bundle");

        // 6. Créer le nouveau produit bundle
        Product bundle = new Product();
        bundle.setName(bundleName);
        bundle.setPrice(totalPrice);
        bundle.setSources(sources);

        // 7. Sauvegarder et retourner
        return repository.save(bundle);
    }

    /**
     * Méthode pour vérifier l'absence de cycles dans les relations
     * Un produit ne peut pas contenir directement ou indirectement un produit qui
     * le contient
     */
    private void checkForCycles(List<Product> sourceProducts) {
        Set<Long> visited = new HashSet<>();
        Set<Long> recursionStack = new HashSet<>();

        for (Product product : sourceProducts) {
            if (hasCycle(product, visited, recursionStack)) {
                throw new RuntimeException("Cycle détecté dans la hiérarchie des produits");
            }
        }
    }

    /**
     * Algorithme de détection de cycles (DFS)
     */
    private boolean hasCycle(Product product, Set<Long> visited, Set<Long> recursionStack) {
        if (recursionStack.contains(product.getId())) {
            return true; // Cycle détecté
        }

        if (visited.contains(product.getId())) {
            return false; // Déjà visité sans cycle
        }

        visited.add(product.getId());
        recursionStack.add(product.getId());

        // Vérifier récursivement tous les produits sources
        for (Product source : product.getSources()) {
            if (hasCycle(source, visited, recursionStack)) {
                return true;
            }
        }

        recursionStack.remove(product.getId());
        return false;
    }
}
