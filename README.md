# Réponses aux questions du TP4 – Spring Cloud

## A.4 – Questions de conception

**1. Comment Eureka permet-il aux microservices de se découvrir automatiquement ?**

Eureka joue le rôle d'un annuaire de services. Lorsqu'un microservice démarre, il s'enregistre auprès du serveur Eureka en indiquant son nom et son adresse. Les autres services peuvent ensuite consulter cet annuaire pour trouver les services dont ils ont besoin, sans avoir à connaître leurs adresses IP ou leurs ports à l'avance.

**2. Pourquoi utiliser une API Gateway ? Quels sont ses avantages et ses inconvénients ?**

L'API Gateway représente un point d'entrée unique pour accéder aux différents microservices. Elle permet de centraliser le routage des requêtes et d'éviter aux clients de communiquer directement avec chaque service.

Cependant, elle peut devenir un point de défaillance unique si elle tombe en panne. Il est donc important de prévoir des mécanismes pour assurer sa disponibilité.

**3. Avec deux instances du service Calcul et six requêtes, comment les requêtes sont-elles réparties avec Round Robin ?**

Avec l'algorithme Round Robin, les requêtes sont réparties alternativement entre les deux instances. Pour six requêtes, chaque instance reçoit normalement trois requêtes.

Le client obtient le même résultat, quel que soit le serveur qui traite la demande.

## B.1 – Configuration des ports

**Pourquoi chaque microservice utilise-t-il un port différent ?**

Chaque application doit utiliser un port différent pour éviter les conflits. Dans notre projet, Eureka utilise le port `8761`, le service Calcul utilise le port `8081` et le service Historique utilise le port `8082`.

Ainsi, les différentes applications peuvent fonctionner simultanément sur la même machine.

## C.1 – Enregistrement de plusieurs instances

**Pourquoi les deux instances du service Calcul ont-elles le même nom ?**

Les deux instances utilisent le même nom de service `CALCUL-SERVICE`, car elles réalisent les mêmes fonctionnalités.

Cependant, elles possèdent des identifiants et des ports différents, ce qui permet à Eureka de les distinguer. Par exemple, la première instance fonctionne sur le port `8081` et la deuxième sur le port `8083`.

## C.2 – Lancement de la deuxième instance

**Que se passe-t-il si on lance la deuxième instance sans préciser un nouveau port ?**

Si aucun nouveau port n'est précisé, la deuxième instance essaie d'utiliser le port `8081`, déjà occupé par la première instance.

Le démarrage échoue donc à cause du conflit de port. Pour résoudre ce problème, il faut lancer la deuxième instance avec l'option `--server.port=8083`.

## D.1 – Répartition des requêtes

**Comment fonctionne le Load Balancer dans notre projet ?**

Le Load Balancer utilise l'algorithme Round Robin pour répartir les requêtes entre les instances disponibles du service Calcul.

Par exemple, les requêtes peuvent être envoyées successivement vers l'instance `8081`, puis vers l'instance `8083`. Cette répartition permet d'utiliser les différentes instances au lieu de solliciter toujours la même.

## D.2 – Gestion d'un nom de service incorrect

**Que se passe-t-il lorsqu'on utilise un nom de service incorrect ?**

Si le nom du service est incorrect, par exemple `CALCUL-SERVIC` au lieu de `CALCUL-SERVICE`, Eureka ne trouve aucune instance correspondante.

La communication échoue alors et le client peut recevoir une erreur `503 Service Unavailable`. Pour corriger ce problème, il faut vérifier et utiliser le nom exact enregistré dans Eureka.

## E.1 – Gestion des en-têtes Forwarded

**Pourquoi faut-il configurer `server.forward-headers-strategy=framework` ?**

Lorsqu'une requête passe par l'API Gateway, les informations concernant l'adresse et le protocole d'origine peuvent être transmises dans des en-têtes `X-Forwarded-*`.

La configuration `server.forward-headers-strategy=framework` permet à Spring de prendre en compte ces informations. Cela aide notamment à générer des URL correctes, en utilisant l'adresse publique de la Gateway plutôt que l'adresse interne du microservice.

## E.2 – Routage statique et routage dynamique

**Quelle est la différence entre le routage statique et le routage dynamique ?**

Le routage statique consiste à définir manuellement les routes dans le fichier de configuration de la Gateway. Lorsqu'un nouveau microservice est ajouté, il faut modifier cette configuration.

Le routage dynamique utilise Eureka pour découvrir automatiquement les services disponibles. La Gateway peut alors créer les routes à partir des services enregistrés.

Le routage dynamique est donc plus flexible et facilite l'ajout de nouveaux microservices.

## E.3 – Découverte de la Gateway elle-même

**Pourquoi la Gateway peut-elle apparaître dans les routes générées automatiquement ?**

La Gateway est également enregistrée auprès d'Eureka. Elle peut donc être découverte comme les autres microservices.

Lors de la génération automatique des routes, elle peut apparaître dans la liste des services disponibles. Il est préférable de filtrer sa propre route pour éviter des accès inutiles ou des problèmes de routage.

---

# Questions de synthèse

## 1. Quels sont les principaux concepts étudiés dans ce TP ?

Ce TP permet de mettre en pratique plusieurs concepts de Spring Cloud :

