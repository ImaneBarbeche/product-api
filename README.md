# Product API
Une API RESTful développée en Java avec Spring Boot pour gérer des produits, leur duplication et la création de bundles.

## 🏗️ Structure du projet

- `controller/` : expose les routes HTTP
- `repository/` : interface JPA pour interagir avec la base H2
- `model/` : entité `Product` avec relations (ex : bundles)
- `test/` : tests unitaires avec MockMvc


## ⚙️ Choix techniques

Java 17
Spring Boot 3.5
Base H2 en mémoire
Tests avec JUnit 5 et MockMvc
Swagger via springdoc-openapi

## 📋 Règles métier

Duplication de produit par ID
Création de bundle à partir d’une liste de produits
Protection contre les cycles (un produit ne peut pas contenir un bundle qui le contient déjà)

## 💻 Jeu de requêtes CURL

✅ Créer un produit
curl -X POST http://localhost:8080/products \
 -H "Content-Type: application/json" \
 -d '{"name": "Stylo", "price": 2.5}'

🔁 Dupliquer un produit
curl -X POST http://localhost:8080/products/1/duplicate

📦 Créer un bundle
curl -X POST http://localhost:8080/products/bundle \
 -H "Content-Type: application/json" \
 -d '[1, 2]'

## 🚀 Lancer le projet
mvn spring-boot:run

L’interface Swagger sera accessible à :
http://localhost:8080/swagger-ui

## Auteur 
Imane Barbeche