# TP3 — REST : Microservices « Calculatrice » avec Spring Boot

**Faculté des Sciences de Sfax — Année universitaire 2026–2027**  
**Filière :** MPSRCC1  
**Matière :** Architecture MicroServices  
**Technologies :** Java 17, Spring Boot, Maven, REST, HTTP, JSON, Postman

## 1. Présentation du projet

Ce TP consiste à transformer une application REST de calculatrice en une architecture composée de deux microservices indépendants :

- **calcul-service** : réalise les opérations mathématiques.
- **historique-service** : appelle le service de calcul, enregistre les opérations et expose les fonctionnalités CRUD.

### Architecture

| Microservice | Port | Responsabilité |
|---|---:|---|
| calcul-service | 8081 | Calculs mathématiques |
| historique-service | 8082 | Historique des opérations |

L'historique communique avec le service de calcul par HTTP. Les opérations sont conservées en mémoire dans cette version du TP : elles disparaissent lorsque le service d'historique redémarre.

## 2. Lancement du projet

### Prérequis

- JDK 17 ou version compatible avec le projet.
- Maven ou Maven Wrapper.
- IntelliJ IDEA.
- Postman.

### Configuration de calcul-service

Dans `application.properties` :

```properties
spring.application.name=calcul-service
server.port=8081
```

### Configuration de historique-service

Dans `application.properties` :

```properties
spring.application.name=historique-service
server.port=8082
calcul.service.url=http://localhost:8081
```

### Démarrage

1. Démarrer `calcul-service` sur le port 8081.
2. Démarrer `historique-service` sur le port 8082.
3. Vérifier que les deux applications sont démarrées sans erreur.
4. Envoyer les requêtes de test depuis Postman.

Les deux applications doivent rester démarrées pour que les opérations nécessitant un calcul puissent fonctionner.

## 3. Tests du service de calcul

| Méthode | URL | Résultat attendu |
|---|---|---|
| GET | `http://localhost:8081/calcul/somme?a=2&b=3` | `5` |
| GET | `http://localhost:8081/calcul/soustraction?a=10&b=4` | `6` |
| GET | `http://localhost:8081/calcul/produit?a=6&b=7` | `42` |
| GET | `http://localhost:8081/calcul/division?a=7&b=2` | `3.5` |
| GET | `http://localhost:8081/calcul/produit/6/7` | `42` |
| GET | `http://localhost:8081/calcul/somme/detail?a=2&b=3` | Objet JSON contenant l'opération et le résultat |
| GET | `http://localhost:8081/calcul/division?a=7&b=0` | Erreur HTTP 400 |

## 4. Tests du service d'historique

### Créer une opération

**Méthode :** POST  
**URL :** `http://localhost:8082/operations`  
**Postman :** Body → raw → JSON

```json
{
  "operateur": "somme",
  "a": 8,
  "b": 2
}
```

Résultat attendu : HTTP **201 Created**, avec un identifiant généré, les opérandes et le résultat `10.0`.

### Lister les opérations

```http
GET http://localhost:8082/operations
```

### Lire une opération

```http
GET http://localhost:8082/operations/1
```

### Modifier une opération

**Méthode :** PUT  
**URL :** `http://localhost:8082/operations/1`

```json
{
  "operateur": "produit",
  "a": 8,
  "b": 2
}
```

Résultat attendu : HTTP **200 OK**, avec le résultat recalculé `16.0`.

### Supprimer une opération

```http
DELETE http://localhost:8082/operations/1
```

Résultat attendu : HTTP **204 No Content**. Une nouvelle lecture de cette opération renvoie HTTP 404.

---

# 5. Réponses aux questions du TP

## A. Architecture microservices

### A.4 — Analyse de l'architecture

**1. Quel est l'avantage et l'inconvénient d'une communication HTTP entre deux microservices ?**

L'avantage est que les deux services sont indépendants : ils peuvent être développés, déployés et mis à l'échelle séparément. L'inconvénient est que la communication réseau peut introduire de la latence et des erreurs de connexion.

**2. Que se passe-t-il si calcul-service est arrêté ?**

Les requêtes directes vers calcul-service échouent. En revanche, les opérations déjà enregistrées restent consultables par historique-service, car les données sont stockées dans ce service. Les créations et modifications nécessitant un nouveau calcul échouent tant que calcul-service est indisponible.

**3. Peut-on démarrer deux applications sur le même port ?**

Non, pas sur la même adresse réseau et le même port simultanément. Le système ne permet généralement pas aux deux serveurs d'écouter le même couple adresse-port. Il faut utiliser des ports différents ou une configuration réseau spécifique.

**4. Quel code HTTP utiliser lorsque calcul-service est indisponible ?**

Le code **503 Service Unavailable** est adapté : le service demandé ne peut temporairement pas traiter la requête parce qu'une dépendance est indisponible.

## B. Création et configuration des microservices

Les deux applications sont des projets Spring Boot distincts. Chaque projet possède son propre fichier `pom.xml`, sa classe principale annotée `@SpringBootApplication` et sa configuration.

