package com.anoyi.douyin.controller;

import com.anoyi.douyin.entity.DyAweme;
import com.anoyi.douyin.entity.DyUser;
import com.anoyi.douyin.service.DouyinService;
import com.anoyi.douyin.bean.UserVO;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/douyin")
@AllArgsConstructor
@CrossOrigin("*")
public class DouyinController {

    private final DouyinService douyinService;

    @GetMapping("/user/{id}")
    public UserVO user(@PathVariable("id") String id){

        return null;
    }

    @GetMapping("/videos/{id}")
    public DyAweme videos(@PathVariable("id") String id){
        DyUser dyUser = douyinService.getDyUser(id);
        return douyinService.videoList(id, dyUser.getDytk());
    }

}