package com.example.springbootproject.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private boolean success;
    private String errorMag;
    private Object data;
    private Long total;

    public static Result ok() {
        return new Result(true,null,null,null);
    }
    public static  Result ok(Object data) {
        return new Result(true,null,data,null);
    }
    public static Result ok(List<?> data, Long total){

        return new Result(true, null, data, total);
    }

    public static Result fail(String errorMag) {
        return new Result(false,errorMag,null,null);
    }

}

