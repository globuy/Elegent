package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrderDto;
import com.itheima.reggie.entity.Orders;
import org.springframework.web.bind.annotation.RequestBody;

public interface OrderService extends IService<Orders> {
    public R<String> submit(@RequestBody Orders orders);
    // 获取订单分页展示
    public Page<OrderDto> getPage(Long page, Long pageSize);

    Page<OrderDto> getAllPage(Long page, Long pageSize, String number, String beginTime, String endTime);

}
