package com.heroku.nwl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ServiceCatalog {
    @JsonProperty(value = "name",required = true)
    private String serviceName;
    @JsonProperty(value = "price",required = true)
    private double price;
    @JsonProperty(value = "average_time",required = true)
    private int time;
}
