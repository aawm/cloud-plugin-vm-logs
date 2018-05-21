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

package com.bwsw.cloudstack.vm.logs.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.ServerApiException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ParameterUtils {

    private static final ObjectMapper s_objectMapper = new ObjectMapper();

    public static String convertToJson(Object[] objects) throws JsonProcessingException {
        if (objects == null) {
            return null;
        }
        return s_objectMapper.writeValueAsString(objects);
    }

    public static Object[] parseFromJson(String json) throws IOException {
        if (json == null) {
            return null;
        }
        return s_objectMapper.readValue(json, Object[].class);
    }

    public static LocalDateTime parseDate(String date, String paramName) throws DateTimeParseException {
        if (date == null) {
            return null;
        }
        try {
            return LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new ServerApiException(ApiErrorCode.PARAM_ERROR, "\"" + paramName + "\" parameter is invalid");
        }
    }
}
