/*
 * Copyright (C) 2009-2016 Hangzhou 2Dfire Technology Co., Ltd. All rights reserved
 */
package com.spring.springboot.realm;

import com.spring.springboot.bean.Account;
import com.spring.springboot.dao.AccountDAO;
import com.spring.springboot.session.RedisSessionDAO;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;

import javax.annotation.Resource;

/**
 * MyShiroRealm
 *
 * @author 萝卜丝
 * @since 2017-10-09
 */
public class MyShiroRealm extends AuthorizingRealm {

    @Resource
    private AccountDAO accountDAO;

    @Resource
    private RedisSessionDAO redisSessionDAO;

    //用户登录次数计数  redisKey 前缀
    private String SHIRO_LOGIN_COUNT = "shiro_login_count_";

    //用户登录是否被锁定    一小时 redisKey 前缀
    private String SHIRO_IS_LOCK = "shiro_is_lock_";
    /**
     * 认证信息.(身份验证)
     * :
     * Authentication 是用来验证用户身份
     * 主要是用来进行身份认证的，也就是说验证用户输入的账号和密码是否正确。
     * @param authcToken 身份认证信息
     * @return 认证之后的信息
     * @throws AuthenticationException 验证不通过则抛出异常
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken) authcToken;
        String name = token.getUsername();
        String password = String.valueOf(token.getPassword());


        // 从数据库获取对应用户名密码的用户
        Account user = accountDAO.findByName(name);
        if (user == null || !user.getName().equals(name) || !user.getPassword().equals(password)) {
            throw new UnknownAccountException("用户名或密码错误！");
        } else if("1".equals(user.getAccountState())) {
            throw new DisabledAccountException("此帐号已经设置为禁止登录！");
        }

        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        System.out.println(session.getId().toString());
        session.setAttribute("user", user);

        // this.redisSessionDAO.doCreate(session);

        return new SimpleAuthenticationInfo(
                user, //用户名
                user.getPassword(), //密码
                getName()  //realm name
        );
    }



    /**
     * 此方法调用  hasRole,hasPermission的时候才会进行回调.
     * 是权限控制，当访问到页面的时候，使用了相应的注解或者shiro标签才会执行此方法否则不会执行，
     * 所以如果只是简单的身份认证没有权限的控制的话，那么这个方法可以不进行实现，直接返回null即可。
     *
     * @param principals 身份及凭据
     * @return 权限验证
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {

        System.out.println("权限配置 --> MyShiroRealm.doGetAuthorizationInfo()");

        // SimpleAuthorizationInfo: 进行角色的添加和权限的添加。
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();

        return authorizationInfo;
    }



}