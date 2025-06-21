package com.nonu1l.msgboxcore.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nonu1l.msgboxcore.entity.UserExt;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 * @author nonu1l
 * @date 2024/11/09
 */

@Mapper
public interface UserMapper extends BaseMapper<UserExt> {

}
