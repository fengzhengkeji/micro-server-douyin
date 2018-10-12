package com.anoyi.douyin.service;

import com.alibaba.fastjson.JSON;
import com.anoyi.douyin.entity.DyAweme;
import com.anoyi.douyin.entity.DyUser;
import com.anoyi.douyin.rpc.RpcNodeDyService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
@Slf4j
public class DouyinService {

    private final static String UserAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1";

    private final static String XMLHttpRequest = "XMLHttpRequest";

    private final static String VIDEO_LIST_API = "https://www.amemv.com/aweme/v1/aweme/post/?user_id=%s&count=21&max_cursor=0&aid=1128&_signature=%s&dytk=%s";

    private final static String USER_SHARE_API = "https://www.amemv.com/share/user/%s?share_type=link";

    private final RpcNodeDyService rpcNodeDyService;

    /**
     * 获取抖音用户视频列表
     */
    public DyAweme videoList(String dyId, String dytk) {
        String signature = rpcNodeDyService.generateSignature(dyId);
        String api = String.format(VIDEO_LIST_API, dyId, signature, dytk);
        try {
            Document document = httpGet(api);
            return JSON.parseObject(document.text(), DyAweme.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("HTTP request error: " + api);
    }

    /**
     * 获取抖音用户信息
     */
    public DyUser getDyUser(String dyId) {
        String api = String.format(USER_SHARE_API, dyId);
        try {
            DyUser dyUser = new DyUser();
            Document document = httpGet(api);
            Matcher matcher = Pattern.compile("dytk: '(.*?)'").matcher(document.html());
            if (matcher.find()){
                String dytk = matcher.group(1);
                System.out.println("dytk: ----  "+dytk);
                dyUser.setDytk(dytk);
            }
            return dyUser;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("HTTP request error: " + api);
    }

    private Document httpGet(String url) throws IOException {
        return Jsoup.connect(url)
                .header("user-agent", UserAgent)
                .header("x-requested-with", XMLHttpRequest)
                .ignoreContentType(true).get();
    }

}
