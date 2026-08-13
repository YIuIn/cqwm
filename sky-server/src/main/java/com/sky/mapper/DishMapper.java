package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DishMapper {
    /**
     * 根据分类查找菜品数量
     * @param id
     * @return
     */
    @Select("select count(*) from dish where dish.category_id=#{id}")
    Integer countByCategoryId(Long id);

    /**
     * 插入菜品数据
     * @param dish
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    /**
     * 菜品分类查询
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据id查询菜品
     * @param id
     */
    @Select("select * from dish where id=#{id}")
    Dish getById(Long id);

    /**
     * 根据ID删除菜品
     */
/*    @Delete("delete from dish where id=#{id}")
    void deleteByID(Long id);*/

    /**
     * 根据菜品id集合批量删除菜品
     * @param ids
     */
    void deleteByIds(List<Long> ids);

    /**
     * 根据主键动态修改菜品数据
     * @param dish
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 根据id跟状态查找菜品
     * @param dish
     * @return
     */
    @Select("select * from dish where category_id=#{categoryId} and status=#{status}")
    List<Dish> list(Dish dish);

    //菜品起售停售
    @Update("update dish set status=#{status} where id=#{id}")
    void startOrStop(Integer status, Long id);
}
