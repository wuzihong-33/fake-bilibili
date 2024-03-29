package com.bilibili.api;

import com.bilibili.domain.*;
import com.bilibili.service.VideoService;
import com.bilibili.support.UserSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
public class VideoApi {
    @Autowired
    private VideoService videoService;

    @Autowired
    private UserSupport userSupport;
    
    /**
     * 视频投稿
     */
    @PostMapping("/videos")
    public JsonResponse<String> addVideos(@RequestBody Video video) {
        Long userId = userSupport.getCurrentUserId();
        video.setUserId(userId);
        videoService.addVideos(video);
        //在es中添加一条视频数据
//        elasticSearchService.addVideo(video);
        return JsonResponse.success();
    }

    /**
     * 分页查询视频列表
     */
    @GetMapping("/videos")
    public JsonResponse<PageResult<Video>> pageListVideos(Integer size, Integer no, String area){
        PageResult<Video> result = videoService.pageListVideos(size, no ,area);
        return new JsonResponse<>(result);
    }

    /**
     * 视频在线播放（本质上就是去下载）
     * 以流的形式写在http响应的输出流里边
     */
    @GetMapping("/video-slices")
    public void viewVideoOnlineBySlices(HttpServletRequest request,
                                        HttpServletResponse response,
                                        String relativePath) {
        videoService.viewVideoOnlineBySlices(request, response, relativePath);
    }

    
    /**
     * 点赞视频
     */
    @PostMapping("/video-likes")
    public JsonResponse<String> addVideoLike(@RequestParam Long videoId) {
        Long userId = userSupport.getCurrentUserId();
        videoService.addVideoLike(videoId, userId);
        return JsonResponse.success();
    }

    /**
     * 取消点赞视频
     */
    @DeleteMapping("/video-likes")
    public JsonResponse<String> deleteVideoLike(@RequestParam Long videoId) {
        Long userId = userSupport.getCurrentUserId();
        videoService.deleteVideoLike(videoId, userId);
        return JsonResponse.success();
    }

    /**
     * 视频收藏
     */
    @PostMapping("/video-collections")
    public JsonResponse<String> addVideoCollection(@RequestBody VideoCollection videoCollection){
        Long userId = userSupport.getCurrentUserId();
        videoService.addVideoCollection(videoCollection, userId);
        return JsonResponse.success();
    }

    /**
     * 取消收藏视频
     */
    @DeleteMapping("/video-collections")
    public JsonResponse<String> deleteVideoCollection(@RequestParam Long videoId){
        Long userId = userSupport.getCurrentUserId();
        videoService.deleteVideoCollection(videoId, userId);
        return JsonResponse.success();
    }

    /**
     * 查询视频收藏数量
     */
    @GetMapping("/video-collections")
    public JsonResponse<Map<String, Object>> getVideoCollections(@RequestParam Long videoId){
        Long userId = null;
        try {
            userId = userSupport.getCurrentUserId();
        } catch (Exception ignored) {}
        Map<String, Object> result = videoService.getVideoCollections(videoId, userId);
        return new JsonResponse<>(result);
    }

    /**
     * 视频投币
     */
    @PostMapping("/video-coins")
    public JsonResponse<String> addVideoCoins(@RequestBody VideoCoin videoCoin){
        Long userId = userSupport.getCurrentUserId();
        videoService.addVideoCoins(videoCoin, userId);
        return JsonResponse.success();
    }

    /**
     * 查询视频投币数量
     */
    @GetMapping("/video-coins")
    public JsonResponse<Map<String, Object>> getVideoCoins(@RequestParam Long videoId){
        Long userId = null;
        try{
            userId = userSupport.getCurrentUserId();
        } catch (Exception ignored){}
        Map<String, Object> result = videoService.getVideoCoins(videoId, userId);
        return new JsonResponse<>(result);
    }


    /**
     * 添加视频评论
     */
    @PostMapping("/video-comments")
    public JsonResponse<String> addVideoComment(@RequestBody VideoComment videoComment){
        Long userId = userSupport.getCurrentUserId();
        videoService.addVideoComment(videoComment, userId);
        return JsonResponse.success();
    }

    /**
     * 分页查询视频评论
     */
    @GetMapping("/video-comments")
    public JsonResponse<PageResult<VideoComment>> pageListVideoComments(@RequestParam Integer size,
                                                                        @RequestParam Integer no,
                                                                        @RequestParam Long videoId){
        PageResult<VideoComment> result = videoService.pageListVideoComments(size, no, videoId);
        return new JsonResponse<>(result);
    }
    
    
    /**
     * 查询视频点赞数量？？？
     */
//    @GetMapping("/video-likes")
//    public JsonResponse<Map<String, Object>> getVideoLikes(@RequestParam Long videoId){
//        Long userId = null;
//        try{
//            userId = userSupport.getCurrentUserId();
//        }catch (Exception ignored){}
//        Map<String, Object> result = videoService.getVideoLikes(videoId, userId);
//        return new JsonResponse<>(result);
//    }


}
