package com.example.productapi;

import com.example.productapi.model.Product;
import com.example.productapi.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc

public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test // vérifier que le produit est bien créer
    void testCreateProduct() throws Exception {
        Product p = new Product("Clavier", 50);

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(p)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Clavier"));
    }

    @Test
    void testReadProduct() throws Exception {
        Product p = repository.save(new Product("Ecran", 150));

        mockMvc.perform(get("/products/" + p.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ecran"));
    }

    @Test // vérifier que le produit est bien dupliqué
    void testDuplicateProduct() throws Exception {
        // Étape 1 – Créer et enregistrer un produit original
        Product original = repository.save(new Product("Souris", 25));

        // Étape 2 – Appeler la route de duplication
        mockMvc.perform(post("/products/" + original.getId() + "/duplicate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                // on vérifie que l'id n'est pas identique à l'original
                .andExpect(jsonPath("$.id").value(org.hamcrest.Matchers.not(original.getId())))
                .andExpect(jsonPath("$.name").value("Souris"))
                .andExpect(jsonPath("$.price").value(25.0));
    }

    @Test // vérifier que le produit est bien supprimé
    void testDeleteProduct() throws Exception {
        // Étape 1 – Créer un produit à supprimer
        Product p = repository.save(new Product("Webcam", 80));

        // Étape 2 – Supprimer ce produit via l'API
        mockMvc.perform(delete("/products/" + p.getId()))
                .andExpect(status().isOk());

        // Étape 3 – Vérifier qu'il n'est plus en base
        boolean exists = repository.findById(p.getId()).isPresent();
        assertFalse(exists);
    }

    @Test // vérifier que le produit est modifié, que l'id reste identique
    void testUpdateProduct() throws Exception {
        // Étape 1 – Créer un produit en base
        Product original = repository.save(new Product("Microphone", 100));

        // Étape 2 – Créer un nouvel objet avec les nouvelles données
        Product updated = new Product("Microphone Pro", 150);

        // Étape 3 – Appeler l'API PUT pour modifier le produit
        mockMvc.perform(put("/products/" + original.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(original.getId()))
                .andExpect(jsonPath("$.name").value("Microphone Pro"))
                .andExpect(jsonPath("$.price").value(150.0));
    }

    @Test // vérifier que que l'api renvoie les produits enregistrés, les données sont
          // correctes et le tableau est un json.
    void testGetAllProducts() throws Exception {
        // Étape 1 – Préparer des données en base
        repository.save(new Product("Stylo", 2.5));
        repository.save(new Product("Carnet", 5.0));

        // Étape 2 – Appeler l’API GET
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Stylo"))
                .andExpect(jsonPath("$[1].name").value("Carnet"));
    }

    @Test // vérifier que le bundle fonctionne
    void testCreateBundle() throws Exception {
        // Étape 1 – Préparer des données en base
        Product p1 = repository.save(new Product("Feuilles blanches", 10.0));
        Product p2 = repository.save(new Product("Règle", 12.0));

        // Étape 2 – Créer la liste des IDs à envoyer
        List<Long> ids = List.of(p1.getId(), p2.getId());

        // Étape 3 – Appeler l’API POST pour créer le bundle
        mockMvc.perform(post("/products/bundle")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Feuilles blanches + Règle"))
                .andExpect(jsonPath("$.price").value(22.0));
    }

    @Test
void testCreateBundleWithInvalidId() throws Exception {
    // Créer un seul vrai produit
    Product p1 = repository.save(new Product("Agenda", 7.0));

    // Créer une liste avec un ID invalide
    List<Long> ids = List.of(p1.getId(), 9999L); // 9999 = inexistant

    mockMvc.perform(post("/products/bundle")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(ids)))
            .andExpect(status().isNotFound());
}


}
