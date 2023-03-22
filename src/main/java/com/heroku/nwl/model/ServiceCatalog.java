package com.heroku.nwl.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.util.List;

@Data
@Entity(name = "service_catalog")
public class ServiceCatalog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long serviceId;
    @Column(unique = true)
    private String name;
    private double price;
    @Column(name = "active_service", columnDefinition = "TINYINT(1)")
    private boolean activeService;
    private int averageTime;
}
