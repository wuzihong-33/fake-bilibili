package com.bilibili.service;

import com.bilibili.constant.UserConstant;
import com.bilibili.dao.UserFollowingDao;
import com.bilibili.domain.FollowingGroup;
import com.bilibili.domain.User;
import com.bilibili.domain.UserFollowing;
import com.bilibili.domain.UserInfo;
import com.bilibili.exception.ConditionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class UserFollowingService {
    @Autowired
    private UserFollowingDao userFollowingDao;

    @Autowired
    private FollowingGroupService followingGroupService;

    @Autowired
    private UserService userService;

    /**
     * 添加用户关注
     * 步骤：
     * 1、如果前端没有传来关注分组，则设置为默认分组：2
     * 2、如果前端传来关注分组，判断该分组是否存在
     * 3、分组存在，判断被关注的用户是否存在
     * 4、删除 被关注者 和 关注者 间的关注信息（如果这个关联不存在也不会报错）——不想写更新操作，就可以这么写：写一条删除、然后一条插入
     * 5、删除 再 插入 ，需要保证原子性；添加事务注释 @Transactional，方便失败时回滚事务
     * @param userFollowing
     */
    @Transactional
    public void addUserFollowings(UserFollowing userFollowing) {
        Long groupId = userFollowing.getGroupId();
        if(groupId == null){
            FollowingGroup followingGroup = followingGroupService.getByType(UserConstant.USER_FOLLOWING_GROUP_TYPE_DEFAULT);
            userFollowing.setGroupId(followingGroup.getId());
        }else{
            FollowingGroup followingGroup = followingGroupService.getById(groupId);
            if(followingGroup == null){
                throw new ConditionException("关注分组不存在！");
            }
        }
        Long followingId = userFollowing.getFollowingId();
        User user = userService.getUserById(followingId);
        if(user == null){
            throw new ConditionException("关注的用户不存在！");
        }
        userFollowingDao.deleteUserFollowing(userFollowing.getUserId(), followingId);
        userFollowing.setCreateTime(new Date());
        userFollowingDao.addUserFollowing(userFollowing);
    }

    /**
     * 获取全部关注列表和自定义列表
     * @return
     */
    public List<FollowingGroup> getUserFollowings(Long userId) {
        List<FollowingGroup> result = new ArrayList<>();
        // 获取全部关注分组信息
        List<UserFollowing> userFollowings = userFollowingDao.getUserFollowings(userId);
        Set<Long> followingIdSet = userFollowings.stream().map(UserFollowing::getFollowingId).collect(Collectors.toSet());
        List<UserInfo> userInfoList = new ArrayList<>();
        if(followingIdSet.size() > 0){
            userInfoList = userService.getUserInfoByUserIds(followingIdSet);
        }
        for (UserFollowing userFollowing : userFollowings) {
            for (UserInfo userInfo : userInfoList) {
                if (userFollowing.getFollowingId().equals(userInfo.getUserId())) {
                    userFollowing.setUserInfo(userInfo);
                }
            }
        }
        FollowingGroup allGroup = new FollowingGroup(); // 全部关注
        allGroup.setName(UserConstant.USER_FOLLOWING_GROUP_ALL_NAME);
        allGroup.setFollowingUserInfoList(userInfoList);
        result.add(allGroup);
        // 获取自定义分组信息
        // 疑惑：为什么不能在数据库层面写一个关联查询；另外，能不能再细分一个do、uo之类的东西
        List<FollowingGroup> userFollowingGroupsAll = followingGroupService.getByUserId(userId);
        for (FollowingGroup followingGroup : userFollowingGroupsAll) {
            List<UserInfo> userInfos = new ArrayList<>();
            for (UserFollowing userFollowing : userFollowings) {
                if (followingGroup.getId().equals(userFollowing.getGroupId())) {
                    userInfos.add(userFollowing.getUserInfo());
                }
            }
            followingGroup.setFollowingUserInfoList(userInfos);
            result.add(followingGroup);
        }
        return result;
    }

    public Long addUserFollowingGroups(FollowingGroup followingGroup) {
        followingGroup.setCreateTime(new Date());
        followingGroup.setType(UserConstant.USER_FOLLOWING_GROUP_TYPE_USER);
        followingGroupService.addFollowingGroup(followingGroup);
        return followingGroup.getId();
    }

    public List<FollowingGroup> getUserFollowingGroups(Long userId) {
        return followingGroupService.getUserFollowingGroups(userId);
    }
    
    
    public List<UserFollowing> getUserFans(Long userId) {
        // 1、获得所有userId=userId的
        Long followingId = userId;// 求userId的粉丝：即，followingId=userId的字段
        List<UserFollowing> fanList = userFollowingDao.getUserFans(followingId); // userId的粉丝列表
        Set<Long> fanIdSet = fanList.stream().map(UserFollowing::getUserId).collect(Collectors.toSet());
        List<UserInfo> fanUserInfoList = new ArrayList<>();
        if(fanIdSet.size() > 0){
            fanUserInfoList = userService.getUserInfoByUserIds(fanIdSet);
        }
        List<UserFollowing> followingList = userFollowingDao.getUserFollowings(userId); // userId的关注列表
        for (UserFollowing fan : fanList) {
            for(UserInfo userInfo : fanUserInfoList){
                if (fan.getUserId().equals(userInfo.getUserId())) {
                    userInfo.setFollowed(false); 
                    fan.setUserInfo(userInfo);
                }
            }
            for (UserFollowing following : followingList) {
                if (following.getFollowingId().equals(fan.getUserId())) {
                    fan.getUserInfo().setFollowed(true);// 互关
                }
            }
        }
        return fanList;
    }
}
