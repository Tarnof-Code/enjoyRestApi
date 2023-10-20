package com.tarnof.enjoyrestapi.utilisateur;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


public enum Role {
    DIRECTEUR,
    ANIMATEUR,
    ASSISTANT_SANITAIRE
}

