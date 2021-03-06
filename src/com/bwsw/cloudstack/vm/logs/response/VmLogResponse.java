// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package com.bwsw.cloudstack.vm.logs.response;

import com.bwsw.cloudstack.vm.logs.entity.EntityConstants;
import com.bwsw.cloudstack.vm.logs.entity.ResponseEntity;
import com.bwsw.cloudstack.vm.logs.service.VmLogRequestBuilder;
import com.cloud.serializer.Param;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import org.apache.cloudstack.api.BaseResponse;

public class VmLogResponse extends BaseResponse implements ResponseEntity {

    @Param(description = "the log id")
    @JsonIgnore
    @SerializedName(EntityConstants.ID)
    private String id;

    @Param(description = "the log event timestamp")
    @JsonProperty(VmLogRequestBuilder.DATE_FIELD)
    @SerializedName(EntityConstants.TIMESTAMP)
    private String timestamp;

    @Param(description = "the log file")
    @JsonProperty(VmLogRequestBuilder.LOG_FILE_FIELD)
    @SerializedName(EntityConstants.FILE)
    private String file;

    @Param(description = "the log data")
    @JsonProperty(VmLogRequestBuilder.DATA_FIELD)
    @SerializedName(EntityConstants.LOG)
    private String log;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }
}
