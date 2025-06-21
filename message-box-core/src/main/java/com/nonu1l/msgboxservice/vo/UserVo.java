package com.nonu1l.msgboxservice.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;

/** User VO
 * @author zhonghanbo
 * @date 2024年11月26日 16:02
 */

@Data
public class UserVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String userId;
    private String nickname;
    private String username;
    private String email;
    private Integer status;
    private Timestamp lastLoginTime;
}
