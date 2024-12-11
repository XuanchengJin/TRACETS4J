package com.extract.utils;

import com.csvreader.CsvReader;
import com.extract.info.cpg.CpgNodeEntity;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class CsvFileUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(CsvFileUtils.class);

    public List<CpgNodeEntity> readNodeCsv(String nodeCsvPath) {
        List<CpgNodeEntity> cpgNodeEntityList = new ArrayList<>();
        try {
            CsvReader csvReader = new CsvReader(nodeCsvPath, ',', Charset.defaultCharset());
            csvReader.readHeaders();
            while (csvReader.readRecord()) {
                String[] values = csvReader.getValues();
                cpgNodeEntityList.add(new CpgNodeEntity(values));
            }
            csvReader.close();
        } catch (IOException e) {
            logger.error("reader node file fair.", e);
        }
        return cpgNodeEntityList;
    }

    public Map<Long, List<Long>> readEdgesCsv(String edgesCsvPath) {
        Map<Long, List<Long>> edgesMap = new HashMap<>();
        try {
            CsvReader csvReader = new CsvReader(edgesCsvPath, ',', Charset.defaultCharset());
            csvReader.readHeaders();
            while (csvReader.readRecord()) {
                String[] values = csvReader.getValues();
                if (values.length == 3 && Objects.equals(values[2], "DFG")) {
                    Long key = StringUtils.isNumeric(values[1]) ? Long.parseLong(values[1]) : -99;
                    List<Long> mapValue = edgesMap.getOrDefault(key, new ArrayList<>());
                    mapValue.add(Long.parseLong(values[0]));
                    edgesMap.put(key, mapValue);
                }
            }
            csvReader.close();
        } catch (IOException e) {
            logger.error("reader node file fair", e);
        }
        return edgesMap;
    }
}
