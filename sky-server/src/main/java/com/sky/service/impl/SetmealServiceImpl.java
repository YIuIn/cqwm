package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;
    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page=setmealMapper.page(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        //向套餐表插入一条数据
        setmealMapper.saveWithDish(setmeal);
        Long setmealId= setmeal.getId();
        List<SetmealDish> dishes=setmealDTO.getSetmealDishes();
        if(dishes!=null&&dishes.size()>0) {
            dishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealId);
            });
            setmealDishMapper.insertBatch(dishes);
        }
    }

    /**
     * 根据id查询套餐数据
     * @param id
     * @return
     */
    @Override
    public SetmealVO getById(Long id) {
        SetmealVO setmealVO=new SetmealVO();
        Setmeal setmeal = setmealMapper.getById(id);
        BeanUtils.copyProperties(setmeal,setmealVO);
        List<SetmealDish> setmealDishes = setmealDishMapper.getDishBySetmealId(id);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 根据id批量删除套餐
     * @param ids
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        //判断是否存在起售中的套餐
        for(Long id:ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if(setmeal!=null && setmeal.getStatus() == StatusConstant.ENABLE){
                //当前套餐处于起售中不能删除
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //根据套餐id批量删除套餐
        setmealMapper.deleteBatch(ids);
        //删除套餐对应的菜品数据
        setmealDishMapper.deleteBySetmealIds(ids);
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Transactional
    @Override
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        //修改套餐表数据
        setmealMapper.update(setmeal);
        //修改套餐关联的菜品数据
        List<Long> list=new ArrayList<>();
        list.add(setmealDTO.getId());
        //删除原来关联的菜品数据
        setmealDishMapper.deleteBySetmealIds(list);
        //插入修改后的数据
        List<SetmealDish> setmealDishes=setmealDTO.getSetmealDishes();
        if(setmealDishes!=null&&setmealDishes.size()>0) {
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealDTO.getId());
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 起售停售套餐
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        List<SetmealDish> setmealDishes = setmealDishMapper.getDishBySetmealId(id);
        if(status==StatusConstant.ENABLE) {
            List<Long> ids =new ArrayList<>();
            for (SetmealDish setmealDish : setmealDishes) {
                ids.add(setmealDish.getDishId());

            }
            List<Dish> dishes=dishMapper.getByIds(ids);
            if(ids==null||ids.size()==0){
                throw new SetmealEnableFailedException(MessageConstant.SETMEAL_NOT_HAVE_DISH);
            }
            for(Dish dish:dishes) {
                if (dish.getStatus() != StatusConstant.ENABLE) {
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            }
        }
        Setmeal setmeal=new Setmeal();
        setmeal.setStatus(status);
        setmeal.setId(id);
        setmealMapper.update(setmeal);
    }


}
