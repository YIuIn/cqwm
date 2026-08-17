package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品ID查询对应的套餐ID
     * @param dishIds
     * @return
     */

    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /**
     * 批量插入菜品数据
     * @param dishes
     */
    void insertBatch(List<SetmealDish> dishes);

    /**
     * 根据套餐id查询对应的菜品
     * @param setmealId
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id=#{setmealId}")
    List<SetmealDish> getDishBySetmealId(Long setmealId);

    /**
     * 根据套餐id批量删除对应的菜品
     * @param setmealIds
     */
    void deleteBySetmealIds(List<Long> setmealIds);


    /**
     * 根据套餐id批量查询对应的菜品信息
     * @param ids
     * @return
     */
    List<SetmealDish> getDishBySetmealIds(List<Long> ids);
}
