package com.extract.info;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class RepoResult {
    @JSONField(ordinal = 1)
    public String repo_id;
    @JSONField(ordinal = 2)
    public String repo_name;
    @JSONField(ordinal = 3)
    public String repo_url;
    @JSONField(serialize = false)
    public int extract_num;

    public RepoResult(String repo_id, String repo_name, String repo_url) {
        this.repo_id =  repo_id;
        this.repo_name = repo_name;
        this.repo_url = repo_url;
        extract_num = 0;
    }
}
