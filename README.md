# EGoPass - Documentation

## Description
EGoPass est une application de gestion de laissez-passer électroniques pour voyageurs. Elle permet de créer des réservations, de générer des laissez-passer électroniques (eGoPass) avec QR codes, et de produire des documents PDF.

## Prérequis
- JDK 17 ou supérieur
- Maven 3.6+
- Docker
- IntelliJ IDEA (recommandé)
- Git

## Installation et démarrage

### 1. Cloner le projet
```bash
git clone https://github.com/Adzaba3/egopass-service.git
cd egopass-service
```

### 2. Lancer la base de données PostgreSQL avec Docker
Exécutez la commande suivante pour démarrer un conteneur PostgreSQL :
```bash
docker run --name postgres-container -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=egopassdb -p 5432:5432 -d postgres
```

Cette commande crée un conteneur Docker avec les paramètres suivants :
- Nom du conteneur : `postgres-container`
- Utilisateur PostgreSQL : `postgres`
- Mot de passe PostgreSQL : `postgres`
- Nom de la base de données : `egopassdb`
- Port : `5432` (mappage du port du conteneur vers le port local)

Vous pouvez vérifier que le conteneur est en cours d'exécution avec :
```bash
docker ps
```

### 3. Configurer l'application

Ouvrez le projet dans IntelliJ IDEA :
1. Lancez IntelliJ IDEA
2. Sélectionnez "Open" ou "Import Project"
3. Naviguez jusqu'au dossier du projet et sélectionnez-le

Configurez le fichier `application.yml` situé dans `src/main/resources/` pour qu'il contienne les informations de connexion à la base de données :

```properties
# Configuration de la base de données
datasource:
  url: jdbc:postgresql://localhost:5432/egopassdb
  username: postgres
  password: postgres
  driver-class-name: org.postgresql.Driver


# Configuration JPA/Hibernate
jpa:
  hibernate:
    ddl-auto: update
  show-sql: true
  properties:
    hibernate:
      format_sql: true
  dialect: org.hibernate.dialect.PostgreSQLDialect

# Configuration du serveur
server.port=8080

```

### 4. Compiler et exécuter l'application

#### Option 1 : Via IntelliJ IDEA
1. Ouvrez la classe principale (généralement `EgopassApplication.java`)
2. Cliquez sur le bouton ▶️ (Run) à côté de la déclaration de classe

#### Option 2 : Via Maven
```bash
mvn clean install
mvn spring-boot:run
```

L'application sera accessible à l'adresse [http://localhost:8080](http://localhost:8080).

## Structure du projet

Le projet suit une architecture en couches standard :

- **Controller** : Gestion des requêtes HTTP
- **Service** : Logique métier
- **Repository** : Accès aux données
- **Model** : Entités de la base de données
- **DTO** : Objets de transfert de données
- **Mapper** : Conversion entre entités et DTOs
- **Exception** : Gestion des erreurs personnalisées

## Fonctionnalités principales

- Création de réservations
- Génération d'eGoPass avec QR codes
- Génération de documents PDF
- Gestion des utilisateurs

## API REST


Pour les détails sur les API disponibles, consultez la documentation Swagger disponible à l'adresse [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) une fois l'application démarrée.



## Dépannage

### Problèmes courants :

1. **Connexion à la base de données refusée** : Vérifiez que le conteneur Docker est en cours d'exécution.
2. **Port déjà utilisé** : Modifiez le port dans le fichier `application.yml` ou arrêtez l'application qui utilise le port.
3. **Échec de compilation** : Assurez-vous que toutes les dépendances sont correctement installées avec `mvn clean install`.




