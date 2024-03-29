package com.heroku.nwl.service.implementation;

import com.aspose.cells.SaveFormat;
import com.aspose.cells.Workbook;
import com.aspose.cells.Worksheet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heroku.nwl.config.CustomBotException;
import com.heroku.nwl.constants.Constants;
import com.heroku.nwl.constants.ErrorMessage;
import com.heroku.nwl.dto.ServiceCatalogDto;
import com.heroku.nwl.dto.WorkSettingsDto;
import com.heroku.nwl.model.ServiceCatalog;
import com.heroku.nwl.model.ServiceCatalogRepository;
import com.heroku.nwl.model.WorkSettings;
import com.heroku.nwl.model.WorkSettingsRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.heroku.nwl.constants.Constants.A1;
import static com.heroku.nwl.constants.Constants.AVERAGE_TIME;
import static com.heroku.nwl.constants.Constants.A_ROW;
import static com.heroku.nwl.constants.Constants.B1;
import static com.heroku.nwl.constants.Constants.B_ROW;
import static com.heroku.nwl.constants.Constants.C1;
import static com.heroku.nwl.constants.Constants.C_ROW;
import static com.heroku.nwl.constants.Constants.NAME;
import static com.heroku.nwl.constants.Constants.PRICE;
import static com.heroku.nwl.constants.Constants.SERVICE_CATALOG_COLUMNS;
import static com.heroku.nwl.constants.Constants.SERVICE_CATALOG_FILE;
import static com.heroku.nwl.constants.Constants.SERVICE_CATALOG_SHEET_INDEX;
import static com.heroku.nwl.constants.Constants.WORK_SETTINGS_COLUMNS;
import static com.heroku.nwl.constants.Constants.WORK_SETTINGS_SHEET_INDEX;

@Slf4j
@Service
@AllArgsConstructor
public class FileHandler {
    private final ServiceCatalogRepository serviceCatalogRepository;
    private final WorkSettingsRepository workSettingsRepository;

