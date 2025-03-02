package com.rva.egopass.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class APIResponse<T> {
    private String status;
    private String errorCode;
    private String message;
    private T data;
    private Map<String, String> links;


}