- **Eureka :** enregistrement et découverte des microservices.
- **OpenFeign :** communication entre les microservices.
- **LoadBalancer :** répartition des requêtes entre plusieurs instances.
- **Spring Cloud Gateway :** gestion du routage et point d'entrée unique.
- **Forwarded Headers :** gestion des informations transmises par la Gateway.

## 2. Décrire le parcours d'une requête POST pour enregistrer une opération dans l'historique.

Lorsqu'un client envoie une requête POST à l'adresse de la Gateway sur le port `8888`, celle-ci transmet la demande au service Historique.

Le service Historique reçoit les données de l'opération, puis utilise OpenFeign pour appeler le service Calcul. Grâce à Eureka et au LoadBalancer, une instance disponible de Calcul est sélectionnée.

Le service Calcul effectue l'opération et retourne le résultat au service Historique. Ce dernier enregistre ensuite l'opération et renvoie la réponse au client en passant par la Gateway.

## 3. Quels programmes utilisent le répartiteur de charge Spring Cloud LoadBalancer ?

Dans notre architecture, le service Historique utilise le LoadBalancer pour choisir une instance du service `CALCUL-SERVICE` lors d'un appel Feign.

La Gateway utilise également le LoadBalancer pour acheminer les requêtes vers les instances des microservices enregistrées auprès d'Eureka.

## 4. Quels composants Spring Cloud sont utilisés dans chaque microservice ?

- **Eureka Server :** fournit l'annuaire des services.
- **Calcul Service :** s'enregistre auprès d'Eureka pour être découvert.
- **Historique Service :** utilise Eureka et OpenFeign pour communiquer avec Calcul.
- **Gateway Service :** utilise Eureka et Spring Cloud Gateway pour découvrir les services et acheminer les requêtes.

## 5. Pourquoi utiliser Eureka et OpenFeign au lieu d'une adresse IP fixe ?

Avec une adresse IP fixe, le service appelant doit connaître directement l'adresse et le port du service distant. Cela rend l'application moins flexible, notamment lorsqu'une instance change d'adresse ou lorsqu'on ajoute plusieurs instances.

Eureka permet de retrouver automatiquement les services grâce à leur nom. OpenFeign simplifie les appels entre microservices, tandis que le LoadBalancer permet de répartir les requêtes entre les instances disponibles.

## 6. Quel problème peut apparaître lorsque plusieurs instances du service Historique sont lancées ?

Si chaque instance du service Historique conserve ses données uniquement en mémoire, chaque instance possède son propre historique.

Par conséquent, une opération enregistrée dans une instance ne sera pas forcément visible depuis l'autre. Pour résoudre ce problème, il faut utiliser une base de données partagée par toutes les instances.

## 7. Comment lancer les cinq programmes de notre architecture ?

Il faut d'abord construire les projets avec Maven :

```bash
mvn clean package -DskipTests
```

Ensuite, lancer les cinq programmes dans des terminaux différents, dans l'ordre suivant :

**1. Eureka Server**

```bash
java -jar eureka-service/target/eureka-service-0.0.1-SNAPSHOT.jar
```

**2. Première instance de Calcul**

```bash
java -jar calcul-service/target/calcul-service-0.0.1-SNAPSHOT.jar
```

**3. Deuxième instance de Calcul**

```bash
java -jar calcul-service/target/calcul-service-0.0.1-SNAPSHOT.jar --server.port=8083
```

**4. Historique Service**

```bash
java -jar historique-service/target/historique-service-0.0.1-SNAPSHOT.jar
```

**5. Gateway Service**

```bash
java -jar gateway-service/target/gateway-service-0.0.1-SNAPSHOT.jar
```

*Remarque : les noms des fichiers JAR peuvent varier selon la version définie dans les fichiers `pom.xml`.*

## 8. Quelle est la différence entre `@GetMapping` dans un Controller et dans une interface Feign ?

Dans un Controller, `@GetMapping` permet de définir une adresse HTTP qui reçoit les requêtes envoyées par le client.

Dans une interface Feign, `@GetMapping` définit l'adresse HTTP utilisée pour appeler un autre microservice.

La même annotation est donc utilisée, mais son rôle dépend de son emplacement.

## 9. Pourquoi les microservices restent-ils indépendants même s'ils sont dans le même projet IntelliJ ?

Même si les microservices sont regroupés dans un seul projet IntelliJ, chacun possède sa propre classe principale, sa configuration et son port.

Chaque microservice peut être compilé, exécuté et arrêté indépendamment des autres. Le projet Maven parent permet simplement de regrouper et d'organiser les différents modules.

## 10. Pourquoi Swagger doit-il prendre en compte les en-têtes Forwarded ?

Lorsque Swagger est consulté à travers la Gateway, il doit utiliser l'adresse publique de cette dernière pour générer les URL des requêtes.

Sans la prise en compte des en-têtes Forwarded, Swagger risque d'utiliser directement le port interne du microservice. La configuration `server.forward-headers-strategy=framework` permet de corriger ce comportement.

## 11. Pourquoi les méthodes générées par Lombok ne sont-elles pas visibles directement dans le code source ?

Lombok génère automatiquement certaines méthodes pendant la compilation, comme les getters, les setters et les constructeurs.

Ces méthodes sont présentes dans les fichiers `.class` générés par Java, même si elles ne sont pas écrites directement dans le code source. Lombok est principalement nécessaire pendant la compilation et n'a pas besoin d'être utilisé à l'exécution.
