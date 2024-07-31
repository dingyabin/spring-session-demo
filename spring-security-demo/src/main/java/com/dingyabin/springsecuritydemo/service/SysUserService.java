package com.dingyabin.springsecuritydemo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingyabin.springsecuritydemo.entity.SysUser;
import com.dingyabin.springsecuritydemo.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

/**
 * @author 丁亚宾
 */
@Service
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {



    public SysUser loadSysUserByName(String name) {
        LambdaQueryWrapper<SysUser> lambdaQuery = Wrappers.lambdaQuery(SysUser.class);
        lambdaQuery.eq(SysUser::getName, name);
        return getOne(lambdaQuery);
    }


}




