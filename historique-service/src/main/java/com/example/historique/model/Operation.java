package com.example.historique.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

// Lombok génère automatiquement les constructeurs,
// les getters, les setters et la méthode toString.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Operation {

    private Integer id;
    private String operateur;
    private int a;
    private int b;
    private Double resultat;
}