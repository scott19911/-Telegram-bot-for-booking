package com.heroku.nwl.service;

import com.aspose.cells.Workbook;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heroku.nwl.constants.Constants;
import com.heroku.nwl.dto.ServiceCatalog;
import com.heroku.nwl.dto.WorkTimeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Service
public class FileHandler {
    public String fileHandler(String fileName, String path){
        String message ;
        switch (fileName){
            case Constants.FILE_SERVICES_CATALOG ->message = saveServiceCatalog(path);
            case Constants.FILE_SETTINGS -> message = saveWorkTime(path);
            default -> message = Constants.ERROR;
        }
        return message;
    }
    private String saveServiceCatalog(String path){
        ObjectMapper objectMapper = new ObjectMapper();
        List<ServiceCatalog> serviceCatalogs;
        try {
            serviceCatalogs = objectMapper.readValue(getJsonFromXLSX(path,2), new TypeReference<>(){});
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return Constants.ERROR;
        }
        return serviceCatalogs.get(0).toString();
    }
    private String saveWorkTime(String path){
        ObjectMapper objectMapper = new ObjectMapper();
        List<WorkTimeDto> workTimeDto;
        try {
            workTimeDto = objectMapper.readValue(getJsonFromXLSX(path, 3), new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return Constants.ERROR;
        }
        return workTimeDto.get(0).toString();
    }
    public String getJsonFromXLSX(String files,int lastColumn){
        Workbook workbook;
        try {
            workbook = new Workbook(files);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        List<Map<String, Object>> data = new ArrayList<>();
        int sheetIndex = 0; // Use the first sheet
        int firstRow = 1; // Skip the header row
        int lastRow = workbook.getWorksheets().get(sheetIndex).getCells().getMaxDataRow();
        for (int i = firstRow; i <= lastRow; i++) {
            Map<String, Object> rowData = new HashMap<>();
            for (int j = 0; j <= lastColumn; j++) {
                String columnName = workbook.getWorksheets().get(sheetIndex).getCells().get(0, j).getStringValue();
                Object cellValue = workbook.getWorksheets().get(sheetIndex).getCells().get(i, j).getValue();
                if(cellValue == null){
                    i = lastRow;
                } else {
                    rowData.put(columnName, cellValue);
                }
            }
            if (!rowData.isEmpty()) {
                data.add(rowData);
            }
        }
        ObjectMapper objectMapper = new ObjectMapper();
        String json;
        try {
            json = objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return json;
    }
}
