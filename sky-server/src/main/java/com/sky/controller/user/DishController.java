package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api("C端-菜品浏览接口")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 根据分类ID查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId){
        log.info("要查询的菜品的分类id为:{}",categoryId);
        //构造redis当中的key，规则：dish_分类ID
        String key="dish_"+categoryId;
        //查询redis中是否存在菜品数据
        List<DishVO> list =(List<DishVO>)redisTemplate.opsForValue().get(key);
        //如果存在,直接返回,无需查询数据库
        if(list!=null&&list.size()!=0){
            return Result.success(list);
        }
        //如果不存在,查询数据库，将查询到的数据保存到redis中
        Dish dish=new Dish();
        dish.setCategoryId(categoryId);
        //查询的菜品要在起售中
        dish.setStatus(StatusConstant.ENABLE);
        list = dishService.listWithFlavor(dish);
        redisTemplate.opsForValue().set(key,list);
        return Result.success(list);
    }
}
