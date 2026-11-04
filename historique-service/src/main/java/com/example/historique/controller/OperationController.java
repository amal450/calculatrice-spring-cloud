package com.example.historique.controller;

import com.example.historique.client.CalculClient;
import com.example.historique.model.Operation;
import com.example.historique.service.OperationService;

import feign.FeignException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/operations")
public class OperationController {

    private static final List<String> OPERATEURS =
            List.of("somme", "soustraction", "produit", "division");

    private final OperationService operationService;
    private final CalculClient calculClient;

    public OperationController(
            OperationService operationService,
            CalculClient calculClient) {
        this.operationService = operationService;
        this.calculClient = calculClient;
    }

    // Demande le calcul à calcul-service avec OpenFeign.
    // Renvoie une réponse d'erreur si le calcul échoue.
    private ResponseEntity<Object> calculer(Operation op) {

        if (op.getOperateur() == null
                || !OPERATEURS.contains(op.getOperateur())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur",
                            "Operateur inconnu : " + op.getOperateur()));
        }

        try {
            // Appel du microservice calcul-service via Feign
            String corps = calculClient.calculer(
                    op.getOperateur(),
                    op.getA(),
                    op.getB()
            );

            // Conversion du résultat String en Double
            op.setResultat(Double.parseDouble(corps));

            return null;

        } catch (FeignException.BadRequest e) {

            // Erreur HTTP 400 : par exemple, division par zéro
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", e.contentUTF8()));

        } catch (FeignException e) {

            // Autre erreur Feign : service indisponible
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("erreur", "calcul-service injoignable"));
        }
    }

    // POST /operations
    @PostMapping
    public ResponseEntity<?> creer(@RequestBody Operation op) {

        System.out.println("=== POST /operations reçu ===");
        System.out.println("operateur = " + op.getOperateur());
        System.out.println("a = " + op.getA());
        System.out.println("b = " + op.getB());

        ResponseEntity<Object> erreur = calculer(op);

        if (erreur != null) {
            System.out.println("Erreur pendant le calcul : " + erreur);
            return erreur;
        }

        Operation creee = operationService.ajouter(op);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creee.getId())
                .toUri();

        return ResponseEntity.created(location).body(creee);
    }

    // GET /operations
    @GetMapping
    public List<Operation> lister() {
        return operationService.lister();
    }

    // GET /operations/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> lire(@PathVariable("id") int id) {

        Operation op = operationService.lire(id);

        if (op == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(op);
    }

    // PUT /operations/{id}
    @PutMapping("/{id}")
    public ResponseEntity<?> modifier(
            @PathVariable("id") int id,
            @RequestBody Operation op) {

        if (!operationService.existe(id)) {
            return ResponseEntity.notFound().build();
        }

        ResponseEntity<Object> erreur = calculer(op);

        if (erreur != null) {
            return erreur;
        }

        operationService.remplacer(id, op);
        return ResponseEntity.ok(op);
    }

    // DELETE /operations/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimer(
            @PathVariable("id") int id) {

        Operation supprimee = operationService.supprimer(id);

        if (supprimee == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}