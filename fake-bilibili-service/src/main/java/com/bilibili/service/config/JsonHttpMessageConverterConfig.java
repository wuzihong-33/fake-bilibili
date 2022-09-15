package com.bilibili.service.config;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
public class JsonHttpMessageConverterConfig {

    // DisableCircularReferenceDetect 作用展示demo
    public static void main(String[] args){
        List<Object> list = new ArrayList<>();
        Object o = new Object();
        list.add(o);
        list.add(o);
        System.out.println(list.size());
        //[{},{"$ref":"$[0]"}]    不关闭循环引用检测
        //[{},{}]                 关闭循环引用检测
        // fastJson在发现前面已经有相同的序列化元素时，后一个就会返回它的引用
        System.out.println(JSONObject.toJSONString(list));
        System.out.println(JSONObject.toJSONString(list, SerializerFeature.DisableCircularReferenceDetect));
    }

    @Bean
    @Primary
    public HttpMessageConverters fastJsonHttpMessageConverters(){
        FastJsonHttpMessageConverter fastConverter = new FastJsonHttpMessageConverter();
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");
        // WriteNullStringAsEmpty  将null转成空字符串，这样就能让前端看到了
        // MapSortField 对map根据key做一个升序的排序
        // DisableCircularReferenceDetect 关闭循环引用检测
        fastJsonConfig.setSerializerFeatures(
                SerializerFeature.PrettyFormat,
                SerializerFeature.WriteNullStringAsEmpty,
                SerializerFeature.WriteNullListAsEmpty,
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.MapSortField,
                SerializerFeature.DisableCircularReferenceDetect
        );
        fastConverter.setFastJsonConfig(fastJsonConfig);
        //如果使用feign进行微服务间的接口调用，则需要加上该配置
        fastConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));
        return new HttpMessageConverters(fastConverter);
    }
}