Cette séparation permet de démarrer, tester et déployer les applications indépendamment.

## C. Contrôleurs REST et endpoints

### C.1 — Pourquoi l'ancienne URL `/RestWebApp/api` a-t-elle disparu ?

Dans le TP précédent, cette partie de l'URL provenait de la configuration de l'application JAX-RS déployée dans Tomcat. Dans Spring Boot, le serveur embarqué utilise ici le chemin de contexte `/` par défaut. Les endpoints sont donc accessibles directement à partir de leurs mappings, par exemple `/calcul/somme`.

### C.2 — Que se passe-t-il si on envoie POST vers un endpoint GET ?

Spring renvoie normalement **405 Method Not Allowed**, car l'endpoint accepte GET, mais pas POST. La méthode HTTP doit correspondre à celle déclarée dans le contrôleur.

## D. Paramètres et gestion des erreurs

### D.1 — Que se passe-t-il si un paramètre obligatoire est absent ou invalide ?

Spring renvoie généralement HTTP **400 Bad Request** si un paramètre obligatoire manque ou si sa valeur ne peut pas être convertie vers le type demandé, par exemple un entier.

Ce comportement est approprié pour une API REST, car la requête invalide est rejetée plutôt que d'utiliser silencieusement une valeur incorrecte.

### D.2 — Quel contenu peut contenir la réponse d'erreur Spring ?

La réponse d'erreur par défaut peut être un objet JSON contenant notamment :

- `timestamp` : date et heure de l'erreur ;
- `status` : code HTTP ;
- `error` : libellé de l'erreur ;
- `path` : chemin de la requête.

Les champs exacts peuvent varier selon la version de Spring Boot et la configuration de l'application.

### D.3 — Équivalences entre JAX-RS et Spring MVC

| JAX-RS | Spring MVC |
|---|---|
| `@Path` | `@RequestMapping` |
| `@GET` | `@GetMapping` |
| `@POST` | `@PostMapping` |
| `@PUT` | `@PutMapping` |
| `@DELETE` | `@DeleteMapping` |
| `@QueryParam` | `@RequestParam` |
| `@PathParam` | `@PathVariable` |
| `@Consumes` | `consumes` et/ou `@RequestBody` selon le rôle |
| `@Produces` | `produces` et/ou négociation de contenu |
| `Response` | `ResponseEntity` |

`@RequestBody` sert à convertir le corps de la requête HTTP, par exemple un objet JSON, en objet Java.

## E. JSON et négociation de contenu

### E.1 — L'ordre des propriétés JSON est-il important ?

Non. Les deux objets suivants représentent les mêmes données :

```json
{
  "operateur": "somme",
  "a": 8,
  "b": 2
}
```

```json
{
  "b": 2,
  "a": 8,
  "operateur": "somme"
}
```

L'ordre des propriétés d'un objet JSON n'a pas d'importance pour la désérialisation habituelle avec Jackson dans Spring Boot.

### E.2 — Que se passe-t-il si le client demande du XML alors que le service fournit du JSON ?

Si le client envoie l'en-tête `Accept: application/xml` et que l'endpoint ne peut produire que du JSON, Spring peut renvoyer **406 Not Acceptable**. Le format demandé par le client n'est pas disponible pour cette ressource.

## F. Communication entre microservices et CRUD

### F.1 — Pourquoi peut-on consulter l'historique lorsque calcul-service est arrêté ?

La consultation de l'historique lit les opérations déjà enregistrées dans historique-service. Elle n'a pas besoin de recalculer les résultats. En revanche, créer ou modifier une opération nécessite un appel à calcul-service.

Cette isolation montre l'intérêt de séparer les responsabilités entre microservices.

### F.2 — Que se passe-t-il si l'appel HTTP vers calcul-service échoue sans gestion d'erreur ?

Une exception de connexion non traitée peut provoquer une réponse générique HTTP 500 Internal Server Error. Pour rendre l'API plus claire, il est préférable d'intercepter l'erreur de connexion et de renvoyer **503 Service Unavailable**.

### F.3 à F.5 — Création et lecture des opérations

- **POST `/operations`** : demande un calcul, enregistre l'opération et renvoie HTTP 201 Created en cas de réussite.
- **GET `/operations`** : renvoie la liste des opérations enregistrées.
- **GET `/operations/{id}`** : renvoie l'opération correspondante ou HTTP 404 Not Found si l'identifiant n'existe pas.

Le résultat est recalculé par calcul-service lors d'une création ou d'une modification, et non lors de la simple consultation de l'historique.

### F.6 — Résultats des tests Postman

| Test | Résultat attendu |
|---|---|
| Créer une somme | 201 Created |
| Créer une division valide | 201 Created |
| Lister les opérations | 200 OK |
| Lire une opération existante | 200 OK |
| Lire une opération inexistante | 404 Not Found |
| Créer une division par zéro | 400 Bad Request |
| Utiliser un opérateur inconnu | 400 Bad Request |
| Appeler calcul-service arrêté lors d'un calcul | 503 Service Unavailable si l'erreur est gérée |

