package com.anoyi.douyin.service;

import com.alibaba.fastjson.JSON;
import com.anoyi.douyin.bean.DyUserVO;
import com.anoyi.douyin.entity.DyAweme;
import com.anoyi.douyin.rpc.RpcNodeDyService;
import com.anoyi.douyin.util.DyNumberConvertor;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
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

    private final static String VIDEO_LIST_API = "https://aweme.snssdk.com/aweme/v1/aweme/post/?iid=49056318644&device_id=49424741750&os_api=18&app_name=aweme&idfa=A0323AA0-7404-4C40-A197-F56435940521&device_platform=iphone&build_number=31006&vid=73FDACB2-B49A-403E-94DF-62B55CE64418&openudid=a46ca9b2600e4ca85c74c5eef29434a30e7b5ea9&device_type=iPhone7,1&app_version=3.1.0&js_sdk_version=1.3.0.1&version_code=3.1.0&os_version=12.1&screen_width=1242&aid=1128&pass-region=1&count=21&max_cursor=%s&user_id=%s&mas=010b172b781c26a10dbd49c6ba98f702ac5d1c01a7dff4cfea6f3e&as=a1059a3ea5d1eb15524585";

    private final static String USER_SHARE_API = "https://www.iesdouyin.com/share/user/%s";

    private final static String REFERER_URL = "https://www.iesdouyin.com/share/user/%s";

    private final RpcNodeDyService rpcNodeDyService;

    /**
     * 获取抖音用户视频列表
     */
    public DyAweme videoList(String dyId, String dytk, String cursor) {
        String api = String.format(VIDEO_LIST_API, cursor, dyId);
        String referer = String.format(REFERER_URL, dyId);
        try {
            Document document = httpGetWithReferer(api, referer);
            return JSON.parseObject(document.text(), DyAweme.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("HTTP request error: " + api);
    }

    /**
     * 获取抖音用户信息
     */
    public DyUserVO getDyUser(String dyId) {
        String api = String.format(USER_SHARE_API, dyId);
        try {
            DyUserVO dyUser = new DyUserVO();
            dyUser.setId(dyId);
            Document document = httpGet(api);
            parseIconFonts(document);
            String nickname = document.select("p.nickname").text();
            dyUser.setNickname(nickname);
            String avatar = document.select("img.avatar").attr("src");
            dyUser.setAvatar(avatar);
            String tk = match(document.html(), "dytk: '(.*?)'");
            dyUser.setTk(tk);
            String shortId = document.select("p.shortid").text();
            dyUser.setShortId(shortId);
            String verifyInfo = document.select("div.verify-info").text();
            dyUser.setVerifyInfo(verifyInfo);
            String signature = document.select("p.signature").text();
            dyUser.setSignature(signature);
            String location = document.select("span.location").text();
            dyUser.getExtraInfo().put("location", location);
            String constellation = document.select("span.constellation").text();
            dyUser.getExtraInfo().put("constellation", constellation);
            String focus = document.select("span.focus.block span.num").text();
            dyUser.getFollowInfo().put("focus", focus);
            String follower = document.select("span.follower.block span.num").text();
            dyUser.getFollowInfo().put("follower", follower);
            String likeNum = document.select("span.liked-num.block span.num").text();
            dyUser.getFollowInfo().put("likeNum", likeNum);
            return dyUser;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("HTTP request error: " + api);
    }

    /**
     * HTTP 请求
     */
    private Document httpGet(String url) throws IOException {
        Connection.Response response = Jsoup.connect(url)
                .header("user-agent", UserAgent)
                .header("x-requested-with", XMLHttpRequest)
                .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("referer", "https://www.iesdouyin.com/share/user/")
                .ignoreContentType(true).execute();
        String html = response.body().replace("&#xe", "");
        return Jsoup.parse(html);
    }

    /**
     * HTTP 请求
     */
    private Document httpGetWithReferer(String url, String referer) throws IOException {
        Connection.Response response = Jsoup.connect(url)
                .header("user-agent", UserAgent)
                .header("x-requested-with", XMLHttpRequest)
                .ignoreContentType(true).execute();
        String html = response.body().replace("&#xe", "");
        return Jsoup.parse(html);
    }

    /**
     * 正则匹配
     */
    private String match(String content, String regx){
        Matcher matcher = Pattern.compile(regx).matcher(content);
        if (matcher.find()){
            return matcher.group(1);
        }
        return "";
    }

    /**
     * 全局 icon 数字解析
     */
    private void parseIconFonts(Document document){
        Elements elements = document.select("i.icon.iconfont");
        elements.forEach(element -> {
            String text = element.text();
            String number = DyNumberConvertor.getNumber(text);
            element.text(number);
        });
    }

}