    public String fileHandler(String fileName, String path) throws CustomBotException {
        String message;
        if (fileName.equals(Constants.FILE_SETTINGS)) {
            message = saveSettings(path);
        } else {
            log.info(ErrorMessage.ERROR_WRONG_FILE_NAME + fileName);
            throw new CustomBotException(ErrorMessage.ERROR_WRONG_FILE_NAME);
        }
        return message;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean saveServiceCatalog(String path) {
        List<ServiceCatalog> serviceCatalog = getServiceCatalogFromDto(getServiceCatalogFromXlsxFile(path));
        if (serviceCatalog == null) {
            return false;
        }
        List<ServiceCatalog> currentCatalog = serviceCatalogRepository.findAll();
        currentCatalog.removeAll(serviceCatalog);
        for (ServiceCatalog service : currentCatalog
        ) {
            service.setActiveService(false);
            serviceCatalogRepository.save(service);
        }
        for (ServiceCatalog service:serviceCatalog
             ) {
            ServiceCatalog findService = serviceCatalogRepository.findByName(service.getName()).orElse(null);
            if (findService == null){
                service.setActiveService(true);
                serviceCatalogRepository.save(service);
            }else {
                findService.setPrice(service.getPrice());
                findService.setAverageTime(service.getAverageTime());
                findService.setActiveService(true);
                serviceCatalogRepository.save(findService);
            }
        }
        return true;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String saveSettings(String path) throws CustomBotException {
        WorkSettings workSettings = getWorkSettingsFromXlsxFile(path);
        String result = Constants.SUCCESS_FILE_SETTINGS;
        if(workSettings == null){
            throw new CustomBotException(Constants.ERROR_FILE);
        }
        workSettings.setId(1L);
        workSettingsRepository.save(workSettings);
        if (!saveServiceCatalog(path)){
            throw new CustomBotException(Constants.ERROR_FILE);
        }
        return result;
    }
    private List<ServiceCatalogDto> getServiceCatalogFromXlsxFile(String path) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<ServiceCatalogDto> serviceCatalogs;
        try {
            serviceCatalogs = objectMapper.readValue(
                    getJsonFromXLSX(path, SERVICE_CATALOG_COLUMNS, SERVICE_CATALOG_SHEET_INDEX),
                    new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return null;
        }
        if(!checkServiceFields(serviceCatalogs)){
            return null;
        }
        return serviceCatalogs;
    }

    private WorkSettings getWorkSettingsFromXlsxFile(String path) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<WorkSettingsDto> workSettingsDtos;
        try {
            workSettingsDtos = objectMapper.readValue(
                    getJsonFromXLSX(path, WORK_SETTINGS_COLUMNS,WORK_SETTINGS_SHEET_INDEX),
                    new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return null;
        }
        if (!checkSettingFields(workSettingsDtos.get(0).getWorkTimeSettings())){
            return null;
        }
        return workSettingsDtos.get(0).getWorkTimeSettings();
    }

    public String getJsonFromXLSX (String files,int lastColumn, int sheetIndex) throws JsonProcessingException {
        Workbook workbook;
        try {
            workbook = new Workbook(files);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        List<Map<String, String>> data = new ArrayList<>();
        int firstRow = 1;
        int lastRow = workbook.getWorksheets().get(sheetIndex).getCells().getMaxDataRow();
        for (int i = firstRow; i <= lastRow; i++) {
            Map<String, String> rowData = new HashMap<>();
            for (int j = 0; j <= lastColumn; j++) {
                String columnName = workbook.getWorksheets().get(sheetIndex).getCells().get(0, j).getStringValue();
                String cellValue = workbook.getWorksheets().get(sheetIndex).getCells().get(i, j).getStringValue();
                if(j == 0 && cellValue.isBlank()){
                    i = lastRow + 1;
                    j = lastColumn + 1;
                    rowData = null;
                } else {
                    rowData.put(columnName, cellValue);
                }
            }
            if (rowData != null) {
                data.add(rowData);
            }
        }
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(data);
    }

    public File getServiceCatalogFile() {
        Workbook workbook = new Workbook();
        Worksheet worksheet = workbook.getWorksheets().get(0);
        List<ServiceCatalog> serviceCatalogList = serviceCatalogRepository.findAllByActiveService(true);
        worksheet.getCells().get(A1).setValue(NAME);
        worksheet.getCells().get(B1).setValue(PRICE);
        worksheet.getCells().get(C1).setValue(AVERAGE_TIME);
        int row = 2;
        for (ServiceCatalog service : serviceCatalogList) {
            worksheet.getCells().get(A_ROW + row).setValue(service.getName());
            worksheet.getCells().get(B_ROW + row).setValue(service.getPrice());
            worksheet.getCells().get(C_ROW + row).setValue(service.getAverageTime());
            row++;
        }
        try {
            workbook.save(SERVICE_CATALOG_FILE, SaveFormat.XLSX);
            return new File(SERVICE_CATALOG_FILE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private boolean checkServiceFields(List<ServiceCatalogDto> serviceCatalogDtos) {
        for (ServiceCatalogDto service : serviceCatalogDtos
        ) {
            if (service.getServiceName() == null) {
                return false;
            }
            if (service.getServiceName().isBlank()) {
                return false;
            }
            if (service.getPrice() == 0 || service.getTime() == 0) {
                return false;
            }
        }
        return true;
    }
    private boolean checkSettingFields(WorkSettings settings){
        return !settings.getCity().isBlank()
                && !settings.getBuilding().isBlank()
                && !settings.getPhoneNumber().isBlank()
                && !settings.getStreet().isBlank();
    }
    private List<ServiceCatalog> getServiceCatalogFromDto(List<ServiceCatalogDto> dtos) {
        List<ServiceCatalog> catalogs = new ArrayList<>();
        if (dtos == null) {
            return null;
        }
        for (ServiceCatalogDto dto : dtos
        ) {
            catalogs.add(dto.getServiceCatalog());
        }
        return catalogs;
    }
}