### F.7 — Que faire si calcul-service est arrêté ?

Il faut démarrer ou rétablir calcul-service sur le port 8081, puis réessayer la requête. Le service d'historique doit traiter l'indisponibilité de sa dépendance et renvoyer une erreur explicite plutôt que d'enregistrer une opération sans résultat valide.

### F.8 — Quels codes HTTP pour la modification ?

| Situation | Code HTTP |
|---|---|
| Identifiant inexistant | 404 Not Found |
| Modification réussie | 200 OK |
| Calcul impossible ou opérateur invalide | 400 Bad Request |
| Service de calcul indisponible | 503 Service Unavailable |

### F.9 — Quels codes HTTP pour la suppression ?

| Situation | Code HTTP |
|---|---|
| Suppression réussie | 204 No Content |
| Identifiant inexistant | 404 Not Found |

Une réponse 204 ne contient normalement pas de corps. Une deuxième suppression du même identifiant peut renvoyer 404 si l'application vérifie que l'opération existe.

## 6. Synthèse générale

### 6.1 — Différences observées entre les deux TP

Le TP précédent utilisait JAX-RS et un déploiement dans Tomcat. Le TP3 utilise Spring Boot, son serveur embarqué, des annotations Spring MVC et un modèle d'architecture à deux microservices.

Les principales différences sont visibles dans la configuration des routes, le démarrage des applications, les ports, la gestion des erreurs et les appels HTTP entre services.

### 6.2 — Équivalences principales entre JAX-RS et Spring Boot

| JAX-RS | Spring Boot |
|---|---|
| `@Path("/operations")` | `@RequestMapping("/operations")` |
| `@GET` | `@GetMapping` |
| `@POST` | `@PostMapping` |
| `@PUT` | `@PutMapping` |
| `@DELETE` | `@DeleteMapping` |
| `@QueryParam("a")` | `@RequestParam("a")` |
| `@PathParam("id")` | `@PathVariable("id")` |
| `Response` | `ResponseEntity` |
| `@ApplicationPath` | Configuration de l'application et du contexte Spring Boot |

### 6.3 — Comment lancer le projet sans IntelliJ ?

Depuis le dossier de chaque projet, construire le fichier JAR avec Maven :

```bash
mvn clean package
```

Ou, sous Windows, si Maven n'est pas installé mais que le wrapper est présent :

```powershell
.\mvnw.cmd clean package
```

Lancer ensuite chaque JAR dans un terminal différent :

```bash
java -jar target/nom-du-jar.jar
```

Le nom réel du fichier JAR dépend de la configuration Maven. Vérifier le contenu du dossier `target` avant de lancer la commande.

### 6.4 — Un service web est-il nécessairement un microservice ?

Non. Un service web expose des fonctionnalités à travers une interface, souvent HTTP. Un microservice est un service conçu pour être relativement autonome, indépendant dans son déploiement et limité à une responsabilité précise.

Un service web n'est donc pas automatiquement un microservice. Dans ce TP, chaque application Spring Boot est un service autonome avec son propre port et son propre rôle.

### 6.5 — Pourquoi une URL codée en dur limite-t-elle la répartition de charge ?

Si historique-service appelle toujours `http://localhost:8081`, il contacte une seule adresse fixe. Il ne découvre pas automatiquement plusieurs instances de calcul-service.

Pour répartir les appels entre plusieurs instances, on peut utiliser un mécanisme de découverte de services et un répartiteur de charge. Ces mécanismes sont des sujets d'architecture plus avancée.

### 6.6 — Le stockage de l'historique contredit-il le principe de statelessness ?

Pas nécessairement. Le principe stateless de HTTP signifie que chaque requête doit contenir les informations nécessaires à son traitement et ne dépend pas d'une session conversationnelle implicite du client.

Le service peut tout de même stocker des données métier, comme les opérations de calcul. Ici, l'historique est conservé côté serveur en mémoire ; ce stockage ne constitue pas, à lui seul, une session HTTP.

### 6.7 — POST et PUT sont-ils idempotents ?

- **POST** n'est généralement pas idempotent : envoyer plusieurs fois une demande de création peut créer plusieurs opérations.
- **PUT** est conçu pour être idempotent : répéter la même demande de remplacement sur la même ressource doit conduire au même état final.

Ces propriétés proviennent de la sémantique HTTP et s'appliquent aussi bien au TP précédent qu'au TP3.

## 7. Conclusion

Ce TP permet de comprendre la création de microservices REST avec Spring Boot, l'utilisation des méthodes HTTP, le transfert de données JSON, la gestion des codes d'erreur et la communication entre services.

La séparation entre calcul-service et historique-service améliore la modularité et permet de mieux comprendre les conséquences de l'indisponibilité d'une dépendance.

**Remarque :** les résultats indiqués dans ce document sont les résultats attendus. Les tests réels doivent être exécutés dans Postman pour confirmer le bon fonctionnement de l'implémentation.
