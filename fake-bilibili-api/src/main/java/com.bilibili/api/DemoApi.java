package com.bilibili.api;

import com.bilibili.domain.JsonResponse;
import com.bilibili.service.DemoService;
import com.bilibili.service.util.FastDFSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/demo")
@RestController
public class DemoApi {

    @Autowired
    private DemoService demoService;
    @Autowired
    private FastDFSUtil fastDFSUtil;
    

    @GetMapping("/test")
    public JsonResponse<String> test() {
        return JsonResponse.success("test api access success");
    }

    /**
     * 测试上传文件切片功能
     * @param file
     * @throws Exception
     */
    @PostMapping("/slices")
    public void slices(MultipartFile file) throws Exception {
        System.out.println("fileName: " + file.getName());
        fastDFSUtil.convertFileToSlices(file);
    }
    
//    @GetMapping("/query")
//    public String query(Long id) {
//        return demoService.query(id);
//    }
}
