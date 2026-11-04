package org.example.calcul.controller;
import org.example.calcul.service.Calculatrice;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.example.calcul.model.Resultat;
import org.springframework.beans.factory.annotation.Value;
@RestController
@RequestMapping("/calcul")
public class CalculController {
    private final Calculatrice calculService;
    @Value("${server.port}")
    private String port;
    // Injection par constructeur : Spring fournit l'objet Calculatrice
    public CalculController(Calculatrice calculService) {
        this.calculService = calculService;
    }
    @GetMapping(value = "/somme", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> somme(@RequestParam("a") int a,
                                        @RequestParam("b") int b) {

        System.out.println("Requête traitée par l'instance : " + port);

        int result = calculService.somme(a, b);
        return ResponseEntity.ok(String.valueOf(result));
    }
    // Calcul de la soustraction
    @GetMapping(value = "/soustraction", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> soustraction(
            @RequestParam("a") int a,
            @RequestParam("b") int b) {

        int result = calculService.soustraction(a, b);
        return ResponseEntity.ok(String.valueOf(result));
    }

    // Calcul du produit
    @GetMapping(value = "/produit", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> produit(
            @RequestParam("a") int a,
            @RequestParam("b") int b) {

        int result = calculService.produit(a, b);
        return ResponseEntity.ok(String.valueOf(result));
    }
    @GetMapping(value = "/division", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> division(@RequestParam("a") int a,
                                           @RequestParam("b") int b) {
        if (b == 0) {
            return ResponseEntity.badRequest()
                    .body("Erreur : division par 0 impossible");
        }
        double result = calculService.division(a, b);
        return ResponseEntity.ok(String.valueOf(result));
    }
    @GetMapping(value = "/produit/{a}/{b}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> produitChemin(@PathVariable("a") int a,
                                                @PathVariable("b") int b) {
        int result = calculService.produit(a, b);
        return ResponseEntity.ok(String.valueOf(result));
    }
    @GetMapping("/somme/detail")
    public Resultat sommeDetail(
            @RequestParam("a") int a,
            @RequestParam("b") int b) {

        return new Resultat(
                "somme",
                a,
                b,
                calculService.somme(a, b)
        );
    }
    @GetMapping("/division/detail")
    public ResponseEntity<Resultat> divisionDetail(
            @RequestParam("a") int a,
            @RequestParam("b") int b) {

        if (b == 0) {
            return ResponseEntity.badRequest().build();
        }

        double resultat = calculService.division(a, b);

        Resultat response =
                new Resultat("division", a, b, resultat);

        return ResponseEntity.ok(response);
    }
}