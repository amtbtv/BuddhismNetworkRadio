package com.jianchi.fsp.buddhismnetworkradio.tools;

public class UrlHelper {

    private static final String picUrl = "https://vod.amtb.de/redirect/v/amtbtv/pic/";

    //{"channels":[{"name":"\u963f\u5f4c\u9640\u7d93","amtbid":"1"},{"name":"\u7121\u91cf\u58fd\u7d93","amtbid":"2"}]}
    //private static final String liveChannelsUrl  = "https://amtbapi.amtb.de/amtbtv/channels/live,mp4";
    //https://amtbapi.amtb.de/amtbtv/channels/mp3,mp4
    private static final String livesUrl = "https://amtbapi.amtb.de/amtbtv/channels/live";
    private static final String channelsUrl = "https://amtbapi.amtb.de/amtbtv/channels/mp3,mp4";

    //{"files":["02-012-0001.mp4","02-012-0002.mp4"]}
    //private static final String filesUrl  = "https://amtbapi.amtb.de/amtbtv/%s/mp3";
    //{"files":["02-012-0001.mp4","02-012-0002.mp4"]}
    //private static final String filesUrl  = "https://amtbapi.amtb.de/amtbtv/%s/mp4";
    private static final String mp3FilesUrl = "https://amtbapi.amtb.de/amtbtv/medias/%s/mp3";
    private static final String mp4FilesUrl = "https://amtbapi.amtb.de/amtbtv/medias/%s/mp4";
    //                                            http://amtbapi.amtb.de/amtbtv/medias/21-786/mp4

    //{"programs":[{"name":"\u7121\u91cf\u58fd\u7d93\u5927\u610f","identifier":"02-002","recDate":"1992.12","recAddress":"\u7f8e\u570b","picCreated":"1","mp4":"1","mp3":"1"}]}
    //private static final String programsUrl  = "https://amtbapi.amtb.de/amtbtv/%d/mp4";
    //{"programs":[{"name":"\u7121\u91cf\u58fd\u7d93\u5927\u610f","identifier":"02-002","recDate":"1992.12","recAddress":"\u7f8e\u570b","picCreated":"1","mp4":"1","mp3":"1"}]}
    private static final String programsUrl = "https://amtbapi.amtb.de/amtbtv/%d/mp3";

    //最近视频，暂时先不使用此功能
    //private static final String newMediasUrl = "https://amtbapi.amtb.de/amtbtv/newmedias/mp4?limit=20";
    //最近视频，暂时先不使用此功能
    private static final String newMediasUrl = "https://amtbapi.amtb.de/amtbtv/newmedias/mp3?limit=20";


    //12-017-0019.mp3
    public static String makeMp3PlayUrl(String mp3) {
        //                  http://amtbsg.cloudapp.net/redirect/vod/_definst_/mp3:mp3/02/02-041/02-041-0001.mp3/playlist.m3u8
        //                  http://amtbsg.cloudapp.net/redirect/media/mp3/02/02-041/02-041-0001.mp3
        //String urlFormat = "https://amtbsg.cloudapp.net/redirect/vod/_definst_/mp3:mp3/%s/%s/%s/playlist.m3u8";
        //全自动跳转到最快的服务器
        String urlFormat = "https://vod.amtb.de/redirect/media/mp3/%s/%s/%s";
        String[] sp = mp3.split("-");
        String url = String.format(urlFormat, sp[0], sp[0] + "-" + sp[1], mp3);
        return url;
    }

    public static String makeMp4PlayUrl(String mp4) {
        //                  http://amtbsg.cloudapp.net/redirect/vod/_definst_/mp3:mp3/02/02-041/02-041-0001.mp3/playlist.m3u8
        //                  http://amtbsg.cloudapp.net/redirect/media/mp3/02/02-041/02-041-0001.mp3
        //String urlFormat = "https://amtbsg.cloudapp.net/redirect/vod/_definst_/mp3:mp3/%s/%s/%s/playlist.m3u8";
        //全自动跳转到最快的服务器
        String urlFormat = "https://vod.amtb.de/redirect/media/mp4/%s/%s/%s";
        String[] sp = mp4.split("-");
        String url = String.format(urlFormat, sp[0], sp[0] + "-" + sp[1], mp4);
        return url;
    }

    public static String getNewsUrl(String id, int pager, int per_page) {
        return "https://www.hwadzan.tv/wp-json/wp/v2/posts?categories=" + id + "&page=" + String.valueOf(pager) + "&per_page=" + String.valueOf(per_page);
        //return "http://www.amtb.tw/tvchannel/show_marquee.asp";
    }

    //http://amtbapi.amtb.de/amtbtv/docs/21-786-0001_en/html/zh_TW
    public static String makeMp3DocUrl(String itemId, String country) {
        return String.format("https://amtbapi.amtb.de/amtbtv/docs/%s/html/zh_%s", itemId, country);
    }

    public static String getBestMp3ServerUrl() {
        return "http://vod.amtb.de/loadbalancer/amtbservers.php?singleserver=1&servertype=httpserver&mediatype=media&media=mp3";
    }

    //由XML中的URL转为真实的URL  <fileurl>56k/12/12-017-0019.mp3</fileurl>
    public static String makeDownloadMp3Url(String domain, String xmlFileUrl) {
        //"https://amtbsg.cloudapp.net/redirect/media/mp3/"
        //"https://"+m.group(1)+"/media/mp3/"
        //domain js1.amtb.cn
        if (domain.isEmpty())
            return "http://vod.amtb.de/redirect/media/mp3/" + xmlFileUrl;
        else
            return "http://" + domain + "/media/mp3/" + xmlFileUrl;
    }

    public static String takeLivesUrl() {
        return livesUrl;
    }

    public static String takeChannelsUrl() {
        return channelsUrl;
    }

    public static String takeMp3FilesUrl(String identifier) {
        return String.format(mp3FilesUrl, identifier);
    }

    public static String takeMp4FilesUrl(String identifier) {
        return String.format(mp4FilesUrl, identifier);
    }

    public static String takeProgramsUrl(int amtbid) {
        return String.format(programsUrl, amtbid);
    }

    public static String getNewsCategorieUrl() {
        return "https://amtbapi.amtb.de/amtbtv/news";
    }

    public static String getRsdNewsUrl(int pager, int per_page) {
        return "https://rsd.amtb.tw/wp-json/wp/v2/posts?" + "&page=" + String.valueOf(pager) + "&per_page=" + String.valueOf(per_page);
    }

    //图片地址
    //http://amtbsg.cloudapp.net/redirect/v/amtbtv/pic/02-037_bg.jpg
    //http://amtbsg.cloudapp.net/redirect/v/amtbtv/pic/02-037_card.jpg

}
