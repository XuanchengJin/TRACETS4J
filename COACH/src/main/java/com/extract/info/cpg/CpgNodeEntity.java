package com.extract.info.cpg;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class CpgNodeEntity {
    private Long key;
    private Integer line;
    private String type;
    private String name;
    private Integer startLine;
    private Integer endLine;

    public CpgNodeEntity() {}

    public CpgNodeEntity(String[] values) {
        if (values.length == 7) {
            this.key = Long.valueOf(values[0]);
            this.line = StringUtils.isEmpty(values[1]) ? -1 : Integer.parseInt(values[1]);
            this.type = values[2];
            this.name = values[3];
            this.startLine = StringUtils.isEmpty(values[5]) ? -1 : Integer.parseInt(values[5]);
            this.endLine = StringUtils.isEmpty(values[6]) ? -1 : Integer.parseInt(values[6]);
        }
    }
}
