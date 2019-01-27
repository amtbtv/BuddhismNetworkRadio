package com.jianchi.fsp.buddhismnetworkradio.tools;

public class UrlHelper {

    private static final String picUrl = "https://vod.hwadzan.com/redirect/v/amtbtv/pic/";

    //{"channels":[{"name":"\u963f\u5f4c\u9640\u7d93","amtbid":"1"},{"name":"\u7121\u91cf\u58fd\u7d93","amtbid":"2"}]}
    //private static final String liveChannelsUrl  = "https://amtbapi.hwadzan.com/amtbtv/channels/live,mp4";
    private static final String livesUrl  = "https://amtbapi.hwadzan.com/amtbtv/channels/live";
    private static final String channelsUrl  = "https://amtbapi.hwadzan.com/amtbtv/channels/mp3";

    //{"files":["02-012-0001.mp4","02-012-0002.mp4"]}
    //private static final String filesUrl  = "https://amtbapi.hwadzan.com/amtbtv/%s/mp3";
    //{"files":["02-012-0001.mp4","02-012-0002.mp4"]}
    //private static final String filesUrl  = "https://amtbapi.hwadzan.com/amtbtv/%s/mp4";
    private static final String filesUrl  = "https://amtbapi.hwadzan.com/amtbtv/medias/%s/mp3";
    //                                            http://amtbapi.hwadzan.com/amtbtv/medias/21-786/mp3

    //{"programs":[{"name":"\u7121\u91cf\u58fd\u7d93\u5927\u610f","identifier":"02-002","recDate":"1992.12","recAddress":"\u7f8e\u570b","picCreated":"1","mp4":"1","mp3":"1"}]}
    //private static final String programsUrl  = "https://amtbapi.hwadzan.com/amtbtv/%d/mp4";
    //{"programs":[{"name":"\u7121\u91cf\u58fd\u7d93\u5927\u610f","identifier":"02-002","recDate":"1992.12","recAddress":"\u7f8e\u570b","picCreated":"1","mp4":"1","mp3":"1"}]}
    private static final String programsUrl  = "https://amtbapi.hwadzan.com/amtbtv/%d/mp3";

    //最近视频，暂时先不使用此功能
    //private static final String newMediasUrl = "https://amtbapi.hwadzan.com/amtbtv/newmedias/mp4?limit=20";
    //最近视频，暂时先不使用此功能
    private static final String newMediasUrl = "https://amtbapi.hwadzan.com/amtbtv/newmedias/mp3?limit=20";


    //12-017-0019.mp3
    public static String makeMp3PlayUrl(String mp3, String identifier) {
        //                  http://amtbsg.cloudapp.net/redirect/vod/_definst_/mp3:mp3/02/02-041/02-041-0001.mp3/playlist.m3u8
        //                  http://amtbsg.cloudapp.net/redirect/media/mp3/02/02-041/02-041-0001.mp3
        //String urlFormat = "https://amtbsg.cloudapp.net/redirect/vod/_definst_/mp3:mp3/%s/%s/%s/playlist.m3u8";
        //全自动跳转到最快的服务器
        String urlFormat = "http://amtbsg.cloudapp.net/redirect/media/mp3/%s/%s/%s";
        String[] sp = mp3.split("-");
        String url = String.format(urlFormat, sp[0], identifier, mp3);
        return url;
    }

    public static String getNewsUrl(){
        return "https://www.amtb.tw/tvchannel/show_marquee.asp";
    }

    //http://amtbapi.hwadzan.com/amtbtv/docs/21-786-0001_en/html/zh_TW
    public static String makeMp3DocUrl(String itemId, String country){
        return String.format("https://amtbapi.hwadzan.com/amtbtv/docs/%s/html/zh_%s", itemId, country);
    }

    public static String getBestMp3ServerUrl(){
        return "http://amtbsg.cloudapp.net/loadbalancer/amtbservers.php?singleserver=1&servertype=httpserver&mediatype=media&media=mp3";
    }

    //由XML中的URL转为真实的URL  <fileurl>56k/12/12-017-0019.mp3</fileurl>
    public static String makeDownloadMp3Url(String domain, String xmlFileUrl){
        //"https://amtbsg.cloudapp.net/redirect/media/mp3/"
        //"https://"+m.group(1)+"/media/mp3/"
        //domain js1.amtb.cn
        if(domain.isEmpty())
            return "http://amtbsg.cloudapp.net/redirect/media/mp3/"+xmlFileUrl;
        else
            return "http://"+domain+"/media/mp3/"+xmlFileUrl;
    }

    public static String takeLivesUrl(){
        return livesUrl;
    }

    public static String takeChannelsUrl(){
        return channelsUrl;
    }

    public static String takeFilesUrl(String identifier){
        return String.format(filesUrl, identifier);
    }

    public static String takeProgramsUrl(int amtbid){
        return String.format(programsUrl, amtbid);
    }

    //图片地址
    //http://amtbsg.cloudapp.net/redirect/v/amtbtv/pic/02-037_bg.jpg
    //http://amtbsg.cloudapp.net/redirect/v/amtbtv/pic/02-037_card.jpg

}
