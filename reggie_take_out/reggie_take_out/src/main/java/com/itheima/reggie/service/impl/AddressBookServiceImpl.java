package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.mapper.AddressBookMapper;
import com.itheima.reggie.service.AddressBookService;
import org.springframework.stereotype.Service;

/**
* @author 30612
* @description 针对表【address_book(地址管理)】的数据库操作Service实现
* @createDate 2023-09-21 20:01:23
*/
@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper,AddressBook>
    implements AddressBookService {

}




