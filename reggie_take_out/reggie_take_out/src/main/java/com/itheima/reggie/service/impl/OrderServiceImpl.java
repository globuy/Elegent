package com.itheima.reggie.service.impl;

import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrderDto;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrderMapper;
import com.itheima.reggie.service.*;
import net.sf.jsqlparser.expression.StringValue;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderDetailService orderDetailService;

    @Override
    @Transactional
    public R<String> submit(@RequestBody Orders orders){
        Long UserId = BaseContext.getCurrentId();

        LambdaQueryWrapper<ShoppingCart>  queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,UserId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);

        if (shoppingCarts == null ||shoppingCarts.size() == 0){
            throw new CustomException("购物车为空");
        }

        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBook == null){
            throw new CustomException("用户地址有误，无法下单");
        }

        User user = userService.getById(UserId);
        long orderId = IdWorker.getId();

        AtomicInteger amount = new AtomicInteger(0);
        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item)->{
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(UserId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));
        this.save(orders);
        orderDetailService.saveBatch(orderDetails);
        shoppingCarts.remove(queryWrapper);


        return null;

    }
    @Override
    public Page<OrderDto> getPage(Long page, Long pageSize){

        Page<Orders> ordersPage = new Page<>(page, pageSize);
        // 2.创建OrderDto的分页封装器
        Page<OrderDto> dtoPage = new Page<>();
        // 3.创建Orders的查询条件封装器
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        // 3.1 添加查询条件：按下单时间降序排列
        lqw.orderByDesc(Orders::getOrderTime);
        // 3.2 条件查询条件：按当前用户ID查询
        Long userId = BaseContext.getCurrentId();
        lqw.eq(userId != null, Orders::getUserId, userId);
        // 4.Orders分页查询
        this.page(ordersPage, lqw);

        // 5.除了Record都复制
        BeanUtils.copyProperties(ordersPage, dtoPage, "records");

        // 6.获取当前用户所有的order对象
        List<Orders> orders = this.list(lqw);
        // 7.通过stream流逐一包装成OrderDto对象
        List<OrderDto> orderDtos = orders.stream().map(order -> {
            // 7.1 创建OrderDto对象
            OrderDto orderDto = new OrderDto();
            // 7.2 拷贝属性
            BeanUtils.copyProperties(order, orderDto);
            // 7.3 调用OrderDetail业务层获取订单明细集合
            LambdaQueryWrapper<OrderDetail> orderDetailLqw = new LambdaQueryWrapper<>();
            orderDetailLqw.eq(OrderDetail::getOrderId, order.getNumber());
            List<OrderDetail> orderDetails = orderDetailService.list(orderDetailLqw);
            // 7.4 设置orderDto的订单明细属性
            orderDto.setOrderDetails(orderDetails);
            // 7.5 返回orderDto
            return orderDto;
        }).collect(Collectors.toList());

        // 8.设置dtoPage的records属性
        dtoPage.setRecords(orderDtos);
        return dtoPage;


    }
    // 后台管理端获取订单分页展示
    @Override
    public Page<OrderDto> getAllPage(Long page, Long pageSize, String number, String beginTime, String endTime) {
        // 1.创建分页封装器
        Page<Orders> ordersPage = new Page<>(page, pageSize);
        // 2.创建OrderDto的分页封装器
        Page<OrderDto> dtoPage = new Page<>();

        // 3.创建Orders的查询条件封装器
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        // 3.1 添加查询条件：按下单时间降序排列
        lqw.orderByDesc(Orders::getOrderTime);
        // 3.2 添加查询条件：按订单号查询
        lqw.like(number != null, Orders::getNumber, number);
        // 3.3 添加查询条件：  动态SQL-字符串使用StringUtils.isNotEmpty这个方法来判断
        lqw.gt(StringUtils.isNotEmpty(beginTime), Orders::getOrderTime, beginTime);
        lqw.lt(StringUtils.isNotEmpty(endTime), Orders::getOrderTime, endTime);
        // 4.Orders分页查询
        this.page(ordersPage, lqw);

        // 5.除了Record都复制
        BeanUtils.copyProperties(ordersPage, dtoPage, "records");

        // 6.获取当前用户所有的order对象
        List<Orders> orders = this.list(lqw);
        // 7.通过stream流逐一包装成OrderDto对象
        List<OrderDto> orderDtos = orders.stream().map(order -> {
            // 7.1 创建OrderDto对象
            OrderDto orderDto = new OrderDto();
            // 7.2 拷贝属性
            BeanUtils.copyProperties(order, orderDto);
            // 7.3 调用OrderDetail业务层获取订单明细集合
            LambdaQueryWrapper<OrderDetail> orderDetailLqw = new LambdaQueryWrapper<>();
            orderDetailLqw.eq(OrderDetail::getOrderId, order.getNumber());
            List<OrderDetail> orderDetails = orderDetailService.list(orderDetailLqw);
            // 7.4 设置orderDto的订单明细属性
            orderDto.setOrderDetails(orderDetails);
            // 7.5 返回orderDto
            return orderDto;
        }).collect(Collectors.toList());

        // 8.设置dtoPage的records属性
        dtoPage.setRecords(orderDtos);
        return dtoPage;
    }

}
