package com.heroku.nwl.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceCatalogRepository extends CrudRepository<ServiceCatalog,Long> {
    List<ServiceCatalog> findAll();
    List<ServiceCatalog> findAllByActiveService(boolean activeService);
    Optional<ServiceCatalog> findByName(String name);

}
