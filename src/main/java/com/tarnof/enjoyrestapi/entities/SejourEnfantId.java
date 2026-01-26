package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SejourEnfantId implements Serializable {
    @Column(name = "sejour_id")
    private Integer sejourId;    
    @Column(name = "enfant_id")
    private Integer enfantId;
}
