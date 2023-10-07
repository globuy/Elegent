package com.itheima.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.reggie.entity.AddressBook;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 30612
* @description 针对表【address_book(地址管理)】的数据库操作Mapper
* @createDate 2023-09-21 20:01:23
* @Entity generator.domain.AddressBook
*/
@Mapper
public interface AddressBookMapper extends BaseMapper<AddressBook> {

}




