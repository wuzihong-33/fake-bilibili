package com.bilibili.domain;

public class JsonResponse<T> {
    private String code;
    private String msg;
    private T data;

    public JsonResponse(String code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public JsonResponse(T data){
        this("0", "success");
        this.data = data;
    }

    // 场景：请求成功但不需要返回数据给前端
    public static JsonResponse<String> success(){
        return new JsonResponse<>(null);
    }
    public static JsonResponse<String> success(String data){
        return new JsonResponse<>(data);
    }

    public static JsonResponse<String> fail(){
        return new JsonResponse<>("1", "fail");
    }

    public static JsonResponse<String> fail(String code, String msg){
        return new JsonResponse<>(code, msg);
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
