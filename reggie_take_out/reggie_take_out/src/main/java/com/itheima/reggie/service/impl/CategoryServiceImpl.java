package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private DishService dishService;

    @Autowired(required = false)
    private SetmealService setmealService;


    @Override
    public void remove(long id){

        LambdaQueryWrapper<Dish> dishlambdaQueryWrapper = new LambdaQueryWrapper<Dish>();
        //添加查询条件，根据id进行分类

        dishlambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int count1 = dishService.count(dishlambdaQueryWrapper);
        //判断是否关联了菜品，关联了就抛出一个业务异常
        if (count1 > 0){
            throw new CustomException("当前分类下关联了菜品");
        }

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<Setmeal>();
        //添加查询条件，根据id进行分类

        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        //判断是否关联了套餐，关联了就抛出一个业务异常

        if(count2 > 0){
            throw new CustomException("当前分类下关联了套餐");
        }
        //正常删除条件
    }

}
