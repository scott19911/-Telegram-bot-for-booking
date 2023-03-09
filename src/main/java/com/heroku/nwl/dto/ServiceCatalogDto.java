package com.heroku.nwl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.heroku.nwl.model.ServiceCatalog;
import lombok.Data;

@Data
public class ServiceCatalogDto {
    @JsonProperty(value = "name")
    private String serviceName;
    @JsonProperty(value = "price")
    private double price;
    @JsonProperty(value = "average_time")
    private int time;
    public ServiceCatalog getServiceCatalog(){
        ServiceCatalog serviceCatalog = new ServiceCatalog();
        serviceCatalog.setName(serviceName);
        serviceCatalog.setPrice(price);
        serviceCatalog.setAverageTime(time);
        return serviceCatalog;
    }
}
