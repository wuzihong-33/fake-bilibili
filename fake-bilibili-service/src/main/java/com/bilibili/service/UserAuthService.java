package com.bilibili.service;

import com.bilibili.domain.auth.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserAuthService {

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private AuthRoleService authRoleService;

    /**
     * 获取用户所有的可操作权限
     * @param userId
     * @return
     */
    public UserAuthorities getUserAuthorities(Long userId) {
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        Set<Long> roleIdSet = userRoleList.stream().map(UserRole :: getRoleId).collect(Collectors.toSet());
        List<AuthRoleElementOperation> roleElementOperationList = authRoleService.getRoleElementOperationsByRoleIds(roleIdSet);
        List<AuthRoleMenu> authRoleMenuList = authRoleService.getAuthRoleMenusByRoleIds(roleIdSet);
        UserAuthorities userAuthorities = new UserAuthorities();
        userAuthorities.setRoleElementOperationList(roleElementOperationList);
        userAuthorities.setRoleMenuList(authRoleMenuList);
        return userAuthorities;
    }

//    public void addUserDefaultRole(Long id) {
//        UserRole userRole = new UserRole();
//        AuthRole role = authRoleService.getRoleByCode(AuthRoleConstant.ROLE_LV0);
//        userRole.setUserId(id);
//        userRole.setRoleId(role.getId());
//        userRoleService.addUserRole(userRole);
//    }
}
